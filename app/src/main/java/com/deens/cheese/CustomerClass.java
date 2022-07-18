package com.deens.cheese;

public class CustomerClass {

    private int CustomerID;

    public CustomerClass(int customerID, String customerCode, String customerName, int cityID, String cityName, String area, String address, String mobile, String mobile1) {
        CustomerID = customerID;
        CustomerCode = customerCode;
        CustomerName = customerName;
        CityID = cityID;
        CityName = cityName;
        Area = area;
        Address = address;
        Mobile = mobile;
        Mobile1 = mobile1;
    }

    private String CustomerCode;

    private String CustomerName;

    private int CityID;

    private String CityName;

    private String Area;

    private String Address;

    private String Mobile;

    private String Mobile1;

    private String RegistrationDate;

    private boolean IsActive;

    private int GM;

    private String GMName;

    private int ST;

    private String STName;

    private int DM;

    private String DMName;

    private int SDM;

    private String SDMName;

    private int SO_ID;

    private String SO_Name;

    private String Visit_Day;

    private String CreditLimit;

    private String PaymentTypeID;

    private String PaymentTypeName;

    private boolean IsApproved;

    private String ApprovedBy;

    private String ApprovedOn;

    private String MapLocation;

    private Double Lat;

    private Double Long;

    private Boolean Is_Tax_Register;

    private String NTNNo;

    private String STNNo;

    public CustomerClass(int customerID, String customerName) {
        CustomerID = customerID;
        CustomerName = customerName;
    }


    public CustomerClass(int customerID, String customerCode, String customerName, int cityID, String cityName,
                         String area, String address, String mobile, String mobile1, String registrationDate,
                         boolean isActive, int GM, String GMName, int ST, String STName, int DM, String DMName,
                         int SDM, String SDMName, int SO_ID, String SO_Name, String visit_Day, String creditLimit,
                         String paymentTypeID, String paymentTypeName, boolean isApproved, String approvedBy,
                         String approvedOn, String mapLocation, Double lat, Double aLong) {
        CustomerID = customerID;
        CustomerCode = customerCode;
        CustomerName = customerName;
        CityID = cityID;
        CityName = cityName;
        Area = area;
        Address = address;
        Mobile = mobile;
        Mobile1 = mobile1;
        RegistrationDate = registrationDate;
        IsActive = isActive;
        this.GM = GM;
        this.GMName = GMName;
        this.ST = ST;
        this.STName = STName;
        this.DM = DM;
        this.DMName = DMName;
        this.SDM = SDM;
        this.SDMName = SDMName;
        this.SO_ID = SO_ID;
        this.SO_Name = SO_Name;
        Visit_Day = visit_Day;
        CreditLimit = creditLimit;
        PaymentTypeID = paymentTypeID;
        PaymentTypeName = paymentTypeName;
        IsApproved = isApproved;
        ApprovedBy = approvedBy;
        ApprovedOn = approvedOn;
        MapLocation = mapLocation;
        Lat = lat;
        Long = aLong;
    }

    public CustomerClass(int customerID, String customerCode, String customerName, int cityID, String cityName, String area, String address,
                         String mobile, String mobile1, String registrationDate, boolean isActive, int GM, String GMName, int ST,
                         String STName, int DM, String DMName, int SDM, String SDMName, int SO_ID, String SO_Name, String visit_Day,
                         String creditLimit, String paymentTypeID, String paymentTypeName, boolean isApproved, String approvedBy,
                         String approvedOn, String mapLocation, Double lat, Double aLong, Boolean is_Tax_Register, String NTNNo,
                         String STNNo) {
        CustomerID = customerID;
        CustomerCode = customerCode;
        CustomerName = customerName;
        CityID = cityID;
        CityName = cityName;
        Area = area;
        Address = address;
        Mobile = mobile;
        Mobile1 = mobile1;
        RegistrationDate = registrationDate;
        IsActive = isActive;
        this.GM = GM;
        this.GMName = GMName;
        this.ST = ST;
        this.STName = STName;
        this.DM = DM;
        this.DMName = DMName;
        this.SDM = SDM;
        this.SDMName = SDMName;
        this.SO_ID = SO_ID;
        this.SO_Name = SO_Name;
        Visit_Day = visit_Day;
        CreditLimit = creditLimit;
        PaymentTypeID = paymentTypeID;
        PaymentTypeName = paymentTypeName;
        IsApproved = isApproved;
        ApprovedBy = approvedBy;
        ApprovedOn = approvedOn;
        MapLocation = mapLocation;
        Lat = lat;
        Long = aLong;
        Is_Tax_Register = is_Tax_Register;
        this.NTNNo = NTNNo;
        this.STNNo = STNNo;
    }

