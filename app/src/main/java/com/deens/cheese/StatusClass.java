package com.deens.cheese;

public class StatusClass {

    int EmpStatusID;
    String EmpStatusName;

    public StatusClass(int empStatusID, String empStatusName) {
        EmpStatusID = empStatusID;
        EmpStatusName = empStatusName;
    }

    public int getEmpStatusID() {
        return EmpStatusID;
    }

    public void setEmpStatusID(int empStatusID) {
        EmpStatusID = empStatusID;
    }

    public String getEmpStatusName() {
        return EmpStatusName;
    }

    public void setEmpStatusName(String empStatusName) {
        EmpStatusName = empStatusName;
    }
}
