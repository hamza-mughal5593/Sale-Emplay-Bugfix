package com.deens.cheese;

public class Cart_list_item {
    String salePrice, quantity, name, tradeOffer, offerAmount, total;

    public Cart_list_item(String salePrice, String quantity, String name, String tradeOffer, String offerAmount, String total) {
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.name = name;
        this.tradeOffer = tradeOffer;
        this.offerAmount = offerAmount;
        this.total = total;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTradeOffer() {
        return tradeOffer;
    }

    public void setTradeOffer(String tradeOffer) {
        this.tradeOffer = tradeOffer;
    }

    public String getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(String offerAmount) {
        this.offerAmount = offerAmount;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
