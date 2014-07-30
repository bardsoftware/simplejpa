package com.spaceprogram.simplejpa.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import com.amazonaws.AmazonClientException;
import com.spaceprogram.simplejpa.AnnotationInfo;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.NamingHelper;
import com.spaceprogram.simplejpa.PersistentProperty;

/**
 * Need to support the following: <p/> <p/> - Navigation operator (.) DONE - Arithmetic operators: +, - unary *, / multiplication and division +, - addition and subtraction -
 * Comparison operators : =, >, >=, <, <=, <> (not equal), [NOT] BETWEEN, [NOT] LIKE, [NOT] IN, IS [NOT] NULL, IS [NOT] EMPTY, [NOT] MEMBER [OF] - Logical operators: NOT AND OR
 * <p/> see: http://docs.solarmetric.com/full/html/ejb3_langref.html#ejb3_langref_where <p/> User: treeder Date: Feb 8, 2008 Time: 7:33:20 PM
 */
public class QueryImpl extends AbstractQuery {

    // \b is a word boundary, so \bis\b means that we want to match is whole word only (http://www.regular-expressions.info/wordboundaries.html)
    private static final Pattern CONDITION_PATTERN = Pattern.compile("(<>)|(>=)|(<=)|=|>|<|\\band\\b|\\bor\\b|\\bis\\b|\\blike\\b|\\bin\\b", Pattern.CASE_INSENSITIVE);
    private static Logger logger = Logger.getLogger(QueryImpl.class.getName());

    public static List<String> tokenizeWhere(String where) {
        List<String> split = new ArrayList<String>();
        Matcher matcher = CONDITION_PATTERN.matcher(where);
        int lastIndex = 0;
        String s;
        while (matcher.find()) {
            s = where.substring(lastIndex, matcher.start()).trim();
            logger.finest("value: " + s);
            split.add(s);
            s = matcher.group();
            split.add(s);
            logger.finest("matcher found: " + s + " at " + matcher.start() + " to " + matcher.end());
            lastIndex = matcher.end();
        }
        s = where.substring(lastIndex).trim();
        logger.finest("final:" + s);
        split.add(s);
        return split;
    }

    private JPAQuery q;

    private String qString;

    // private AmazonQueryString amazonQuery;
    private Map<String, List<String>> foreignIds = new HashMap();

    public QueryImpl(EntityManagerSimpleJPA em, JPAQuery q) {
        super(em);
        this.q = q;
        this.qString = q.toString();
        init(em);
    }

    public QueryImpl(EntityManagerSimpleJPA em, String qString) {
        super(em);
        this.qString = qString;
        logger.fine("query=" + qString);
        this.q = new JPAQuery();
        JPAQueryParser parser = new JPAQueryParser(q, qString);
        parser.parse();
        init(em);
    }

