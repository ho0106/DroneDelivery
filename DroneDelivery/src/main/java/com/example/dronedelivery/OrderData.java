package com.example.dronedelivery;

public class OrderData {
    private String order_menu;
    private String order_address;
    private String order_request;
    private String order_postcode;

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

    public String getOrder_postcode() {
        return order_postcode;
    }

    public void setOrder_postcode(String order_postcode) {
        this.order_postcode = order_postcode;
    }
}