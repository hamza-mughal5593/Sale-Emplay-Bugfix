package com.deens.cheese;

public class POProductDetailClass {

    private int ID;

    private int POID;

    private int ProductID;

    private String ProductName;

    private int Quantity;

    private int TotalAmount;

    private int SalePrice;

    public int getSalePrice() {
        return SalePrice;
    }

    public void setSalePrice(int salePrice) {
        SalePrice = salePrice;
    }

    //
    public int getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        TotalAmount = totalAmount;
    }

    public void setID(int ID){
        this.ID = ID;
    }
    public int getID(){
        return this.ID;
    }
    public void setPOID(int POID){
        this.POID = POID;
    }
    public int getPOID(){
        return this.POID;
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
    public void setQuantity(int Quantity){
        this.Quantity = Quantity;
    }
    public int getQuantity(){
        return this.Quantity;
    }

    public POProductDetailClass(int ID, int POID, int productID, String productName, int quantity, int totalAmount, int salePrice) {
        this.ID = ID;
        this.POID = POID;
        ProductID = productID;
        ProductName = productName;
        Quantity = quantity;
        TotalAmount = totalAmount;
        SalePrice = salePrice;
    }
}
