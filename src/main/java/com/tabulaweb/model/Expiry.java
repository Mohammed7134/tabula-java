package com.tabulaweb.model;

public class Expiry {

    private final String code;
    private final String date;
    // optional quantity field, default to null if not provided

    private final double Quantity;

    public Expiry(String code, String date, double Quantity) {
        this.code = code;
        this.date = date;
        this.Quantity = Quantity;
    }

    public Expiry(String code, String date) {
        this(code, date, 0.0);
    }

    public String getCode() {
        return code;
    }

    public String getDate() {
        return date;
    }
    public double getQuantity() {
        return Quantity;
    }
    @Override
    public String toString() {
        return "Expiry{code='" + code + "', date='" + date + "', Quantity=" + Quantity + "}";
    }
}
