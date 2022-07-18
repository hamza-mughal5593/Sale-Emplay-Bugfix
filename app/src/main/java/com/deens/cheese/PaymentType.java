package com.deens.cheese;

public class PaymentType {

    String PaymentTypeID;
    String PaymentTypeName;

    public PaymentType(String paymentTypeID, String paymentTypeName) {
        PaymentTypeID = paymentTypeID;
        PaymentTypeName = paymentTypeName;
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
}
