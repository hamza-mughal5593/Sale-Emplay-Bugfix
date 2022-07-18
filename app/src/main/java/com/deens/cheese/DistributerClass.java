package com.deens.cheese;

public class DistributerClass {

    private int DB_ID;
    private String DB_Name;

    public DistributerClass(int DB_ID, String DB_Name) {
        this.DB_ID = DB_ID;
        this.DB_Name = DB_Name;
    }

    public int getDB_ID() {
        return DB_ID;
    }

    public void setDB_ID(int DB_ID) {
        this.DB_ID = DB_ID;
    }

    public String getDB_Name() {
        return DB_Name;
    }

    public void setDB_Name(String DB_Name) {
        this.DB_Name = DB_Name;
    }
}
