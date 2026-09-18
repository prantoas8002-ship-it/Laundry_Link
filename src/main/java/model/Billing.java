package model;

public class Billing {

    private int billId;
    private String orderId;
    private String customer;
    private String service;
    private double amount;
    private String date;
    private String status;

    public Billing(int billId, String orderId, String customer,
                   String service, double amount,
                   String date, String status) {

        this.billId = billId;
        this.orderId = orderId;
        this.customer = customer;
        this.service = service;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public int getBillId() {
        return billId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomer() {
        return customer;
    }

    public String getService() {
        return service;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}