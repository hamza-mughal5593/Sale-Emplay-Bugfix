package com.deens.cheese;

public class DepartmentClass {

    int DepartmentID;
    String DepartmentName;

    public DepartmentClass(int departmentID, String departmentName) {
        DepartmentID = departmentID;
        DepartmentName = departmentName;
    }

    public int getDepartmentID() {
        return DepartmentID;
    }

    public void setDepartmentID(int departmentID) {
        DepartmentID = departmentID;
    }

    public String getDepartmentName() {
        return DepartmentName;
    }

    public void setDepartmentName(String departmentName) {
        DepartmentName = departmentName;
    }
}
