package com.example.voltapp.account.model;

public class PaymentMethod {
    public String name;
    public String status;
    public int logoRes;

    public PaymentMethod(String name, String status) {
        this(name, status, 0);
    }

    public PaymentMethod(String name, String status, int logoRes) {
        this.name = name;
        this.status = status;
        this.logoRes = logoRes;
    }
}
