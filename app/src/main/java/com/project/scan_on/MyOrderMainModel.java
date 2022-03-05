package com.project.scan_on;

import java.util.Date;

public class MyOrderMainModel implements Comparable<MyOrderMainModel> {

    private String deliveryPrice;
    private String orderStatus;
    private String paymentStatus;
    private long totalAmmount;
    private long savedAmmount;
    private long totalItem;
    private String orderId;
    private long totalItemPrice;
    private Date orderDate;
    private String PaymentMethod;


    public MyOrderMainModel(String deliveryPrice, String orderStatus, String paymentStatus, long totalAmmount, long savedAmmount, long totalItem, long totalItemPrice, Date orderDate, String orderId, String PaymentMethod) {
        this.deliveryPrice = deliveryPrice;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.totalAmmount = totalAmmount;
        this.savedAmmount = savedAmmount;
        this.totalItem = totalItem;
        this.totalItemPrice = totalItemPrice;
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.PaymentMethod = PaymentMethod;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(String deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public long getTotalAmmount() {
        return totalAmmount;
    }

    public void setTotalAmmount(long totalAmmount) {
        this.totalAmmount = totalAmmount;
    }

    public long getSavedAmmount() {
        return savedAmmount;
    }

    public void setSavedAmmount(long savedAmmount) {
        this.savedAmmount = savedAmmount;
    }

    public long getTotalItem() {
        return totalItem;
    }

    public void setTotalItem(long totalItem) {
        this.totalItem = totalItem;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getTotalItemPrice() {
        return totalItemPrice;
    }

    public void setTotalItemPrice(long totalItemPrice) {
        this.totalItemPrice = totalItemPrice;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    @Override
    public int compareTo(MyOrderMainModel o) {
        return getOrderDate().compareTo(o.getOrderDate());
    }
}
