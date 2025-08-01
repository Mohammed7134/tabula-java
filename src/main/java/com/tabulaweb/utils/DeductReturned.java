package com.tabulaweb.utils;

import java.util.List;

import com.tabulaweb.model.BreifItem;
import com.tabulaweb.model.ReturnedItem;

public class DeductReturned {

    // Returns the updated list of BreifItems after deduction
    public static List<BreifItem> deductReturnedMedicines(List<ReturnedItem> returnItems, List<BreifItem> breifItems) {
        if (returnItems == null || returnItems.isEmpty()) {
            System.out.println("No return orders found.");
            return breifItems;
        }

        for (ReturnedItem update : returnItems) {
            String updateNameLower = update.getCode().toLowerCase(); // or getName() if you have name field

            for (BreifItem row : breifItems) {
                String nameLower = row.getName() != null ? row.getName().toLowerCase() : "";

                if (nameLower.contains(updateNameLower)) {
                    double currentStock = row.getCurrentStock();
                    double totalOut = row.getTotalOut();

                    double newTotalOut = totalOut - update.getQuantity();
                    if (newTotalOut < 0) {
                        newTotalOut = 0;
                    }

                    double newDifference = currentStock - newTotalOut;

                    // Update the fields
                    row.setTotalOut(newTotalOut);
                    row.setDifference(newDifference);

                    System.out.printf("Updated item %s: totalOut=%.2f, difference=%.2f%n",
                            row.getName(), newTotalOut, newDifference);
                }
            }
        }

        return breifItems;
    }
}
