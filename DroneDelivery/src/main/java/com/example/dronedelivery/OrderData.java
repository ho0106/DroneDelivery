package com.example.dronedelivery;

public class OrderData {
    private String order_id;
    private String order_menu;
    private String order_request;
    private String order_address;
    private String order_phoneNo;

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_menu() {
        return order_menu;
    }

    public void setOrder_menu(String order_menu) {
        this.order_menu = order_menu;
    }

    public String getOrder_address() {
        return order_address;
    }

    public void setOrder_address(String order_address) {
        this.order_address = order_address;
    }

    public String getOrder_request() {
        return order_request;
    }

    public void setOrder_request(String order_request) {
        this.order_request = order_request;
    }

    public String getOrder_phoneNo() {
        return order_phoneNo;
    }

    public void setOrder_phoneNo(String order_phoneNo) {
        this.order_phoneNo = order_phoneNo;
    }
}
