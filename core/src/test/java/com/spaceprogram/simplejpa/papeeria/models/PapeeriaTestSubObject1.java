package com.spaceprogram.simplejpa.papeeria.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * @author gkalabin@bardsoftware.com
 */
@Entity
public class PapeeriaTestSubObject1 {
    private String str;
    private byte[] data;

    @ManyToOne
    private PapeeriaTestObject parent;

    public PapeeriaTestSubObject1() {
    }

    public PapeeriaTestSubObject1(String id, byte[] data, PapeeriaTestObject parent) {
        this.str = id;
        this.data = data;
        this.parent = parent;
    }

    @Id
    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Lob
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public PapeeriaTestObject getParent() {
        return parent;
    }

    public void setParent(PapeeriaTestObject parent) {
        this.parent = parent;
    }
}
