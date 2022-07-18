package com.deens.cheese;

public class CartonClass {

    private int ProductID;

    private String ProductName;

    private int QuantityCtn;

    private int QuantityPkt;

    private int PickedQuantityCtn = 0;

    private int PickedQuantityPkt = 0;

    public CartonClass(int productID, String productName, int quantityCtn, int quantityPkt) {
        ProductID = productID;
        ProductName = productName;
        QuantityCtn = quantityCtn;
        QuantityPkt = quantityPkt;
    }

    public int getProductID() {
        return ProductID;
    }

    public void setProductID(int productID) {
        ProductID = productID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public int getQuantityCtn() {
        return QuantityCtn;
    }

    public void setQuantityCtn(int quantityCtn) {
        QuantityCtn = quantityCtn;
    }

    public int getQuantityPkt() {
        return QuantityPkt;
    }

    public void setQuantityPkt(int quantityPkt) {
        QuantityPkt = quantityPkt;
    }

    public int getPickedQuantityCtn() {
        return PickedQuantityCtn;
    }

    public void setPickedQuantityCtn(int pickedQuantityCtn) {
        PickedQuantityCtn = pickedQuantityCtn;
    }

    public int getPickedQuantityPkt() {
        return PickedQuantityPkt;
    }

    public void setPickedQuantityPkt(int pickedQuantityPkt) {
        PickedQuantityPkt = pickedQuantityPkt;
    }
}
