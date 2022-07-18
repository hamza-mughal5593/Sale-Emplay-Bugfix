package com.deens.cheese;

public class LeaveClass {

    Double leaveID;
    String leaveTitle;

    public LeaveClass(Double leaveID, String leaveTitle) {
        this.leaveID = leaveID;
        this.leaveTitle = leaveTitle;
    }

    public Double getLeaveID() {
        return leaveID;
    }

    public void setLeaveID(Double leaveID) {
        this.leaveID = leaveID;
    }

    public String getLeaveTitle() {
        return leaveTitle;
    }

    public void setLeaveTitle(String leaveTitle) {
        this.leaveTitle = leaveTitle;
    }
}
