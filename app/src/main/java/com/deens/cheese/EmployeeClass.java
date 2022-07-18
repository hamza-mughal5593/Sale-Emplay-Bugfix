package com.deens.cheese;

public class EmployeeClass {

    private int UserID;

    private String UserName;

    private String Password;

    private int UserTypeID;

    private String UserTypeName;

    private int EmpID;

    private String EmpCode;

    private String EmpName;

    private String EmpFatherName;

    private String CNIC;

    private String DOB;

    private String Gender;

    private int EmpCityID;

    private String EmpCityName;

    private String EmpAddress;

    private String ContactNo;

    private String ContactNo2nd;

    private int BranchID;

    private String BranchName;

    private int DepartmentID;

    private String DepartmentName;

    private int DesignationID;

    private String DesignationName;

    private String DOJ;

    private int EmpStatusID;

    private String EmpStatusName;

    private String DOR;

    private String Remarks;

    private int GM;

    private String GMName;

    private int ST;

    private String STName;

    private int DM;

    private String DMName;

    private int SDM;

    private String SDMName;

    private int SO;

    private String SOName;

    public EmployeeClass(int userID, String userName, String password, int userTypeID, String userTypeName, int empID,
                         String empCode, String empName, String empFatherName, String CNIC, String DOB, String gender,
                         int empCityID, String empCityName, String empAddress, String contactNo, String contactNo2nd,
                         int branchID, String branchName, int departmentID, String departmentName, int designationID,
                         String designationName, String DOJ, int empStatusID, String empStatusName, String DOR,
                         String remarks, int GM, String GMName, int ST, String STName, int DM, String DMName, int SDM,
                         String SDMName, int SO, String SOName) {
        UserID = userID;
        UserName = userName;
        Password = password;
        UserTypeID = userTypeID;
        UserTypeName = userTypeName;
        EmpID = empID;
        EmpCode = empCode;
        EmpName = empName;
        EmpFatherName = empFatherName;
        this.CNIC = CNIC;
        this.DOB = DOB;
        Gender = gender;
        EmpCityID = empCityID;
        EmpCityName = empCityName;
        EmpAddress = empAddress;
        ContactNo = contactNo;
        ContactNo2nd = contactNo2nd;
        BranchID = branchID;
        BranchName = branchName;
        DepartmentID = departmentID;
        DepartmentName = departmentName;
        DesignationID = designationID;
        DesignationName = designationName;
        this.DOJ = DOJ;
        EmpStatusID = empStatusID;
        EmpStatusName = empStatusName;
        this.DOR = DOR;
        Remarks = remarks;
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

    public EmployeeClass(int userID, String empName, int soID) {
        UserID = userID;
        EmpName = empName;
        SO = soID;
    }

    public void setUserID(int UserID){
        this.UserID = UserID;
    }
    public int getUserID(){
        return this.UserID;
    }
    public void setUserName(String UserName){
        this.UserName = UserName;
    }
    public String getUserName(){
        return this.UserName;
    }
    public void setPassword(String Password){
        this.Password = Password;
    }
    public String getPassword(){
        return this.Password;
    }
    public void setUserTypeID(int UserTypeID){
        this.UserTypeID = UserTypeID;
    }
    public int getUserTypeID(){
        return this.UserTypeID;
    }
    public void setUserTypeName(String UserTypeName){
        this.UserTypeName = UserTypeName;
    }
    public String getUserTypeName(){
        return this.UserTypeName;
    }
    public void setEmpID(int EmpID){
        this.EmpID = EmpID;
    }
    public int getEmpID(){
        return this.EmpID;
    }
    public void setEmpCode(String EmpCode){
        this.EmpCode = EmpCode;
    }
    public String getEmpCode(){
        return this.EmpCode;
    }
    public void setEmpName(String EmpName){
        this.EmpName = EmpName;
    }
    public String getEmpName(){
        return this.EmpName;
    }
    public void setEmpFatherName(String EmpFatherName){
        this.EmpFatherName = EmpFatherName;
    }
    public String getEmpFatherName(){
        return this.EmpFatherName;
    }
    public void setCNIC(String CNIC){
        this.CNIC = CNIC;
    }
    public String getCNIC(){
        return this.CNIC;
    }
    public void setDOB(String DOB){
        this.DOB = DOB;
    }
    public String getDOB(){
        return this.DOB;
    }
    public void setGender(String Gender){
        this.Gender = Gender;
    }
    public String getGender(){
        return this.Gender;
    }
    public void setEmpCityID(int EmpCityID){
        this.EmpCityID = EmpCityID;
    }
    public int getEmpCityID(){
        return this.EmpCityID;
    }
    public void setEmpCityName(String EmpCityName){
        this.EmpCityName = EmpCityName;
    }
    public String getEmpCityName(){
        return this.EmpCityName;
    }
    public void setEmpAddress(String EmpAddress){
        this.EmpAddress = EmpAddress;
    }
    public String getEmpAddress(){
        return this.EmpAddress;
    }
    public void setContactNo(String ContactNo){
        this.ContactNo = ContactNo;
    }
    public String getContactNo(){
        return this.ContactNo;
    }
    public void setContactNo2nd(String ContactNo2nd){
        this.ContactNo2nd = ContactNo2nd;
    }
    public String getContactNo2nd(){
        return this.ContactNo2nd;
    }
    public void setBranchID(int BranchID){
        this.BranchID = BranchID;
    }
    public int getBranchID(){
        return this.BranchID;
    }
    public void setBranchName(String BranchName){
        this.BranchName = BranchName;
    }
    public String getBranchName(){
        return this.BranchName;
    }
    public void setDepartmentID(int DepartmentID){
        this.DepartmentID = DepartmentID;
    }
    public int getDepartmentID(){
        return this.DepartmentID;
    }
    public void setDepartmentName(String DepartmentName){
        this.DepartmentName = DepartmentName;
    }
    public String getDepartmentName(){
        return this.DepartmentName;
    }
    public void setDesignationID(int DesignationID){
        this.DesignationID = DesignationID;
    }
    public int getDesignationID(){
        return this.DesignationID;
    }
    public void setDesignationName(String DesignationName){
        this.DesignationName = DesignationName;
    }
    public String getDesignationName(){
        return this.DesignationName;
    }
    public void setDOJ(String DOJ){
        this.DOJ = DOJ;
    }
    public String getDOJ(){
        return this.DOJ;
    }
    public void setEmpStatusID(int EmpStatusID){
        this.EmpStatusID = EmpStatusID;
    }
    public int getEmpStatusID(){
        return this.EmpStatusID;
    }
    public void setEmpStatusName(String EmpStatusName){
        this.EmpStatusName = EmpStatusName;
    }
    public String getEmpStatusName(){
        return this.EmpStatusName;
    }
    public void setDOR(String DOR){
        this.DOR = DOR;
    }
    public String getDOR(){
        return this.DOR;
    }
    public void setRemarks(String Remarks){
        this.Remarks = Remarks;
    }
    public String getRemarks(){
        return this.Remarks;
    }
    public void setGM(int GM){
        this.GM = GM;
    }
    public int getGM(){
        return this.GM;
    }
    public void setGMName(String GMName){
        this.GMName = GMName;
    }
    public String getGMName(){
        return this.GMName;
    }
    public void setDM(int DM){
        this.DM = DM;
    }
    public int getDM(){
        return this.DM;
    }
    public void setDMName(String DMName){
        this.DMName = DMName;
    }
    public String getDMName(){
        return this.DMName;
    }
    public void setSDM(int SDM){
        this.SDM = SDM;
    }
    public int getSDM(){
        return this.SDM;
    }
    public void setSDMName(String SDMName){
        this.SDMName = SDMName;
    }
    public String getSDMName(){
        return this.SDMName;
    }
    public void setSO(int SO){
        this.SO = SO;
    }
    public int getSO(){
        return this.SO;
    }
    public void setSOName(String SOName){
        this.SOName = SOName;
    }
    public String getSOName(){
        return this.SOName;
    }
    public int getST() {
        return ST;
    }
    public void setST(int ST) {
        this.ST = ST;
    }
    public String getSTName() {
        return STName;
    }
    public void setSTName(String STName) {
        this.STName = STName;
    }
}
