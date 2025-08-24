package com.tabulaweb.model;

public class RequestedItem {

    private String code;
    private String name;
    private double hisQuantity;
    private double smsQuantity;

    public RequestedItem() {
    }

    public RequestedItem(String code, String name, double hisQuantity, double smsQuantity) {
        this.code = code;
        this.name = name;
        this.hisQuantity = hisQuantity;
        this.smsQuantity = smsQuantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getHisQuantity() {
        return hisQuantity;
    }

    public void setHisQuantity(double hisQuantity) {
        this.hisQuantity = hisQuantity;
    }

    public void setSmsQuantity(double smsQuantity) {
        this.smsQuantity = smsQuantity;
    }

    public double getSmsQuantity() {
        return smsQuantity;
    }


    @Override
    public String toString() {
        return "RequestedItem{"
                + "code='" + code + '\''
                + ", name=" + name + '\''
                + ", quantity=" + hisQuantity
                + '}';
    }


}