    private Boolean appendCondition(Class tClass, StringBuilder sb, String field, String comparator, String param) {
        comparator = comparator.toLowerCase();
        AnnotationInfo ai = em.getAnnotationManager().getAnnotationInfo(tClass);

        String fieldSplit[] = field.split("\\.");
        if (fieldSplit.length == 1) {
            field = fieldSplit[0];
// System.out.println("split: " + field + " param=" + param);
            if (field.equals(param)) {
                return false;
            }
        } else if (fieldSplit.length == 2) {
            field = fieldSplit[1];
        } else if (fieldSplit.length == 3) {
            // NOTE: ONLY SUPPORTING SECOND LEVEL OF GRAPH RIGHT NOW
            // then we have to reach down the graph here. eg: myOb.ob2.name or myOb.ob2.id
            // if filtering by id, then don't need to query for second object, just add a filter on the id field
            String refObjectField = fieldSplit[1];
            field = fieldSplit[2];
// System.out.println("field=" + field);
            Class refType = ai.getPersistentProperty(refObjectField).getPropertyClass();
            AnnotationInfo refAi = em.getAnnotationManager().getAnnotationInfo(refType);
            PersistentProperty getterForField = refAi.getPersistentProperty(field);
// System.out.println("getter=" + getterForField);
            String paramValue = getParamValueAsStringForAmazonQuery(param, getterForField);
            logger.finest("paramValue=" + paramValue);
            String idFieldName = refAi.getIdMethod().getFieldName();
            if (idFieldName.equals(field)) {
                logger.finer("Querying using id field, no second query required.");
                appendFilter(sb, NamingHelper.foreignKey(refObjectField), comparator, paramValue);
            } else {
                // no id method, so query for other object(s) first, then apply the returned value to the original query.
                // todo: this needs some work (multiple ref objects? multiple params on same ref object?)
                List<String> ids = foreignIds.get(field);
// System.out.println("got foreign ids=" + ids);
                if (ids == null) {
                    Query sub = em.createQuery("select o from " + refType.getName() + " o where o." + field + " " + comparator + " :paramValue");
                    sub.setParameter("paramValue", parameters.get(paramName(param)));
                    List subResults = sub.getResultList();
                    ids = new ArrayList<String>();
                    for (Object subResult : subResults) {
                        ids.add(em.getId(subResult));
                    }
                    foreignIds.put(field, ids); // Store the ids for next use, really reduces queries when using this repetitively
                }
                if (ids.size() > 0) {
                    appendIn(sb, NamingHelper.foreignKey(refObjectField), ids);
                } else {
                    // no matches so should return nothing right? only if an AND query I guess
                    return null;
                }
            }
            return true;
        } else {
            throw new PersistenceException("Invalid field used in query: " + field);
        }
        logger.finest("field=" + field);
// System.out.println("field=" + field + " paramValue=" + param);
        PersistentProperty getterForField = ai.getPersistentProperty(field);
        if (getterForField == null) {
            throw new PersistenceException("No getter for field: " + field);
        }
        String columnName = getterForField.getColumnName();
        if (columnName == null) {
            columnName = field;
        }
        if (comparator.equals("is")) {
            if (param.equalsIgnoreCase("null")) {
                sb.append(columnName).append(" is null");
            } else if (param.equalsIgnoreCase("not null")) {
                sb.append(columnName).append(" is not null");
            } else {
                throw new PersistenceException("Must use only 'is null' or 'is not null' with where condition containing 'is'");
            }
        } else if (comparator.equals("in")) {
          Object valueForIn = getParameterValue(param);
          // good values for in clause are:
          if (valueForIn instanceof Collection && !((Collection) valueForIn).isEmpty()) {
              // non-empty collection of values
              appendIn(sb, columnName, (Collection<?>) valueForIn);
          } else if (valueForIn instanceof String && ((String) valueForIn).matches("\\(.+\\)")) {
              // string with something in parenthesis (for raw values)
              appendFilter(sb, columnName, comparator, (String) valueForIn);
          } else {
              // the other values are bad
              throw new PersistenceException(String.format("Bad value for in clause: %s", valueForIn));
          }
        } else {
            /* handle the <> comparator which needs to convert to != for SimpleDB */
            if(comparator.equals("<>")) {
                comparator = "!=";
            }
            /* continue with appending */
            String paramValue = getParamValueAsStringForAmazonQuery(param, getterForField);
            logger.finer("paramValue=" + paramValue);
            logger.finer("comp=[" + comparator + "]");
            appendFilter(sb, columnName, comparator, paramValue);
        }
        return true;
    }

    private void appendFilter(StringBuilder sb, boolean not, String field, String comparator, String param, boolean quoteParam) {
        if (not) {
            sb.append("not ");
        }
        appendField(sb, field);
        sb.append(" ");
        sb.append(comparator);
        sb.append(" ");
        if (quoteParam) {
            sb.append("'");
        }
        sb.append(param);
        if (quoteParam) {
            sb.append("'");
        }
    }

    private void appendFilter(StringBuilder sb, String field, String comparator, String param) {
        appendFilter(sb, false, field, comparator, param, false);
    }

    private void appendIn(StringBuilder sb, String field, Collection<?> params) {
        appendField(sb, field);
        sb.append(" ");
        sb.append("IN");
        sb.append(" (");
        boolean firstParam = true;
        for (Object param : params) {
            if (!firstParam) {
                sb.append(",");
            }
            sb.append("'").append(param).append("'");
            firstParam = false;
        }
        sb.append(")");
    }
  
    private void appendField(StringBuilder sb, String field) {
        boolean quoteField = !NamingHelper.NAME_FIELD_REF.equals(field);
        if (quoteField) {
            sb.append("`");
        }
        sb.append(field);
        if (quoteField) {
            sb.append("`");
        }
    }

    public AmazonQueryString createAmazonQuery(boolean appendLimit) throws NoResultsException, AmazonClientException {
        String select = q.getResult();
        boolean count = false;
        if (select != null && select.contains("count")) {
// System.out.println("HAS COUNT: " + select);
            count = true;
        }
        AnnotationInfo ai = em.getAnnotationManager().getAnnotationInfo(tClass);

        // Make sure querying the root Entity class
        String domainName = em.getDomainName(ai.getRootClass());
        if (domainName == null) {
            return null;
// throw new NoResultsException();
        }
        StringBuilder amazonQuery;
        if (q.getFilter() != null) {
            amazonQuery = toAmazonQuery(tClass, q);
            if (amazonQuery == null) {
// throw new NoResultsException();
                return null;
            }
        } else {
            amazonQuery = new StringBuilder();
        }
        if (ai.getDiscriminatorValue() != null) {
            if (amazonQuery.length() == 0) {
                amazonQuery = new StringBuilder();
            } else {
                amazonQuery.append(" and ");
            }
            appendFilter(amazonQuery, EntityManagerFactoryImpl.DTYPE, "=", "'" + ai.getDiscriminatorValue() + "'");
        }

        // now for sorting
        String orderBy = q.getOrdering();
        if (orderBy != null && orderBy.length() > 0) {
// amazonQuery.append(" sort ");
            amazonQuery.append(" order by ");
            String orderByOrder = "asc";
            String orderBySplit[] = orderBy.split(" ");
            if (orderBySplit.length > 2) {
                throw new PersistenceException("Can only sort on a single attribute in SimpleDB. Your order by is: " + orderBy);
            }
            if (orderBySplit.length == 2) {
                orderByOrder = orderBySplit[1];
            }
            String orderByAttribute = orderBySplit[0];
            String fieldSplit[] = orderByAttribute.split("\\.");
            if (fieldSplit.length == 1) {
                orderByAttribute = fieldSplit[0];
            } else if (fieldSplit.length == 2) {
                orderByAttribute = fieldSplit[1];
            }
// amazonQuery.append("'");
            amazonQuery.append(orderByAttribute);
// amazonQuery.append("'");
            amazonQuery.append(" ").append(orderByOrder);
        }
        StringBuilder fullQuery = new StringBuilder();
        fullQuery.append("select ");
        fullQuery.append(count ? "count(*)" : "*");
        fullQuery.append(" from `").append(domainName).append("` ");
        if (amazonQuery.length() > 0) {
            fullQuery.append("where ");
            fullQuery.append(amazonQuery);
        }
        String logString = "amazonQuery: Domain=" + domainName + ", query=" + fullQuery;
        logger.fine(logString);
        if (em.getFactory().isPrintQueries()) {
            System.out.println(logString);
        }

        if (!count && appendLimit && maxResults >= 0) {
            fullQuery.append(" limit ").append(Math.min(MAX_RESULTS_PER_REQUEST, maxResults));
        }
        return new AmazonQueryString(fullQuery.toString(), count);
    }

