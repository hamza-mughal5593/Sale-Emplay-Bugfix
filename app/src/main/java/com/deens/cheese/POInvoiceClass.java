package com.deens.cheese;

import java.util.List;

public class POInvoiceClass {

    private int POID;

    private String PO_Number;

    private String PODate;

    private String Remarks;

    private int SubTotal_Amt;

    private int TotalTax;

    private int TotalAmt;

    private int CusCustomerID;

    private String CusCustomerCode;

    private String CusCustomerName;

    private int CusCityID;

    private String CusCityName;

    private String CusArea;

    private String CusAddress;

    private String CusMobile;

    private String CusMobile1;

    private List<POProductDetailClass> POProductDetailClass;

    public void setPOID(int POID){
        this.POID = POID;
    }
    public int getPOID(){
        return this.POID;
    }
    public void setPO_Number(String PO_Number){
        this.PO_Number = PO_Number;
    }
    public String getPO_Number(){
        return this.PO_Number;
    }
    public void setPODate(String PODate){
        this.PODate = PODate;
    }
    public String getPODate(){
        return this.PODate;
    }
    public void setRemarks(String Remarks){
        this.Remarks = Remarks;
    }
    public String getRemarks(){
        return this.Remarks;
    }
    public void setSubTotal_Amt(int SubTotal_Amt){
        this.SubTotal_Amt = SubTotal_Amt;
    }
    public int getSubTotal_Amt(){
        return this.SubTotal_Amt;
    }
    public void setTotalTax(int TotalTax){
        this.TotalTax = TotalTax;
    }
    public int getTotalTax(){
        return this.TotalTax;
    }
    public void setTotalAmt(int TotalAmt){
        this.TotalAmt = TotalAmt;
    }
    public int getTotalAmt(){
        return this.TotalAmt;
    }
    public void setCusCustomerID(int CusCustomerID){
        this.CusCustomerID = CusCustomerID;
    }
    public int getCusCustomerID(){
        return this.CusCustomerID;
    }
    public void setCusCustomerCode(String CusCustomerCode){
        this.CusCustomerCode = CusCustomerCode;
    }
    public String getCusCustomerCode(){
        return this.CusCustomerCode;
    }
    public void setCusCustomerName(String CusCustomerName){
        this.CusCustomerName = CusCustomerName;
    }
    public String getCusCustomerName(){
        return this.CusCustomerName;
    }
    public void setCusCityID(int CusCityID){
        this.CusCityID = CusCityID;
    }
    public int getCusCityID(){
        return this.CusCityID;
    }
    public void setCusCityName(String CusCityName){
        this.CusCityName = CusCityName;
    }
    public String getCusCityName(){
        return this.CusCityName;
    }
    public void setCusArea(String CusArea){
        this.CusArea = CusArea;
    }
    public String getCusArea(){
        return this.CusArea;
    }
    public void setCusAddress(String CusAddress){
        this.CusAddress = CusAddress;
    }
    public String getCusAddress(){
        return this.CusAddress;
    }
    public void setCusMobile(String CusMobile){
        this.CusMobile = CusMobile;
    }
    public String getCusMobile(){
        return this.CusMobile;
    }
    public void setCusMobile1(String CusMobile1){
        this.CusMobile1 = CusMobile1;
    }
    public String getCusMobile1(){
        return this.CusMobile1;
    }
    public void setPOProductDetailClass(List<POProductDetailClass> POProductDetailClass){
        this.POProductDetailClass = POProductDetailClass;
    }
    public List<POProductDetailClass> getPOProductDetailClass(){
        return this.POProductDetailClass;
    }

    public POInvoiceClass(int POID, String PO_Number, String PODate, String remarks, int subTotal_Amt, int totalTax, int totalAmt,
                          int cusCustomerID, String cusCustomerCode, String cusCustomerName, int cusCityID, String cusCityName,
                          String cusArea, String cusAddress, String cusMobile, String cusMobile1, List<POProductDetailClass> POProductDetailClass) {
        this.POID = POID;
        this.PO_Number = PO_Number;
        this.PODate = PODate;
        Remarks = remarks;
        SubTotal_Amt = subTotal_Amt;
        TotalTax = totalTax;
        TotalAmt = totalAmt;
        CusCustomerID = cusCustomerID;
        CusCustomerCode = cusCustomerCode;
        CusCustomerName = cusCustomerName;
        CusCityID = cusCityID;
        CusCityName = cusCityName;
        CusArea = cusArea;
        CusAddress = cusAddress;
        CusMobile = cusMobile;
        CusMobile1 = cusMobile1;
        this.POProductDetailClass = POProductDetailClass;
    }
}
