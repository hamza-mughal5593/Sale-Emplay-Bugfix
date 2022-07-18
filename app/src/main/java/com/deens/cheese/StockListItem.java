package com.deens.cheese;

public class StockListItem {

    private int StockId;

    private String ProductName;

    private int ProductId;

    private int ProductCost;

    private int SalePrice;

    private int Quantity;

    private String StockDate;

    private String StockType;

    public void setStockId(int StockId){
        this.StockId = StockId;
    }
    public int getStockId(){
        return this.StockId;
    }
    public void setProductName(String ProductName){
        this.ProductName = ProductName;
    }
    public String getProductName(){
        return this.ProductName;
    }
    public void setProductId(int ProductId){
        this.ProductId = ProductId;
    }
    public int getProductId(){
        return this.ProductId;
    }
    public void setProductCost(int ProductCost){
        this.ProductCost = ProductCost;
    }
    public int getProductCost(){
        return this.ProductCost;
    }
    public void setSalePrice(int SalePrice){
        this.SalePrice = SalePrice;
    }
    public int getSalePrice(){
        return this.SalePrice;
    }
    public void setQuantity(int Quantity){
        this.Quantity = Quantity;
    }
    public int getQuantity(){
        return this.Quantity;
    }
    public void setStockDate(String StockDate){
        this.StockDate = StockDate;
    }
    public String getStockDate(){
        return this.StockDate;
    }
    public void setStockType(String StockType){
        this.StockType = StockType;
    }
    public String getStockType(){
        return this.StockType;
    }

    public StockListItem(int stockId, String productName, int productId, int productCost, int salePrice, int quantity,
                         String stockDate, String stockType) {
        StockId = stockId;
        ProductName = productName;
        ProductId = productId;
        ProductCost = productCost;
        SalePrice = salePrice;
        Quantity = quantity;
        StockDate = stockDate;
        StockType = stockType;
    }
}
