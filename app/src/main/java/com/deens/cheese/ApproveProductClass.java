package com.deens.cheese;

public class ApproveProductClass {

    private int ProductID;

    private String CompanyName;

    private String ProductCode;

    private String ProductName;

    private Double ProductSalaPrice;

    private String ProductType;

    private String ProductUnit;

    private Double TradeOffer;

    private Double Discount;

    public Double getDiscount() {
        return Discount;
    }
    public void setDiscount(Double discount) {
        Discount = discount;
    }
    public void setProductID(int ProductID){
        this.ProductID = ProductID;
    }
    public int getProductID(){
        return this.ProductID;
    }
    public void setCompanyName(String CompanyName){
        this.CompanyName = CompanyName;
    }
    public String getCompanyName(){
        return this.CompanyName;
    }
    public void setProductCode(String ProductCode){
        this.ProductCode = ProductCode;
    }
    public String getProductCode(){
        return this.ProductCode;
    }
    public void setProductName(String ProductName){
        this.ProductName = ProductName;
    }
    public String getProductName(){
        return this.ProductName;
    }
    public void setProductSalaPrice(Double ProductSalaPrice){
        this.ProductSalaPrice = ProductSalaPrice;
    }
    public Double getProductSalaPrice(){
        return this.ProductSalaPrice;
    }
    public void setProductType(String ProductType){
        this.ProductType = ProductType;
    }
    public String getProductType(){
        return this.ProductType;
    }
    public void setProductUnit(String ProductUnit){
        this.ProductUnit = ProductUnit;
    }
    public String getProductUnit(){
        return this.ProductUnit;
    }
    public void setTradeOffer(Double TradeOffer){
        this.TradeOffer = TradeOffer;
    }
    public Double getTradeOffer(){
        return this.TradeOffer;
    }


    public ApproveProductClass(int productID, String companyName, String productCode, String productName,
                               Double productSalaPrice, String productType, String productUnit, Double tradeOffer,
                               Double discount) {
        ProductID = productID;
        CompanyName = companyName;
        ProductCode = productCode;
        ProductName = productName;
        ProductSalaPrice = productSalaPrice;
        ProductType = productType;
        ProductUnit = productUnit;
        TradeOffer = tradeOffer;
        Discount = discount;
    }
}
