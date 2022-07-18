package com.deens.cheese;

public class TargetClass {

    private int ID;

    private int SO_ID;

    private String SOName;

    private String DateFrom;

    private String DateTo;

    private Double SaleTarget;

    public void setID(int ID){
        this.ID = ID;
    }
    public int getID(){
        return this.ID;
    }
    public void setSO_ID(int SO_ID){
        this.SO_ID = SO_ID;
    }
    public int getSO_ID(){
        return this.SO_ID;
    }
    public void setSOName(String SOName){
        this.SOName = SOName;
    }
    public String getSOName(){
        return this.SOName;
    }
    public void setDateFrom(String DateFrom){
        this.DateFrom = DateFrom;
    }
    public String getDateFrom(){
        return this.DateFrom;
    }
    public void setDateTo(String DateTo){
        this.DateTo = DateTo;
    }
    public String getDateTo(){
        return this.DateTo;
    }
    public void setSaleTarget(Double SaleTarget){
        this.SaleTarget = SaleTarget;
    }
    public Double getSaleTarget(){
        return this.SaleTarget;
    }

    public TargetClass(int ID, int SO_ID, String SOName, String dateFrom, String dateTo, Double saleTarget) {
        this.ID = ID;
        this.SO_ID = SO_ID;
        this.SOName = SOName;
        DateFrom = dateFrom;
        DateTo = dateTo;
        SaleTarget = saleTarget;
    }
}
