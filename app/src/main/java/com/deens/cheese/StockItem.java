package com.deens.cheese;

public class StockItem {

    private int ProductID;

    private String CompanyName;

    private String ProductCode;

    private String ProductName;

    private Double ProductSalaPrice;

    private String ProductType;

    private String ProductUnit;

    private Double TradeOffer;

    private int quantity = 0;

    private int costPrice = 0;

    private String stockDate;

    private int issuedStock = 0;

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
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getCostPrice() {
        return costPrice;
    }
    public void setCostPrice(int costPrice) {
        this.costPrice = costPrice;
    }
    public String getStockDate() {
        return stockDate;
    }
    public void setStockDate(String stockDate) {
        this.stockDate = stockDate;
    }
    public int getIssuedStock() {
        return issuedStock;
    }
    public void setIssuedStock(int issuedStock) {
        this.issuedStock = issuedStock;
    }

    public StockItem(int productID, String companyName, String productCode, String productName,
                     Double productSalaPrice, String productType, String productUnit, Double tradeOffer) {
        ProductID = productID;
        CompanyName = companyName;
        ProductCode = productCode;
        ProductName = productName;
        ProductSalaPrice = productSalaPrice;
        ProductType = productType;
        ProductUnit = productUnit;
        TradeOffer = tradeOffer;
    }

    public StockItem(int productID, String companyName, String productCode, String productName,
                     Double productSalaPrice, String productType, String productUnit, Double tradeOffer, int quantity) {
        ProductID = productID;
        CompanyName = companyName;
        ProductCode = productCode;
        ProductName = productName;
        ProductSalaPrice = productSalaPrice;
        ProductType = productType;
        ProductUnit = productUnit;
        TradeOffer = tradeOffer;
        this.quantity = quantity;
    }

}
