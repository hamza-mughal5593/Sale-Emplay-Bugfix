package com.deens.cheese;

public class UserClass {

    private int UserID;

    private String UserName;

    private String EmpName;

    private String EmpCode;

    private String PhoneNo;

    private String BranchName;

    private String DepartmentName;

    private String DesignationName;

    private int UserTypeID;

    private String UserTypeName;

    private String GM;

    private String GMName;

    private String ST;

    private String STName;

    private String DM;

    private String DMName;

    private String SDM;

    private String SDMName;

    private String SO;

    private String SOName;

    public UserClass(int userID, String userName, String empName, String empCode, String phoneNo, String branchName,
                     String departmentName, String designationName, int userTypeID, String userTypeName, String GM,
                     String GMName, String ST, String STName, String DM, String DMName, String SDM, String SDMName,
                     String SO, String SOName) {
        UserID = userID;
        UserName = userName;
        EmpName = empName;
        EmpCode = empCode;
        PhoneNo = phoneNo;
        BranchName = branchName;
        DepartmentName = departmentName;
        DesignationName = designationName;
        UserTypeID = userTypeID;
        UserTypeName = userTypeName;
        this.GM = GM;
        this.GMName = GMName;
        this.ST = ST;
        this.STName = STName;
        this.DM = DM;
        this.DMName = DMName;
        this.SDM = SDM;
        this.SDMName = SDMName;
        this.SO = SO;
        this.SOName = SOName;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getEmpName() {
        return EmpName;
    }

    public void setEmpName(String empName) {
        EmpName = empName;
    }

    public String getEmpCode() {
        return EmpCode;
    }

    public void setEmpCode(String empCode) {
        EmpCode = empCode;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        PhoneNo = phoneNo;
    }

    public String getBranchName() {
        return BranchName;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }

    public String getDepartmentName() {
        return DepartmentName;
    }

    public void setDepartmentName(String departmentName) {
        DepartmentName = departmentName;
    }

    public String getDesignationName() {
        return DesignationName;
    }

    public void setDesignationName(String designationName) {
        DesignationName = designationName;
    }

    public int getUserTypeID() {
        return UserTypeID;
    }

    public void setUserTypeID(int userTypeID) {
        UserTypeID = userTypeID;
    }

    public String getUserTypeName() {
        return UserTypeName;
    }

    public void setUserTypeName(String userTypeName) {
        UserTypeName = userTypeName;
    }

    public String getGM() {
        return GM;
    }

    public void setGM(String GM) {
        this.GM = GM;
    }

    public String getGMName() {
        return GMName;
    }

    public void setGMName(String GMName) {
        this.GMName = GMName;
    }

    public String getST() {
        return ST;
    }

    public void setST(String ST) {
        this.ST = ST;
    }

    public String getSTName() {
        return STName;
    }

    public void setSTName(String STName) {
        this.STName = STName;
    }

    public String getDM() {
        return DM;
    }

    public void setDM(String DM) {
        this.DM = DM;
    }

    public String getDMName() {
        return DMName;
    }

    public void setDMName(String DMName) {
        this.DMName = DMName;
    }

    public String getSDM() {
        return SDM;
    }

    public void setSDM(String SDM) {
        this.SDM = SDM;
    }

    public String getSDMName() {
        return SDMName;
    }

    public void setSDMName(String SDMName) {
        this.SDMName = SDMName;
    }

    public String getSO() {
        return SO;
    }

    public void setSO(String SO) {
        this.SO = SO;
    }

    public String getSOName() {
        return SOName;
    }

    public void setSOName(String SOName) {
        this.SOName = SOName;
    }
}
