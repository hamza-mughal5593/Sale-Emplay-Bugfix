package com.deens.cheese;

public class EmployeeType {

    String UserTypeID;
    String UserTypeName;

    public EmployeeType(String userTypeID, String userTypeName) {
        UserTypeID = userTypeID;
        UserTypeName = userTypeName;
    }

    public String getUserTypeID() {
        return UserTypeID;
    }

    public void setUserTypeID(String userTypeID) {
        UserTypeID = userTypeID;
    }

    public String getUserTypeName() {
        return UserTypeName;
    }

    public void setUserTypeName(String userTypeName) {
        UserTypeName = userTypeName;
    }
}
