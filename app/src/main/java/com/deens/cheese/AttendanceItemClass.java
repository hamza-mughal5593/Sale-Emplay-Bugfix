package com.deens.cheese;

public class AttendanceItemClass {

    private String EmpCode;

    private String EmpName;

    private String DesignationTitle;

    private String AttDescription;

    private String DutyTime;

    private String CheckIn;

    private String LateMinutes;

    private String CheckOut;

    private String WorkingMinutes;

    private String Lat;

    private String Long;

    private String Location;

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
    public void setDesignationTitle(String DesignationTitle){
        this.DesignationTitle = DesignationTitle;
    }
    public String getDesignationTitle(){
        return this.DesignationTitle;
    }
    public void setAttDescription(String AttDescription){
        this.AttDescription = AttDescription;
    }
    public String getAttDescription(){
        return this.AttDescription;
    }
    public void setDutyTime(String DutyTime){
        this.DutyTime = DutyTime;
    }
    public String getDutyTime(){
        return this.DutyTime;
    }
    public void setCheckIn(String CheckIn){
        this.CheckIn = CheckIn;
    }
    public String getCheckIn(){
        return this.CheckIn;
    }
    public void setLateMinutes(String LateMinutes){
        this.LateMinutes = LateMinutes;
    }
    public String getLateMinutes(){
        return this.LateMinutes;
    }
    public void setCheckOut(String CheckOut){
        this.CheckOut = CheckOut;
    }
    public String getCheckOut(){
        return this.CheckOut;
    }
    public void setWorkingMinutes(String WorkingMinutes){
        this.WorkingMinutes = WorkingMinutes;
    }
    public String getWorkingMinutes(){
        return this.WorkingMinutes;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLong() {
        return Long;
    }

    public void setLong(String aLong) {
        Long = aLong;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public AttendanceItemClass(String empCode, String empName, String designationTitle, String attDescription,
                               String dutyTime, String checkIn, String lateMinutes, String checkOut,
                               String workingMinutes, String lat, String aLong, String location) {
        EmpCode = empCode;
        EmpName = empName;
        DesignationTitle = designationTitle;
        AttDescription = attDescription;
        DutyTime = dutyTime;
        CheckIn = checkIn;
        LateMinutes = lateMinutes;
        CheckOut = checkOut;
        WorkingMinutes = workingMinutes;
        Lat = lat;
        Long = aLong;
        Location = location;
    }
}
