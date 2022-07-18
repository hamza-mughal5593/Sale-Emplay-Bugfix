package com.deens.cheese;

public class BranchesClass {

    int BranchID;
    String BranchName;

    public BranchesClass(int branchID, String branchName) {
        BranchID = branchID;
        BranchName = branchName;
    }

    public int getBranchID() {
        return BranchID;
    }

    public void setBranchID(int branchID) {
        BranchID = branchID;
    }

    public String getBranchName() {
        return BranchName;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }
}
