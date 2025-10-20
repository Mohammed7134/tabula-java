package com.tabulaweb.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tabulaweb.model.briefItem;
import com.tabulaweb.model.CatalogueItem;
import com.tabulaweb.model.Expiry;
import com.tabulaweb.model.RequestedItem;

public class TalabiyaChecker {

    private static final Set<String> specialCodes = Set.of(
            "6505-99-02-06542", "6505-99-02-06543", "6505-99-02-06546",
            "6505-99-02-06547", "6505-99-02-06534", "6505-99-02-06531",
            "6505-99-02-05571", "6505-99-02-05572", "6505-99-02-05591",
            "6505-99-02-06545", "6505-99-02-08062", "6505-99-02-08065"
    );
    private static final Set<String> specialCodesCatalogue = Set.of(
        "6505020206542", "6505020206543", "6505020206546",
        "6505020206547", "6505020206534", "6505020206531",
        "6505020205571", "6505020205572", "6505020205591",
        "6505020206545", "6505020208062", "6505020208065"
    );
        public static List<CatalogueItem> checkTalabiya(List<briefItem> briefItems, List<Expiry> expiries, List<RequestedItem> requestedItems) {
            // Load catalogue items from JSON
            // This assumes CatalogueLoader.loadCatalogueFromJson() returns a List<CatalogueItem>
            List<CatalogueItem> catalogueItems = CatalogueLoader.loadCatalogueFromJson();
            // Check if briefItems which is the items movement file is null or empty
            if (briefItems == null || briefItems.isEmpty()) {
                System.out.println("No movement data found.");
                return List.of();
            }
            // Check if catalogue items are loaded successfully
            if (catalogueItems == null || catalogueItems.isEmpty()) {
                System.out.println("No catalogue items found.");
                return List.of();
            }
            // Process each brief item
            for (briefItem m : briefItems) {
                String code = m.getCode();
                String finalCode = formatCode(code);
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
                        item.setEXPIRY(m.getExpiry());
                        LocalDate expiryDate = parseDate(item.getEXPIRY());
                        double difference = m.getDifference();
                        double stock = m.getCurrentStock();
                        double out = m.getTotalOut();
                        item.setSTOCK(stock);
                        item.setMOVEMENT(out);
                        // System.out.println("Processing item: " + item.getITEMNO() + ", difference: " + difference);
                        if (difference < 0) {
                            int total = (int) Math.round(Math.abs(difference) / packSize);
                            if (Boolean.TRUE.equals(item.getIGNORE())) {
                                item.setTOTAL("0");
                                item.setNOTE("[IG]");
                                continue;
                            }
                            if (m.isDone()) {
                                item.setTOTAL("0");
                                item.setNOTE("[DN]");
                                continue;
                            }
                            if (expiryDate != null && expiryDate.isAfter(LocalDate.now().plusMonths(6))) {
                                if (total >= 3) {
                                    item.setTOTAL(String.valueOf(total));
                                    System.out.println("total already set to " + total + " for item: " + item.getITEMNO());
                                    m.setDone(true);
                                } else {
                                    item.setTOTAL("0");
                                    item.setNOTE("[UF]");

                                }
                            } else {
                                item.setTOTAL(String.valueOf(total));
                                item.setNOTE("[NE]");
                            }
                        } else {
                            item.setTOTAL("0");
                            item.setNOTE("[NN]");

                        }
                    }
                } else {
                    System.out.println("Invalid code format: " + code + " for item: " + m.getName());
                }
            }
            //loop through the catalogue items and if the total is zero search its code in expirieslist set the expiry date
            for (CatalogueItem item : catalogueItems) {
                if (item.getTOTAL() == null) {
                    String code = item.getITEMNO();
                    if (code != null && !code.isEmpty()) {
                        Expiry expiry = expiries.stream()
                                .filter(e -> {
                                    String finalCode = formatCode(e.getCode());
                                    return code.contains(finalCode);
                                })
                                .findFirst()
                                .orElse(null);
                        if (expiry != null) {
                            item.setEXPIRY(UpdateExpiryDate.convertDateFormat(expiry.getDate()));
                            item.setTOTAL("0");
                            item.setNOTE("[NM]");
                        } else {
                            if (!item.getIGNORE()) {
                                System.out.println("No expiry found for code: " + code);
                                item.setTOTAL("0");
                                item.setNOTE("[N/A]");
                            } else {
                                item.setTOTAL("0");
                                item.setNOTE("[IG]");
                            }
                        }
                    } else {
                        System.out.println("Invalid ITEMNO for item: " + item.getITEMDESC());
                    }
                }
            }
            //loop through the catalogue items and check the matching requested items
            for (CatalogueItem item : catalogueItems) {
                String code = item.getITEMNO();
                if (code != null && !code.isEmpty()) {
                    RequestedItem requestedItem = requestedItems.stream()
                            .filter(r -> code.contains(formatCode(r.getCode())))
                            .findFirst()
                            .orElse(null);
                    Expiry expiry = expiries.stream()
                        .filter(e -> {
                            String finalCode = formatCode(e.getCode());
                            return code.contains(finalCode);
                        })
                        .findFirst()
                        .orElse(null);
                    if (requestedItem != null) {
                        //chekc the total by unit and hisquantity
                        double stock = item.getSTOCK();
                        double out = item.getMOVEMENT();
                        double requestedQuantity = requestedItem.getHisQuantity();
                        item.setTOTAL(String.format("%.2f",requestedQuantity/parseDoubleSafe(item.getPACK())));
                        if(item.getIGNORE()) {
                                item.setNOTE("[Requested - Check - Must ignore]");
                        } else {
                            if (out == 0) {
                                item.setNOTE("[Requested - Check - Not Moved]");
                            } else {
                                double percentageAfter = (stock + requestedQuantity) / out * 100;
                                //too much requested if percetage is above 150% 
                                if (percentageAfter > 150) {
                                    item.setNOTE("[Requested - Check - "+String.format("%.2f", percentageAfter)+"]");
                                } else if (percentageAfter <= 150 && percentageAfter >= 40){
                                    item.setNOTE("[Requested - "+String.format("%.2f", percentageAfter)+"]");
                                } else if (percentageAfter < 40 && percentageAfter > 0) {
                                    item.setNOTE("[Requested - Check - "+String.format("%.2f", percentageAfter)+"]");
                                } else if (percentageAfter == 0) {
                                    item.setNOTE("[Requested - Check - "+String.format("%.2f", percentageAfter)+"]");
                                } else {
                                    item.setNOTE("[Requested - Check - Else]");
                                }
                            }  
                        }
                    } else {
                        double stock = item.getSTOCK();
                        double out = item.getMOVEMENT();
                        if(item.getIGNORE()) {
                                item.setNOTE("[NR - Ignored]");
                        } else {
                            if (out == 0) {
                                    item.setNOTE("[NR - Check - Not Moved]");
                            } else {
                                double percentageAfter = (stock) / out * 100;
                                //too much requested if percetage is above 150% 
                                if (percentageAfter > 150) {
                                    item.setNOTE("[NR - "+String.format("%.2f", percentageAfter)+"]");
                                } else if (percentageAfter <= 150 && percentageAfter >= 40){
                                    item.setNOTE("[NR - "+String.format("%.2f", percentageAfter)+"]");
                                } else if (percentageAfter < 40 && percentageAfter > 0) {
                                    item.setNOTE("[NR - Check - "+String.format("%.2f", percentageAfter)+"]");
                                } else if (percentageAfter == 0) {
                                    item.setNOTE("[NR - Check - "+String.format("%.2f", percentageAfter)+"]");
                                } else {
                                    item.setNOTE("[NR - Check - Else]");
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Invalid ITEMNO for item: " + item.getITEMDESC());
                }
            }

            catalogueItems = catalogueItems.stream()
                .collect(Collectors.groupingBy(item -> item.getITEMNO().substring(0, 12)))
                .values().stream()
                .flatMap(group -> {
                    // 1️⃣ Collect special items (always keep them)
                    List<CatalogueItem> specials = group.stream()
                        .filter(item -> specialCodesCatalogue.contains(item.getITEMNO()))
                        .collect(Collectors.toList());

                    // 2️⃣ Collect non-specials
                    List<CatalogueItem> nonSpecials = group.stream()
                        .filter(item -> !specialCodesCatalogue.contains(item.getITEMNO()))
                        .collect(Collectors.toList());

                    // Split non-specials into zero and non-zero totals
                    List<CatalogueItem> nonZeroTotals = nonSpecials.stream()
                        .filter(item -> parseDoubleSafe(item.getTOTAL()) != 0)
                        .collect(Collectors.toList());

                    List<CatalogueItem> zeroTotals = nonSpecials.stream()
                        .filter(item -> parseDoubleSafe(item.getTOTAL()) == 0)
                        .collect(Collectors.toList());

                    List<CatalogueItem> resultNonSpecials = new ArrayList<>();

                    if (!nonZeroTotals.isEmpty()) {
                        // Case 1️⃣ and 3️⃣: keep only one non-zero instance
                        resultNonSpecials.add(nonZeroTotals.get(0));
                    } else if (!zeroTotals.isEmpty()) {
                        // Case 2️⃣: all zeros → keep one zero instance
                        resultNonSpecials.add(zeroTotals.get(0));
                    }

                    // 3️⃣ Merge specials and non-specials
                    List<CatalogueItem> result = new ArrayList<>();
                    result.addAll(specials);
                    result.addAll(resultNonSpecials);

                    return result.stream();
                })
                .collect(Collectors.toList());
        
        CatalogueSorter.sortCatalogue(catalogueItems);
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

    private static String formatCode(String code) {
        if (specialCodes.contains(code)) {
            code = code.replaceFirst("6505-99", "6505-02").replace("-", "");
            System.out.println("ITEMNO: " + code + " is a special code, formatted to: " + code);

        } else {
            code = code.replaceFirst("6505-99", "6505-02").replace("-", "");
            if (code.length() > 12) {
                code = code.substring(0, 12);
            }
        }
        return code;
    }
}
