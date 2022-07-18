package com.deens.cheese;

public class ProductDetailClass {

    private int Id;

    private int ProductID;

    private String ProductName;

    private int SalePrice;

    private int Quantity;

    private int TradeOffer;

    private int Offer_Amt;

    private int TotalAmount;

    private int DiscountAmount;

    private int TotalDiscount;

    public void setId(int Id){
        this.Id = Id;
    }
    public int getId(){
        return this.Id;
    }
    public void setProductID(int ProductID){
        this.ProductID = ProductID;
    }
    public int getProductID(){
        return this.ProductID;
    }
    public void setProductName(String ProductName){
        this.ProductName = ProductName;
    }
    public String getProductName(){
        return this.ProductName;
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
    public void setTradeOffer(int TradeOffer){
        this.TradeOffer = TradeOffer;
    }
    public int getTradeOffer(){
        return this.TradeOffer;
    }
    public void setOffer_Amt(int Offer_Amt){
        this.Offer_Amt = Offer_Amt;
    }
    public int getOffer_Amt(){
        return this.Offer_Amt;
    }
    public void setTotalAmount(int TotalAmount){
        this.TotalAmount = TotalAmount;
    }
    public int getTotalAmount(){
        return this.TotalAmount;
    }
    public int getDiscountAmount() {
        return DiscountAmount;
    }
    public void setDiscountAmount(int discountAmount) {
        DiscountAmount = discountAmount;
    }
    public int getTotalDiscount() {
        return TotalDiscount;
    }
    public void setTotalDiscount(int totalDiscount) {
        TotalDiscount = totalDiscount;
    }

    public ProductDetailClass(int id, int productID, String productName, int salePrice, int quantity,
                              int tradeOffer, int offer_Amt, int totalAmount, int discountAmount, int totalDiscount) {
        Id = id;
        ProductID = productID;
        ProductName = productName;
        SalePrice = salePrice;
        Quantity = quantity;
        TradeOffer = tradeOffer;
        Offer_Amt = offer_Amt;
        TotalAmount = totalAmount;
        DiscountAmount = discountAmount;
        TotalDiscount = totalDiscount;
    }
}
