package com.tabulaweb.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.tabulaweb.model.Expiry;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.Rectangle;

public class ExpiryParser {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}-\\d{5}\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{2}-[A-Z]{3}-\\d{2}\\b");
private static final Pattern QUANTITY_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    public static List<Expiry> parseExpiries(InputStream pdfInputStream) throws Exception {
        List<Expiry> expiries = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(pdfInputStream))) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            
            PageIterator pages = extractor.extract();

            BasicExtractionAlgorithm algorithm = new BasicExtractionAlgorithm();
            // Instantiate the detector once outside the loop
            NurminenDetectionAlgorithm detector = new NurminenDetectionAlgorithm();

            while (pages.hasNext()) {
                Page page = pages.next();
                // 1. Detect potential table areas on this page
                List<Rectangle> detectedAreas = detector.detect(page);
                if (detectedAreas.isEmpty()) {
                    continue; // No tables found on this page
                }

                // 2. Extract from each detected area
                for (Rectangle rect : detectedAreas) {
                    // Get the sub-page corresponding to the detected rectangle
                    Page areaPage = page.getArea(rect);
                    
                    // Run your existing extraction logic on this specific area
                    List<Table> tables = algorithm.extract(areaPage);
                    // List<Table> tables = algorithm.extract(page);

                    for (Table table : tables) {
                        for (List<RectangularTextContainer> row : table.getRows()) {
                            if (row.size() >= 3) {
                                String codeCell = row.get(0).getText().trim();
                                String nameCell = row.get(1).getText().trim();
                                String quantityCell = row.get(2).getText().trim();
                                String dateCell = row.get(3).getText().trim();
                                // print all the cells in the row for debugging
                                // System.out.println("Row: " + codeCell + " | " + nameCell + " | " + quantityCell + " | " + dateCell);
                                Matcher codeMatcher = CODE_PATTERN.matcher(codeCell);
                                Matcher dateMatcher = DATE_PATTERN.matcher(dateCell);
                                Matcher quantityMatcher = QUANTITY_PATTERN.matcher(quantityCell);

                                if (codeMatcher.find() && dateMatcher.find() && quantityMatcher.find()) {
                                    String code = codeMatcher.group();
                                    String date = dateMatcher.group();
                                    String quantity = quantityMatcher.group();
                                    expiries.add(new Expiry(code, date, quantity.isEmpty() ? 1.1 : Double.parseDouble(quantity)));
                                }
                            }
                        }
                    }
                }
            }
        }
        return expiries;
    }
}
