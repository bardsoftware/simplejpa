package com.spaceprogram.simplejpa.papeeria.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gkalabin@bardsoftware.com
 */
@Entity
public class PapeeriaTestObject {
    private String str;
    private String msg;
    private List<PapeeriaTestSubObject1> objects = new ArrayList<PapeeriaTestSubObject1>();
    private List<PapeeriaTestSubObject2> anotherObjects = new ArrayList<PapeeriaTestSubObject2>();

    public PapeeriaTestObject() {
    }

    public PapeeriaTestObject(String str, String msg) {
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

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<PapeeriaTestSubObject2> getAnotherObjects() {
        return anotherObjects;
    }

    public void setAnotherObjects(List<PapeeriaTestSubObject2> anotherObjects) {
        this.anotherObjects = anotherObjects;
    }

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<PapeeriaTestSubObject1> getObjects() {
        return objects;
    }

    public void setObjects(List<PapeeriaTestSubObject1> objects) {
        this.objects = objects;
    }


}
