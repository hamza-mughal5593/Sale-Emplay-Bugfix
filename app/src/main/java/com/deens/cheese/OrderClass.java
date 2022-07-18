package com.deens.cheese;

import java.util.ArrayList;

public class OrderClass {

    private int INV_InvoiceID;

    private String INV_InvoiceNo;

    private String INV_InvoiceDate;

    private int INV_SubTotal;

    private int INV_Discount_Rate;

    private int INV_DiscountAmount;

    private int INV_GrandTotal;

    private int INV_PaymentReceived;

    private int INV_PaymentDue;

    private String INV_MapLocation;

    private String INV_Lat;

    private String INV_Long;

    private int CusCustomerID;

    private String CusCustomerCode;

    private String CusCustomerName;

    private int CusCityID;

    private String CusCityName;

    private String CusArea;

    private String CusAddress;

    private String CusMobile;

    private String CusMobile1;

    private String CusRegistrationDate;

    private boolean CusIsActive;

    private String CusVisit_Day;

    private String CusCreditLimit;

    private String CusPaymentTypeName;

    private boolean CusIsApproved;

    private String CusApprovedOn;

    private String CusMapLocation;

    private String CusLat;

    private String CusLong;

    private int SO_GM;

    private String SO_GMName;

    private int SO_ST;

    private String SO_STName;

    private int SO_DM;

    private String SO_DMName;

    private int SO_SDM;

    private String SO_SDMName;

    private int SO_ID;

    private String SO_Name;

    private int order;

    private Double INV_TaxRate;

    private Double INV_TaxAmount;

    public Double getINV_TaxRate() {
        return INV_TaxRate;
    }

    public void setINV_TaxRate(Double INV_TaxRate) {
        this.INV_TaxRate = INV_TaxRate;
    }

    public Double getINV_TaxAmount() {
        return INV_TaxAmount;
    }

