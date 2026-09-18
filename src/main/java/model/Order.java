package model;

public class Order {

    private String orderId;
    private String customer;
    private String service;
    private int quantity;
    private String status;
    private double cost;

    public Order(String orderId,
                 String customer,
                 String service,
                 int quantity,
                 String status,
                 double cost) {

        this.orderId = orderId;
        this.customer = customer;
        this.service = service;
        this.quantity = quantity;
        this.status = status;
        this.cost = cost;
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

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public double getCost() {
        return cost;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}