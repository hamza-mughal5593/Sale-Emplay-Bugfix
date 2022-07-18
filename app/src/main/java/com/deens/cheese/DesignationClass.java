package com.deens.cheese;

public class DesignationClass {

    int DesignationID;
    String DesignationName;

    public DesignationClass(int designationID, String designationName) {
        DesignationID = designationID;
        DesignationName = designationName;
    }

    public int getDesignationID() {
        return DesignationID;
    }

    public void setDesignationID(int designationID) {
        DesignationID = designationID;
    }

    public String getDesignationName() {
        return DesignationName;
    }

    public void setDesignationName(String designationName) {
        DesignationName = designationName;
    }
}