    public Map<String, List<String>> getForeignIds() {
        return foreignIds;
    }

    public JPAQuery getQ() {
        return q;
    }

    public String getQString() {
        return qString;
    }

    private void init(EntityManagerSimpleJPA em) {

        String from = q.getFrom();
        logger.finer("from=" + from);
        logger.finer("where=" + q.getFilter());
        if (q.getOrdering() != null && q.getFilter() == null) {
            throw new PersistenceException("Attribute in ORDER BY [" + q.getOrdering() + "] must be included in a WHERE filter.");
        }

        String split[] = q.getFrom().split(" ");
        String obClass = split[0];
        tClass = em.ensureClassIsEntity(obClass);
    }

    public void setForeignIds(Map<String, List<String>> foreignIds) {
        this.foreignIds = foreignIds;
    }

    public int getCount() {
        try {
            if (logger.isLoggable(Level.FINER))
                logger.finer("Getting size.");
            JPAQuery queryClone = (JPAQuery) getQ().clone();
            queryClone.setResult("count(*)");
            QueryImpl query2 = new QueryImpl(em, queryClone);
            query2.setParameters(getParameters());
            query2.setForeignIds(getForeignIds());
            List results = query2.getResultList();
            int resultCount = ((Long) results.get(0)).intValue();
            if (logger.isLoggable(Level.FINER))
                logger.finer("Got:" + resultCount);

            if (maxResults >= 0 && resultCount > maxResults) {
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Too much, adjusting to maxResults: " + maxResults);
                return maxResults;
            } else {
                return resultCount;
            }
        } catch (CloneNotSupportedException e) {
            throw new PersistenceException(e);
        }
    }

    /*
     * public AmazonQueryString getAmazonQuery() { return amazonQuery; } public void setAmazonQuery(AmazonQueryString amazonQuery) { this.amazonQuery = amazonQuery; }
     */
    public void setQ(JPAQuery q) {
        this.q = q;
    }

    public void setQString(String qString) {
        this.qString = qString;
    }

    public StringBuilder toAmazonQuery(Class tClass, JPAQuery q) {
        StringBuilder sb = new StringBuilder();
        String where = q.getFilter();
        where = where.trim();
        // now split it into pieces
        List<String> whereTokens = tokenizeWhere(where);
        Boolean aok = false;
        for (int i = 0; i < whereTokens.size();) {
            if (aok && i > 0) {
                String andOr = whereTokens.get(i);
                if (andOr.equalsIgnoreCase("OR")) {
                    sb.append(" or ");
                } else {
                    sb.append(" and ");
                }
            }
            if (i > 0) {
                i++;
            }
// System.out.println("sbbefore=" + sb);
            // special null cases: is null and is not null
            String firstParam = whereTokens.get(i);
            i++;
            String secondParam = whereTokens.get(i);
            i++;
            String thirdParam = whereTokens.get(i);
            if (thirdParam.equalsIgnoreCase("not")) {
                i++;
                thirdParam += " " + whereTokens.get(i);
            }
            i++;
            aok = appendCondition(tClass, sb, firstParam, secondParam, thirdParam);
// System.out.println("sbafter=" + sb);
            if (aok == null) {
                return null; // todo: only return null if it's an AND query, or's should still continue, but skip the intersection part
            }
        }

        logger.fine("query=" + sb);
        return sb;
    }

    @Override
    public String toString() {
        return "QueryImpl{" + "em=" + em + ", q=" + q + ", parameters=" + parameters + ", maxResults=" + maxResults + ", qString='" + qString + '\'' + '}';
    }

}
