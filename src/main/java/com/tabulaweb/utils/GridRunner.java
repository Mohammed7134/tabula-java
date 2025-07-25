package com.tabulaweb.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class GridRunner {

    public static void addGridToPDF(InputStream pdfIn, OutputStream pdfOut) throws Exception {
        byte[] pdfBytes = pdfIn.readAllBytes();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                Page page = extractor.extract(i + 1);
                List<Table> tables = sea.extract(page);
                PDPage pdPage = document.getPage(i);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, pdPage, PDPageContentStream.AppendMode.APPEND, true)) {
                    contentStream.setStrokingColor(0, 0, 255); // Blue grid lines

                    for (Table table : tables) {
                        List<List<RectangularTextContainer>> rows = table.getRows();
                        for (int r = 0; r < rows.size(); r++) {
                            List<RectangularTextContainer> cells = rows.get(r);
                            for (RectangularTextContainer cell : cells) {
                                float x = (float) cell.getX();
                                float y = (float) (pdPage.getMediaBox().getHeight() - cell.getY());
                                float w = (float) cell.getWidth();
                                float h = (float) cell.getHeight();
                                contentStream.addRect(x, y - h, w, h);
                            }
                        }
                    }

                    contentStream.stroke();
                } // Blue grid lines
                // Blue grid lines
            }

            document.save(pdfOut);
        }
    }
}