    public void setINV_TaxAmount(Double INV_TaxAmount) {
        this.INV_TaxAmount = INV_TaxAmount;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    private ArrayList<ProductDetailClass> productsDetails = new ArrayList<>();

    public void setINV_InvoiceID(int INV_InvoiceID) {
        this.INV_InvoiceID = INV_InvoiceID;
    }

    public int getINV_InvoiceID() {
        return this.INV_InvoiceID;
    }

    public void setINV_InvoiceNo(String INV_InvoiceNo) {
        this.INV_InvoiceNo = INV_InvoiceNo;
    }

    public String getINV_InvoiceNo() {
        return this.INV_InvoiceNo;
    }

    public void setINV_InvoiceDate(String INV_InvoiceDate) {
        this.INV_InvoiceDate = INV_InvoiceDate;
    }

    public String getINV_InvoiceDate() {
        return this.INV_InvoiceDate;
    }

    public void setINV_SubTotal(int INV_SubTotal) {
        this.INV_SubTotal = INV_SubTotal;
    }

    public int getINV_SubTotal() {
        return this.INV_SubTotal;
    }

    public void setINV_Discount_Rate(int INV_Discount_Rate) {
        this.INV_Discount_Rate = INV_Discount_Rate;
    }

    public int getINV_Discount_Rate() {
        return this.INV_Discount_Rate;
    }

    public void setINV_DiscountAmount(int INV_DiscountAmount) {
        this.INV_DiscountAmount = INV_DiscountAmount;
    }

    public int getINV_DiscountAmount() {
        return this.INV_DiscountAmount;
    }

    public void setINV_GrandTotal(int INV_GrandTotal) {
        this.INV_GrandTotal = INV_GrandTotal;
    }

    public int getINV_GrandTotal() {
        return this.INV_GrandTotal;
    }

    public void setINV_PaymentReceived(int INV_PaymentReceived) {
        this.INV_PaymentReceived = INV_PaymentReceived;
    }

    public int getINV_PaymentReceived() {
        return this.INV_PaymentReceived;
    }

    public void setINV_PaymentDue(int INV_PaymentDue) {
        this.INV_PaymentDue = INV_PaymentDue;
    }

    public int getINV_PaymentDue() {
        return this.INV_PaymentDue;
    }

    public void setINV_MapLocation(String INV_MapLocation) {
        this.INV_MapLocation = INV_MapLocation;
    }

    public String getINV_MapLocation() {
        return this.INV_MapLocation;
    }

    public void setINV_Lat(String INV_Lat) {
        this.INV_Lat = INV_Lat;
    }

    public String getINV_Lat() {
        return this.INV_Lat;
    }

    public void setINV_Long(String INV_Long) {
        this.INV_Long = INV_Long;
    }

    public String getINV_Long() {
        return this.INV_Long;
    }

    public void setCusCustomerID(int CusCustomerID) {
        this.CusCustomerID = CusCustomerID;
    }

    public int getCusCustomerID() {
        return this.CusCustomerID;
    }

    public void setCusCustomerCode(String CusCustomerCode) {
        this.CusCustomerCode = CusCustomerCode;
    }

    public String getCusCustomerCode() {
        return this.CusCustomerCode;
    }

    public void setCusCustomerName(String CusCustomerName) {
        this.CusCustomerName = CusCustomerName;
    }

    public String getCusCustomerName() {
        return this.CusCustomerName;
    }

    public void setCusCityID(int CusCityID) {
        this.CusCityID = CusCityID;
    }

    public int getCusCityID() {
        return this.CusCityID;
    }

    public void setCusCityName(String CusCityName) {
        this.CusCityName = CusCityName;
    }

    public String getCusCityName() {
        return this.CusCityName;
    }

    public void setCusArea(String CusArea) {
        this.CusArea = CusArea;
    }

    public String getCusArea() {
        return this.CusArea;
    }

    public void setCusAddress(String CusAddress) {
        this.CusAddress = CusAddress;
    }

    public String getCusAddress() {
        return this.CusAddress;
    }

    public void setCusMobile(String CusMobile) {
        this.CusMobile = CusMobile;
    }

    public String getCusMobile() {
        return this.CusMobile;
    }

    public void setCusMobile1(String CusMobile1) {
        this.CusMobile1 = CusMobile1;
    }

    public String getCusMobile1() {
        return this.CusMobile1;
    }

    public void setCusRegistrationDate(String CusRegistrationDate) {
        this.CusRegistrationDate = CusRegistrationDate;
    }

    public String getCusRegistrationDate() {
        return this.CusRegistrationDate;
    }

    public void setCusIsActive(boolean CusIsActive) {
        this.CusIsActive = CusIsActive;
    }

    public boolean getCusIsActive() {
        return this.CusIsActive;
    }

    public void setCusVisit_Day(String CusVisit_Day) {
        this.CusVisit_Day = CusVisit_Day;
    }

    public String getCusVisit_Day() {
        return this.CusVisit_Day;
    }

    public void setCusCreditLimit(String CusCreditLimit) {
        this.CusCreditLimit = CusCreditLimit;
    }

    public String getCusCreditLimit() {
        return this.CusCreditLimit;
    }

    public void setCusPaymentTypeName(String CusPaymentTypeName) {
        this.CusPaymentTypeName = CusPaymentTypeName;
    }

    public String getCusPaymentTypeName() {
        return this.CusPaymentTypeName;
    }

    public void setCusIsApproved(boolean CusIsApproved) {
        this.CusIsApproved = CusIsApproved;
    }

    public boolean getCusIsApproved() {
        return this.CusIsApproved;
    }

    public void setCusApprovedOn(String CusApprovedOn) {
        this.CusApprovedOn = CusApprovedOn;
    }

    public String getCusApprovedOn() {
        return this.CusApprovedOn;
    }

    public void setCusMapLocation(String CusMapLocation) {
        this.CusMapLocation = CusMapLocation;
    }

    public String getCusMapLocation() {
        return this.CusMapLocation;
    }

    public void setCusLat(String CusLat) {
        this.CusLat = CusLat;
    }

    public String getCusLat() {
        return this.CusLat;
    }

    public void setCusLong(String CusLong) {
        this.CusLong = CusLong;
    }

    public String getCusLong() {
        return this.CusLong;
    }

    public void setSO_GM(int SO_GM) {
        this.SO_GM = SO_GM;
    }

    public int getSO_GM() {
        return this.SO_GM;
    }

    public void setSO_GMName(String SO_GMName) {
        this.SO_GMName = SO_GMName;
    }

    public String getSO_GMName() {
        return this.SO_GMName;
    }

    public void setSO_ST(int SO_ST) {
        this.SO_ST = SO_ST;
    }

    public int getSO_ST() {
        return this.SO_ST;
    }

    public void setSO_STName(String SO_STName) {
        this.SO_STName = SO_STName;
    }

    public String getSO_STName() {
        return this.SO_STName;
    }

    public void setSO_DM(int SO_DM) {
        this.SO_DM = SO_DM;
    }

    public int getSO_DM() {
        return this.SO_DM;
    }

    public void setSO_DMName(String SO_DMName) {
        this.SO_DMName = SO_DMName;
    }

    public String getSO_DMName() {
        return this.SO_DMName;
    }

    public void setSO_SDM(int SO_SDM) {
        this.SO_SDM = SO_SDM;
    }

    public int getSO_SDM() {
        return this.SO_SDM;
    }

    public void setSO_SDMName(String SO_SDMName) {
        this.SO_SDMName = SO_SDMName;
    }

    public String getSO_SDMName() {
        return this.SO_SDMName;
    }

    public void setSO_ID(int SO_ID) {
        this.SO_ID = SO_ID;
    }

    public int getSO_ID() {
        return this.SO_ID;
    }

    public void setSO_Name(String SO_Name) {
        this.SO_Name = SO_Name;
    }

    public String getSO_Name() {
        return this.SO_Name;
    }

    public ArrayList<ProductDetailClass> getProductsDetails() {
        return productsDetails;
    }

    public void setProductsDetails(ArrayList<ProductDetailClass> productsDetails) {
        this.productsDetails = productsDetails;
    }

    public OrderClass(int INV_InvoiceID, String INV_InvoiceNo, String INV_InvoiceDate, int INV_SubTotal,
                      int INV_Discount_Rate, int INV_DiscountAmount, int INV_GrandTotal, int INV_PaymentReceived,
                      int INV_PaymentDue, String INV_MapLocation, String INV_Lat, String INV_Long, int cusCustomerID,
                      String cusCustomerCode, String cusCustomerName, int cusCityID, String cusCityName, String cusArea,
                      String cusAddress, String cusMobile, String cusMobile1, String cusRegistrationDate,
                      boolean cusIsActive, String cusVisit_Day, String cusCreditLimit, String cusPaymentTypeName,
                      boolean cusIsApproved, String cusApprovedOn, String cusMapLocation, String cusLat,
                      String cusLong, int SO_GM, String SO_GMName, int SO_ST, String SO_STName, int SO_DM,
                      String SO_DMName, int SO_SDM, String SO_SDMName, int SO_ID, String SO_Name, Double INV_TaxRate, Double INV_TaxAmount,
                      ArrayList<ProductDetailClass> productsDetails) {
        this.INV_InvoiceID = INV_InvoiceID;
        this.INV_InvoiceNo = INV_InvoiceNo;
        this.INV_InvoiceDate = INV_InvoiceDate;
        this.INV_SubTotal = INV_SubTotal;
        this.INV_Discount_Rate = INV_Discount_Rate;
        this.INV_DiscountAmount = INV_DiscountAmount;
        this.INV_GrandTotal = INV_GrandTotal;
        this.INV_PaymentReceived = INV_PaymentReceived;
        this.INV_PaymentDue = INV_PaymentDue;
        this.INV_MapLocation = INV_MapLocation;
        this.INV_Lat = INV_Lat;
        this.INV_Long = INV_Long;
        CusCustomerID = cusCustomerID;
        CusCustomerCode = cusCustomerCode;
        CusCustomerName = cusCustomerName;
        CusCityID = cusCityID;
        CusCityName = cusCityName;
        CusArea = cusArea;
        CusAddress = cusAddress;
        CusMobile = cusMobile;
        CusMobile1 = cusMobile1;
        CusRegistrationDate = cusRegistrationDate;
        CusIsActive = cusIsActive;
        CusVisit_Day = cusVisit_Day;
        CusCreditLimit = cusCreditLimit;
        CusPaymentTypeName = cusPaymentTypeName;
        CusIsApproved = cusIsApproved;
        CusApprovedOn = cusApprovedOn;
        CusMapLocation = cusMapLocation;
        CusLat = cusLat;
        CusLong = cusLong;
        this.SO_GM = SO_GM;
        this.SO_GMName = SO_GMName;
        this.SO_ST = SO_ST;
        this.SO_STName = SO_STName;
        this.SO_DM = SO_DM;
        this.SO_DMName = SO_DMName;
        this.SO_SDM = SO_SDM;
        this.SO_SDMName = SO_SDMName;
        this.SO_ID = SO_ID;
        this.SO_Name = SO_Name;
        this.INV_TaxRate = INV_TaxRate;
        this.INV_TaxAmount = INV_TaxAmount;
        this.productsDetails = productsDetails;
    }
}


