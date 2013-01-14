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
    public List<PapeeriaTestSubObject1> getObjects() {
        return objects;
    }

    public void setObjects(List<PapeeriaTestSubObject1> objects) {
        this.objects = objects;
    }


}
