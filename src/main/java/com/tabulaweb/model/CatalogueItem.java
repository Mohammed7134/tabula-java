package com.tabulaweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogueItem {

    @JsonProperty("ITEMNO")
    private String ITEMNO;
    @JsonProperty("PACK")
    private String PACK;
    @JsonProperty("EXPIRY")
    private String EXPIRY;
    @JsonProperty("TOTAL")
    private String TOTAL;
    @JsonProperty("IGNORE")
    private Boolean IGNORE;
    @JsonProperty("ITEMDESC")
    private String ITEMDESC;
    @JsonProperty("CARTON")
    private String CARTON;

    // --- extra field not in JSON ---
    private String NOTE;
    private double STOCK;
    private double MOVEMENT;
    // getters and setters
    public String getITEMNO() { return ITEMNO; }
    public void setITEMNO(String ITEMNO) { this.ITEMNO = ITEMNO; }

    public String getITEMDESC() { return ITEMDESC; }
    public void setITEMDESC(String ITEMDESC) { this.ITEMDESC = ITEMDESC; }

    public String getPACK() { return PACK; }
    public void setPACK(String PACK) { this.PACK = PACK; }

    public String getEXPIRY() { return EXPIRY; }
    public void setEXPIRY(String EXPIRY) { this.EXPIRY = EXPIRY; }

    public String getTOTAL() { return TOTAL; }
    public void setTOTAL(String TOTAL) { this.TOTAL = TOTAL; }

    public String getCARTON() { return CARTON; }
    public void setCARTON(String CARTON) { this.CARTON = CARTON; }

    public Boolean getIGNORE() { return IGNORE; }
    public void setIGNORE(Boolean IGNORE) { this.IGNORE = IGNORE; }

    // new field getter/setter
    public String getNOTE() { return NOTE; }
    public void setNOTE(String NOTE) { this.NOTE = NOTE; }

    public double getSTOCK() { return STOCK; }
    public void setSTOCK(double STOCK) { this.STOCK = STOCK; }

    public double getMOVEMENT() { return MOVEMENT; }
    public void setMOVEMENT(double MOVEMENT) { this.MOVEMENT = MOVEMENT; }

    @Override
    public String toString() {
        return "CatalogueItem{"
                + "ITEMNO='" + ITEMNO + '\''
                + ", PACK='" + PACK + '\''
                + ", EXPIRY='" + EXPIRY + '\''
                + ", TOTAL='" + TOTAL + '\''
                + ", IGNORE=" + IGNORE
                + ", ITEMDESC='" + ITEMDESC + '\''
                + ", NOTE='" + NOTE + '\''
                + '}';
    }
}
