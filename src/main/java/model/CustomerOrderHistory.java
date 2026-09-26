package model;

public class CustomerOrderHistory {

    private String orderId;
    private String service;
    private int quantity;
    private double amount;
    private String date;
    private String status;

    public CustomerOrderHistory(String orderId,
                                String service,
                                int quantity,
                                double amount,
                                String date,
                                String status) {

        this.orderId = orderId;
        this.service = service;
        this.quantity = quantity;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}