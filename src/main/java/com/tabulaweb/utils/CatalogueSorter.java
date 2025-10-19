
package com.tabulaweb.utils;

import java.util.Comparator;
import java.util.List;

import com.tabulaweb.model.CatalogueItem;

public class CatalogueSorter {
    
    public static void sortCatalogue(List<CatalogueItem> catalogueItems) {
        catalogueItems.sort(Comparator.comparing(CatalogueItem::getITEMDESC, String.CASE_INSENSITIVE_ORDER));
        catalogueItems.sort(Comparator.comparingInt(item -> extractStoreNumber(item.getITEMNO())));
    }

    private static int extractStoreNumber(String itemNo) {
        if (itemNo.length() >= 8) {
            try {
                // Extract 7th and 8th digits (index 6 and 7)
                return Integer.parseInt(itemNo.substring(6, 8));
            } catch (NumberFormatException e) {
                return 0; // fallback
            }
        }
        return 0; // fallback if ITEMNO is too short
    }
}
