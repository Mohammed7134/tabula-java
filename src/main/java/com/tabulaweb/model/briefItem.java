package com.tabulaweb.model;

public class briefItem {

    private String code;
    private String name;
    private double currentStock;
    private double totalOut;
    private double difference;
    private String expiry;  // nearest expiry date as String (e.g., "yyyy-MM-dd")
    private boolean done;  // indicates if the item is marked as done

    public briefItem() {
    }

    public briefItem(String code, String name, double currentStock, double totalOut, double difference, String expiry) {
        this.code = code;
        this.name = name;
        this.currentStock = currentStock;
        this.totalOut = totalOut;
        this.difference = difference;
        this.expiry = expiry;
        this.done = false;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(double currentStock) {
        this.currentStock = currentStock;
    }

    public double getTotalOut() {
        return totalOut;
    }

    public void setTotalOut(double totalOut) {
        this.totalOut = totalOut;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        return "briefItem{"
                + "code='" + code + '\''
                + ", name='" + name + '\''
                + ", currentStock=" + currentStock
                + ", totalOut=" + totalOut
                + ", difference=" + difference
                + ", expiry='" + expiry + '\''
                + ", done='" + done + '\''
                + '}';
    }
}
