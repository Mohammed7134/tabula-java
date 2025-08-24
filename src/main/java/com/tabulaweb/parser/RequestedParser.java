package com.tabulaweb.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.tabulaweb.model.RequestedItem;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class RequestedParser {
        public static List<RequestedItem> parseRequested(InputStream pdfInputStream) throws Exception {
        List<RequestedItem> requestedItems = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(pdfInputStream))) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            PageIterator pageIterator = extractor.extract();

            SpreadsheetExtractionAlgorithm algo = new SpreadsheetExtractionAlgorithm();

            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();
                List<Table> tables = algo.extract(page);

                for (Table table : tables) {
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        if (row.size() < 4) {
                            continue;  // We expect at least 6 columns
                        }
                        try {
                            String code = row.get(0).getText().trim();
                            String name = row.get(1).getText().trim();
                            double hisQuantity = parseDoubleSafe(row.get(2).getText().trim());
                            double smsQuantity = parseDoubleSafe(row.get(3).getText().trim());

                            RequestedItem item = new RequestedItem(code, name, hisQuantity, smsQuantity);
                            requestedItems.add(item);

                        } catch (NumberFormatException | NullPointerException e) {
                            // skip rows that cannot be parsed correctly
                        }
                    }
                }
            }
        }

        return requestedItems;
    }

    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.replace(",", ""));
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }
}
