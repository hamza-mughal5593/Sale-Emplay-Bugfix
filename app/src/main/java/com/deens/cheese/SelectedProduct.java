package com.deens.cheese;

public class SelectedProduct {

    int productID;
    Double salePrice;
    int quantity;
    Double tradeOffer;
    Double offerAmount;
    Double totalAmount;
    String name;

    public SelectedProduct(int productID, Double salePrice, int quantity, Double tradeOffer, double offerAmount, Double totalAmount, String name) {
        this.productID = productID;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.tradeOffer = tradeOffer;
        this.offerAmount = offerAmount;
        this.totalAmount = totalAmount;
        this.name = name;
    }


    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getTradeOffer() {
        return tradeOffer;
    }

    public void setTradeOffer(Double tradeOffer) {
        this.tradeOffer = tradeOffer;
    }

    public Double getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(Double offerAmount) {
        this.offerAmount = offerAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
