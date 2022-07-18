package com.deens.cheese;

import java.util.ArrayList;

public class CustomerProductClass {

    private int CustomerID;

    private String CustomerCode;

    private String CustomerName;

    private int CityID;

    private String CityName;

    private String Area;

    private String Address;

    private String Mobile;

    private int SO_ID;

    private String SO_Name;

    private Double CreditLimit;

    private int PaymentTypeID;

    private String PaymentTypeName;

    private ArrayList<ApproveProductClass> ProductDetails = new ArrayList<>();

    public void setCustomerID(int CustomerID){
        this.CustomerID = CustomerID;
    }
    public int getCustomerID(){
        return this.CustomerID;
    }
    public void setCustomerCode(String CustomerCode){
        this.CustomerCode = CustomerCode;
    }
    public String getCustomerCode(){
        return this.CustomerCode;
    }
    public void setCustomerName(String CustomerName){
        this.CustomerName = CustomerName;
    }
    public String getCustomerName(){
        return this.CustomerName;
    }
    public void setCityID(int CityID){
        this.CityID = CityID;
    }
    public int getCityID(){
        return this.CityID;
    }
    public void setCityName(String CityName){
        this.CityName = CityName;
    }
    public String getCityName(){
        return this.CityName;
    }
    public void setArea(String Area){
        this.Area = Area;
    }
    public String getArea(){
        return this.Area;
    }
    public void setAddress(String Address){
        this.Address = Address;
    }
    public String getAddress(){
        return this.Address;
    }
    public void setMobile(String Mobile){
        this.Mobile = Mobile;
    }
    public String getMobile(){
        return this.Mobile;
    }
    public void setSO_ID(int SO_ID){
        this.SO_ID = SO_ID;
    }
    public int getSO_ID(){
        return this.SO_ID;
    }
    public void setSO_Name(String SO_Name){
        this.SO_Name = SO_Name;
    }
    public String getSO_Name(){
        return this.SO_Name;
    }
    public void setCreditLimit(Double CreditLimit){
        this.CreditLimit = CreditLimit;
    }
    public Double getCreditLimit(){
        return this.CreditLimit;
    }
    public void setPaymentTypeID(int PaymentTypeID){
        this.PaymentTypeID = PaymentTypeID;
    }
    public int getPaymentTypeID(){
        return this.PaymentTypeID;
    }
    public void setPaymentTypeName(String PaymentTypeName){
        this.PaymentTypeName = PaymentTypeName;
    }
    public String getPaymentTypeName(){
        return this.PaymentTypeName;
    }
    public void setProductDetails(ArrayList<ApproveProductClass> ProducDetails){
        this.ProductDetails = ProducDetails;
    }
    public ArrayList<ApproveProductClass> getProductDetails(){
        return this.ProductDetails;
    }

    public CustomerProductClass(int customerID, String customerCode, String customerName, int cityID, String cityName,
                                String area,
                                String address, String mobile, int SO_ID, String SO_Name, Double creditLimit, int paymentTypeID,
                                String paymentTypeName, ArrayList<ApproveProductClass> productDetails) {
        CustomerID = customerID;
        CustomerCode = customerCode;
        CustomerName = customerName;
        CityID = cityID;
        CityName = cityName;
        Area = area;
        Address = address;
        Mobile = mobile;
        this.SO_ID = SO_ID;
        this.SO_Name = SO_Name;
        CreditLimit = creditLimit;
        PaymentTypeID = paymentTypeID;
        PaymentTypeName = paymentTypeName;
        ProductDetails = productDetails;
    }
}


