package com.spaceprogram.simplejpa.papeeria.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author gkalabin@bardsoftware.com
 */
@Entity
public class PapeeriaTestSubObject {
    private String str;

    @ManyToOne
    private PapeeriaTestObject parent;

    public PapeeriaTestSubObject() {
    }

    public PapeeriaTestSubObject(String id, PapeeriaTestObject parent) {
        this.str = id;
        this.parent = parent;
    }

    @Id
    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public PapeeriaTestObject getParent() {
        return parent;
    }

    public void setParent(PapeeriaTestObject parent) {
        this.parent = parent;
    }
}