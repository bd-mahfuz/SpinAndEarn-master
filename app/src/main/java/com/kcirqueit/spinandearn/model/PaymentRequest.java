package com.kcirqueit.spinandearn.model;

public class PaymentRequest {

    private String id;
    private String userId;
    private String date;
    private int withdrawPoint;
    private String mobileNo;
    private String email;
    private String name;
    private String accountType = "";
    private String paymentType;
    private String transactionStatus = "pending";

    public PaymentRequest() {
    }

    public PaymentRequest(String userId, String date, int withdrawPoint, String mobileNo, String accountType, String paymentType) {
        this.userId = userId;
        this.date = date;
        this.withdrawPoint = withdrawPoint;
        this.mobileNo = mobileNo;
        this.accountType = accountType;
        this.paymentType = paymentType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWithdrawPoint() {
        return withdrawPoint;
    }

    public void setWithdrawPoint(int withdrawPoint) {
        this.withdrawPoint = withdrawPoint;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


