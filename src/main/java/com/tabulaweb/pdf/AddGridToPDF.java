package com.tabulaweb.pdf;

import java.io.File;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class AddGridToPDF {

    public static void addGrid(String inputPath, String outputPath, float rowHeight, float colWidth) throws Exception {
        try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
            for (PDPage page : document.getPages()) {
                PDRectangle mediaBox = page.getMediaBox();
                float width = mediaBox.getWidth();
                float height = mediaBox.getHeight();

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.setStrokingColor(200, 200, 200); // Light gray lines

                    // Draw horizontal lines
                    for (float y = 0; y <= height; y += rowHeight) {
                        contentStream.moveTo(0, y);
                        contentStream.lineTo(width, y);
                    }

                    // Draw vertical lines
                    for (float x = 0; x <= width; x += colWidth) {
                        contentStream.moveTo(x, 0);
                        contentStream.lineTo(x, height);
                    }

                    contentStream.stroke();
                }
            }

            document.save(outputPath);
        }
    }
}
