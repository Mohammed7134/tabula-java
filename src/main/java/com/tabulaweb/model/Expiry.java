package com.tabulaweb.model;

public class Expiry {

    private final String code;
    private final String date;

    public Expiry(String code, String date) {
        this.code = code;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Expiry{code='" + code + "', date='" + date + "'}";
    }
}
