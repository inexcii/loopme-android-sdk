package com.loopme;

/**
 * Created by katerina on 3/9/17.
 */

public enum IntegrationType {

    NORMAL (0, "normal"), MOPUB (1, "mopub"),
    ADMOB (2, "admob"),
    FYBER (3, "fyber"),
    UNITY (4, "unity"),
    ADOBE_AIR (5, "adobe_air"),
    CORONA(6, "corona"),
    AMR(7, "amr");

    private int mId;
    private String mType;

    IntegrationType(int id, String type) {
        this.mId = id;
        this.mType = type;
    }

    public int getId(){
        return this.mId;
    }

    public String getType(){
        return this.mType;
    }
}
