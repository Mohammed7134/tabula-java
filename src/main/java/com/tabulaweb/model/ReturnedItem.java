package com.tabulaweb.model;  // or your preferred package

public class ReturnedItem {

    private String code;
    private double quantity;

    public ReturnedItem() {
    }

    public ReturnedItem(String code, double quantity) {
        this.code = code;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ReturnedItem{"
                + "code='" + code + '\''
                + ", quantity=" + quantity
                + '}';
    }
}
