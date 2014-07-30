package com.spaceprogram.simplejpa.papeeria.models;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author gkalabin@bardsoftware.com
 */
@Entity
public class PlainObject {
  private String str;
  private String msg;

  public PlainObject() {
  }

  public PlainObject(String str, String msg) {
    this.str = str;
    this.msg = msg;
  }

  @Id
  public String getStr() {
    return str;
  }

  public void setStr(String str) {
    this.str = str;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PlainObject)) return false;

    PlainObject that = (PlainObject) o;

    if (msg != null ? !msg.equals(that.msg) : that.msg != null) return false;
    if (str != null ? !str.equals(that.str) : that.str != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = str != null ? str.hashCode() : 0;
    result = 31 * result + (msg != null ? msg.hashCode() : 0);
    return result;
  }
}