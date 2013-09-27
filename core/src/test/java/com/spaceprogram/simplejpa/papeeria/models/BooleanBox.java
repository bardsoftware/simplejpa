package com.spaceprogram.simplejpa.papeeria.models;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author gkalabin@papeeria.com
 */
@Entity
public class BooleanBox {
  private String myId;
  private Boolean myBooleanObject;

  public BooleanBox() {
  }

  public BooleanBox(String id, Boolean booleanObject) {
    myId = id;
    myBooleanObject = booleanObject;
  }

  @Id
  public String getId() {
    return myId;
  }

  public void setId(String id) {
    myId = id;
  }

  public Boolean getBooleanObject() {
    return myBooleanObject;
  }

  public void setBooleanObject(Boolean booleanObject) {
    myBooleanObject = booleanObject;
  }
}
