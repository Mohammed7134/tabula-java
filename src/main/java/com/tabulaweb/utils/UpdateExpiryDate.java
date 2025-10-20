package com.tabulaweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tabulaweb.model.briefItem;
import com.tabulaweb.model.Expiry;

public class UpdateExpiryDate {

    private static final Map<String, String> monthMap = new HashMap<>();

    static {
        monthMap.put("JAN", "01");
        monthMap.put("FEB", "02");
        monthMap.put("MAR", "03");
        monthMap.put("APR", "04");
        monthMap.put("MAY", "05");
        monthMap.put("JUN", "06");
        monthMap.put("JUL", "07");
        monthMap.put("AUG", "08");
        monthMap.put("SEP", "09");
        monthMap.put("OCT", "10");
        monthMap.put("NOV", "11");
        monthMap.put("DEC", "12");
    }

    // Converts "01-JUL-26" to "01/07/2026"
    public static String convertDateFormat(String dateStr) {
        if (dateStr == null || !dateStr.matches("\\d{2}-[A-Z]{3}-\\d{2}")) {
            return dateStr; // return as-is if not matching
        }

        String[] parts = dateStr.split("-");
        String day = parts[0];
        String monthAbbrev = parts[1].toUpperCase();
        String yearShort = parts[2];

        String month = monthMap.getOrDefault(monthAbbrev, "01");
        int year = Integer.parseInt(yearShort);
        String fullYear = (year < 50) ? "20" + yearShort : "19" + yearShort;

        return day + "/" + month + "/" + fullYear;
    }

    // Updates expiry field in briefItems list based on matching Expiry codes
    public static List<briefItem> updateExpiryDates(List<briefItem> briefItems, List<Expiry> expiries) {
        for (briefItem item : briefItems) {
            String code = item.getCode();
            for (Expiry expiry : expiries) {
                if (expiry.getCode().equals(code)) {
                    String formattedDate = convertDateFormat(expiry.getDate());
                    item.setExpiry(formattedDate);
                    System.out.printf("Updated expiry for code %s: %s%n", code, formattedDate);
                    break;
                }
            }
        }

        return briefItems;
    }
}
