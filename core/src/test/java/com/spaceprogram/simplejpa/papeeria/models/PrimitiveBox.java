package com.spaceprogram.simplejpa.papeeria.models;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author gkalabin@bardsoftware.com
 */
@Entity
public class PrimitiveBox {
  private String myId;
  private int myInt;
  private long myLong;
  private double myDouble;

  public PrimitiveBox() {
  }

  public PrimitiveBox(String id, int anInt, long aLong, double aDouble) {
    myId = id;
    myInt = anInt;
    myLong = aLong;
    myDouble = aDouble;
  }

  @Id
  public String getId() {
    return myId;
  }

  public void setId(String id) {
    myId = id;
  }

  public int getInt() {
    return myInt;
  }

  public void setInt(int anInt) {
    myInt = anInt;
  }

  public long getLong() {
    return myLong;
  }

  public void setLong(long aLong) {
    myLong = aLong;
  }

  public double getDouble() {
    return myDouble;
  }

  public void setDouble(double aDouble) {
    myDouble = aDouble;
  }
}
