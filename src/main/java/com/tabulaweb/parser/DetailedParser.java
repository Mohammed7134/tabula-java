package com.tabulaweb.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.tabulaweb.model.ReturnedItem;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;

public class DetailedParser {

    public static List<ReturnedItem> parseDetailed(InputStream pdfInputStream) throws Exception {
        Map<String, ReturnedItem> resultMap = new HashMap<>();

        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(pdfInputStream))) {

            ObjectExtractor extractor = new ObjectExtractor(document);
            BasicExtractionAlgorithm algo = new BasicExtractionAlgorithm();
            PageIterator pageIterator = extractor.extract();

            List<List<RectangularTextContainer>> allRows = new ArrayList<>();

            // Collect all rows from all tables on all pages
            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();
                List<Table> tables = algo.extract(page);
                for (Table table : tables) {
                    allRows.addAll(table.getRows());
                }
            }

            for (int i = 0; i < allRows.size(); i++) {
                List<RectangularTextContainer> row = allRows.get(i);

                boolean hasReturnOrder = row.stream()
                        .anyMatch(cell -> cell.getText().toLowerCase().contains("return order"));
                boolean hasOut = row.stream()
                        .anyMatch(cell -> cell.getText().contains("OUT")); // case-sensitive

                if (hasReturnOrder && hasOut) {
                    double quantity = 0.0;
                    String code = "";

                    // Extract quantity after "return order"
                    for (RectangularTextContainer cell : row) {
                        String text = cell.getText();
                        String lowerText = text.toLowerCase();
                        int pos = lowerText.indexOf("return order");
                        if (pos != -1) {
                            String afterText = text.substring(pos + "return order".length()).trim();
                            String[] parts = afterText.split("[^0-9.+-]");
                            if (parts.length > 0) {
                                try {
                                    quantity = Double.parseDouble(parts[0]);
                                } catch (NumberFormatException e) {
                                    quantity = 0.0;
                                }
                            }
                        }
                    }

                    // Look backwards to find nearest "Item:" cell
                    for (int j = i - 1; j >= 0; j--) {
                        List<RectangularTextContainer> prevRow = allRows.get(j);
                        boolean found = false;
                        for (RectangularTextContainer cell : prevRow) {
                            String cellText = cell.getText().trim();
                            String lowerCellText = cellText.toLowerCase();
                            if (lowerCellText.startsWith("item:")) {
                                int start = lowerCellText.indexOf("item:") + 5;
                                int end = lowerCellText.indexOf("current stock:");
                                if (end == -1) {
                                    end = cellText.length();
                                }
                                code = cellText.substring(start, end).trim();
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            break;
                        }
                    }

                    // Add or accumulate
                    if (!code.isEmpty() && quantity != 0) {
                        double finalQuantity = quantity;
                        resultMap.compute(code, (k, v) -> {
                            if (v == null) {
                                return new ReturnedItem(k, finalQuantity);
                            }
                            v.setQuantity(v.getQuantity() + finalQuantity);
                            return v;
                        });
                    }
                }
            }
        }

        return new ArrayList<>(resultMap.values());
    }
}
