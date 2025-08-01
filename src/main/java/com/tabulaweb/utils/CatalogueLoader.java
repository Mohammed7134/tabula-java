package com.tabulaweb.utils;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tabulaweb.model.CatalogueItem;

public class CatalogueLoader {

    public static List<CatalogueItem> loadCatalogueFromJson() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = CatalogueLoader.class.getClassLoader().getResourceAsStream("catalogue.json")) {
            if (is == null) {
                throw new RuntimeException("Could not find catalogue.json in resources");
            }

            return mapper.readValue(is, new TypeReference<List<CatalogueItem>>() {
            });
        } catch (Exception e) {
            System.err.println("Error loading catalogue: " + e.getMessage());
            return List.of(); // return empty list on failure
        }
    }

    public static void main(String[] args) {
        List<CatalogueItem> catalogue = loadCatalogueFromJson();
        System.out.println("Loaded catalogue with " + catalogue.size() + " items.");
        catalogue.forEach(item -> System.out.println(item.getITEMNO() + " - PACK: " + item.getPACK()));
    }
}
