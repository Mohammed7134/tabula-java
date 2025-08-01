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

public class ExpiryParser {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}-\\d{5}\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{2}-[A-Z]{3}-\\d{2}\\b");

    public static List<Expiry> parseExpiries(InputStream pdfInputStream) throws Exception {
        List<Expiry> expiries = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(pdfInputStream))) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            PageIterator pages = extractor.extract();

            BasicExtractionAlgorithm algorithm = new BasicExtractionAlgorithm();

            while (pages.hasNext()) {
                Page page = pages.next();
                List<Table> tables = algorithm.extract(page);

                for (Table table : tables) {
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        if (row.size() >= 3) {
                            String codeCell = row.get(0).getText().trim();
                            String dateCell = row.get(2).getText().trim();

                            Matcher codeMatcher = CODE_PATTERN.matcher(codeCell);
                            Matcher dateMatcher = DATE_PATTERN.matcher(dateCell);

                            if (codeMatcher.find() && dateMatcher.find()) {
                                String code = codeMatcher.group();
                                String date = dateMatcher.group();
                                expiries.add(new Expiry(code, date));
                            }
                        }
                    }
                }
            }
        }

        return expiries;
    }
}
