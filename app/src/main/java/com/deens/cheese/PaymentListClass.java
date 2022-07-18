package com.deens.cheese;

public class PaymentListClass {

    private int PaymentID;

    private int CustomerID;

    private int LoginUserID;

    private String PaymentDate;

    private int Amount;

    private String PaymentType;

    private String PaymentMode;

    private String Remarks;

    private String ChequeNo;

    private String ChequeDate;

    private String BankName;

    public void setPaymentID(int PaymentID){
        this.PaymentID = PaymentID;
    }
    public int getPaymentID(){
        return this.PaymentID;
    }
    public void setCustomerID(int CustomerID){
        this.CustomerID = CustomerID;
    }
    public int getCustomerID(){
        return this.CustomerID;
    }
    public void setLoginUserID(int LoginUserID){
        this.LoginUserID = LoginUserID;
    }
    public int getLoginUserID(){
        return this.LoginUserID;
    }
    public void setPaymentDate(String PaymentDate){
        this.PaymentDate = PaymentDate;
    }
    public String getPaymentDate(){
        return this.PaymentDate;
    }
    public void setAmount(int Amount){
        this.Amount = Amount;
    }
    public int getAmount(){
        return this.Amount;
    }
    public void setPaymentType(String PaymentType){
        this.PaymentType = PaymentType;
    }
    public String getPaymentType(){
        return this.PaymentType;
    }
    public void setPaymentMode(String PaymentMode){
        this.PaymentMode = PaymentMode;
    }
    public String getPaymentMode(){
        return this.PaymentMode;
    }
    public void setRemarks(String Remarks){
        this.Remarks = Remarks;
    }
    public String getRemarks(){
        return this.Remarks;
    }
    public void setChequeNo(String ChequeNo){
        this.ChequeNo = ChequeNo;
    }
    public String getChequeNo(){
        return this.ChequeNo;
    }
    public void setChequeDate(String ChequeDate){
        this.ChequeDate = ChequeDate;
    }
    public String getChequeDate(){
        return this.ChequeDate;
    }
    public void setBankName(String BankName){
        this.BankName = BankName;
    }
    public String getBankName(){
        return this.BankName;
    }

    public PaymentListClass(int paymentID, int customerID, int loginUserID, String paymentDate, int amount, String paymentType, String paymentMode, String remarks,
                            String chequeNo, String chequeDate, String bankName) {
        PaymentID = paymentID;
        CustomerID = customerID;
        LoginUserID = loginUserID;
        PaymentDate = paymentDate;
        Amount = amount;
        PaymentType = paymentType;
        PaymentMode = paymentMode;
        Remarks = remarks;
        ChequeNo = chequeNo;
        ChequeDate = chequeDate;
        BankName = bankName;
    }
}