    public int getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(int customerID) {
        CustomerID = customerID;
    }

    public String getCustomerCode() {
        return CustomerCode;
    }

    public void setCustomerCode(String customerCode) {
        CustomerCode = customerCode;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public int getCityID() {
        return CityID;
    }

    public void setCityID(int cityID) {
        CityID = cityID;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getMobile1() {
        return Mobile1;
    }

    public void setMobile1(String mobile1) {
        Mobile1 = mobile1;
    }

    public String getRegistrationDate() {
        return RegistrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        RegistrationDate = registrationDate;
    }

    public boolean isActive() {
        return IsActive;
    }

    public void setActive(boolean active) {
        IsActive = active;
    }

    public int getGM() {
        return GM;
    }

    public void setGM(int GM) {
        this.GM = GM;
    }

    public String getGMName() {
        return GMName;
    }

    public void setGMName(String GMName) {
        this.GMName = GMName;
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

    public int getDM() {
        return DM;
    }

    public void setDM(int DM) {
        this.DM = DM;
    }

    public String getDMName() {
        return DMName;
    }

    public void setDMName(String DMName) {
        this.DMName = DMName;
    }

    public int getSDM() {
        return SDM;
    }

    public void setSDM(int SDM) {
        this.SDM = SDM;
    }

    public String getSDMName() {
        return SDMName;
    }

    public void setSDMName(String SDMName) {
        this.SDMName = SDMName;
    }

    public int getSO_ID() {
        return SO_ID;
    }

    public void setSO_ID(int SO_ID) {
        this.SO_ID = SO_ID;
    }

    public String getSO_Name() {
        return SO_Name;
    }

    public void setSO_Name(String SO_Name) {
        this.SO_Name = SO_Name;
    }

    public String getVisit_Day() {
        return Visit_Day;
    }

    public void setVisit_Day(String visit_Day) {
        Visit_Day = visit_Day;
    }

    public String getCreditLimit() {
        return CreditLimit;
    }

    public void setCreditLimit(String creditLimit) {
        CreditLimit = creditLimit;
    }

    public String getPaymentTypeID() {
        return PaymentTypeID;
    }

    public void setPaymentTypeID(String paymentTypeID) {
        PaymentTypeID = paymentTypeID;
    }

    public String getPaymentTypeName() {
        return PaymentTypeName;
    }

    public void setPaymentTypeName(String paymentTypeName) {
        PaymentTypeName = paymentTypeName;
    }

    public boolean isApproved() {
        return IsApproved;
    }

    public void setApproved(boolean approved) {
        IsApproved = approved;
    }

    public String getApprovedBy() {
        return ApprovedBy;
    }

    public void setApprovedBy(String approvedBy) {
        ApprovedBy = approvedBy;
    }

    public String getApprovedOn() {
        return ApprovedOn;
    }

    public void setApprovedOn(String approvedOn) {
        ApprovedOn = approvedOn;
    }

    public String getMapLocation() {
        return MapLocation;
    }

    public void setMapLocation(String mapLocation) {
        MapLocation = mapLocation;
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLong() {
        return Long;
    }

    public Boolean getIs_Tax_Register() {
        return Is_Tax_Register;
    }

    public void setIs_Tax_Register(Boolean is_Tax_Register) {
        Is_Tax_Register = is_Tax_Register;
    }

    public String getNTNNo() {
        return NTNNo;
    }

    public void setNTNNo(String NTNNo) {
        this.NTNNo = NTNNo;
    }

    public String getSTNNo() {
        return STNNo;
    }

    public void setSTNNo(String STNNo) {
        this.STNNo = STNNo;
    }

    public void setLong(Double aLong) {
        Long = aLong;
    }
}
