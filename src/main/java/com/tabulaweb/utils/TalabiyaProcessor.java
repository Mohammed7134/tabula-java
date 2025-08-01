package com.tabulaweb.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tabulaweb.model.BreifItem;
import com.tabulaweb.model.CatalogueItem;
import com.tabulaweb.model.Expiry;

public class TalabiyaProcessor {

    private static final Set<String> specialCodes = Set.of(
            "6505-99-02-06542", "6505-99-02-06543", "6505-99-02-06544",
            "6505-99-02-06547", "6505-99-02-06534", "6505-99-02-06531",
            "6505-99-02-05571", "6505-99-02-05572"
    );

    public static List<CatalogueItem> writeTalabiya(List<BreifItem> breifItems, List<Expiry> expiries) {
        // Load catalogue items from JSON
        // This assumes CatalogueLoader.loadCatalogueFromJson() returns a List<CatalogueItem>
        List<CatalogueItem> catalogueItems = CatalogueLoader.loadCatalogueFromJson();
        // Check if breifItems which is the items movement file is null or empty
        if (breifItems == null || breifItems.isEmpty()) {
            System.out.println("No movement data found.");
            return List.of();
        }
        // Check if catalogue items are loaded successfully
        if (catalogueItems == null || catalogueItems.isEmpty()) {
            System.out.println("No catalogue items found.");
            return List.of();
        }
        // Process each breif item
        for (BreifItem m : breifItems) {
            String code = m.getCode();
            String finalCode = formateCode(code);
            if (finalCode != null
                    && finalCode.matches("\\d{12,13}") // must be 12 or 13 digits
                    && finalCode.startsWith("6505")) {

                List<CatalogueItem> matchingItems;
                // Filter catalogue items based on ITEMNO
                matchingItems = catalogueItems.stream()
                        .filter(c -> {
                            if (c.getITEMNO() == null) {
                                return false; // skip null ITEMNO
                            }
                            // System.out.println("Checking item: " + c.getITEMNO() + " against code: " + finalCode);
                            String itemCode = c.getITEMNO();
                            return itemCode.contains(finalCode);
                        })
                        .sorted(Comparator.comparingDouble((CatalogueItem c) -> parseDoubleSafe(c.getPACK())).reversed())
                        .collect(Collectors.toList());
                if (matchingItems.isEmpty()) {
                    System.out.println("No match found for " + m.getName());
                    continue;
                }
                for (CatalogueItem item : matchingItems) {
                    double packSize = parseDoubleSafe(item.getPACK());
                    if (packSize <= 0) {
                        continue;
                    }
                    double difference = m.getDifference();
                    System.out.println("Processing item: " + item.getITEMNO() + ", difference: " + difference);
                    if (difference < 0) {
                        int total = (int) Math.round(Math.abs(difference) / packSize);
                        if (Boolean.TRUE.equals(item.getIGNORE())) {
                            item.setTOTAL("IGNORED");
                            continue;
                        }
                        if (m.isDone()) {
                            item.setTOTAL("DONE");
                            continue;
                        }
                        item.setEXPIRY(m.getExpiry());
                        LocalDate expiryDate = parseDate(item.getEXPIRY());
                        if (expiryDate != null && expiryDate.isAfter(LocalDate.now().plusMonths(4))) {
                            if (total >= 3) {
                                item.setTOTAL(String.valueOf(total));
                                System.out.println("total already set to " + total + " for item: " + item.getITEMNO());
                                m.setDone(true);
                            } else {
                                item.setTOTAL("UNFAVOURED");
                                m.setDone(true);
                            }
                        } else {
                            item.setTOTAL("near expiry");

                        }
                    } else {
                        item.setTOTAL("NO NEED");
                    }
                }
            } else {
                System.out.println("Invalid code format: " + code + " for item: " + m.getName());
            }
        }
        //loop through the catalogue items and if the total is zero search its code in expirieslist set the expiry date
        for (CatalogueItem item : catalogueItems) {
            if (item.getTOTAL() != null && item.getTOTAL().equals("0")) {
                String code = item.getITEMNO();
                if (code != null && !code.isEmpty()) {
                    Expiry expiry = expiries.stream()
                            .filter(e -> {
                                String finalCode = formateCode(e.getCode());
                                return code.contains(finalCode);
                            })
                            .findFirst()
                            .orElse(null);
                    if (expiry != null) {
                        item.setEXPIRY(UpdateExpiryDate.convertDateFormat(expiry.getDate()));
                        item.setTOTAL("DID NOT MOVE");

                    } else {
                        if (!item.getIGNORE()) {
                            System.out.println("No expiry found for code: " + code);
                            item.setTOTAL("N/A");
                        } else {
                            item.setTOTAL("IGNORED");
                        }

                    }
                } else {
                    System.out.println("Invalid ITEMNO for item: " + item.getITEMDESC());
                }
            }
        }
        return catalogueItems;
    }

    private static double parseDoubleSafe(String val) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private static LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private static String formateCode(String code) {
        if (specialCodes.contains(code)) {
            code = code.replaceFirst("6505-99", "6505-02").replace("-", "");
        } else {
            code = code.replaceFirst("6505-99", "6505-02").replace("-", "");
            if (code.length() > 12) {
                code = code.substring(0, 12);
            }
        }
        return code;
    }
}
