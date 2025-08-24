package com.tabulaweb.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@RestController
public class UploadPDFController {
// last version
    @PostMapping(value = "/process-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> handleUploadprocessAll(
            @RequestParam("pdf") MultipartFile pdf
    ) throws Exception {
        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(pdf.getInputStream()))) {
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
                            double HIS = parseDoubleSafe(row.get(2).getText().trim());
                            double SMS = parseDoubleSafe(row.get(3).getText().trim());

                            System.out.println(code + "   " + name + "   " + HIS + "   " + SMS + "   ");

                        } catch (NumberFormatException | NullPointerException e) {
                            // skip rows that cannot be parsed correctly
                        }
                    }
                }
            }
        }
        StringBuilder html = new StringBuilder();

        // Start table
        html.append("<table id='catalogueTable' class='display'>");

        // ===== THEAD =====
        html.append("<thead>");
        html.append("<tr>");
        String[] headers = {"CODE", "NAME", "HIS", "SMS"};
        for (String header : headers) {
                html.append("<th");
                if ("PACK".equals(header)) {
                        html.append(" style='padding:0 2rem;'");
                }
                html.append(">").append(header).append("</th>");
        }
        html.append("</tr>");
        html.append("</thead>");

// ===== TBODY =====
        // html.append("<tbody>");
        // for (CatalogueItem item : catalogueItems) {
        //     // Calculate STORE same way as in JS
        //     String store = "";
        //     if (item.getITEMNO() != null && item.getITEMNO().length() >= 8) {
        //         store = item.getITEMNO().substring(6, 8);
        //     }

        //     html.append("<tr>");
        //     html.append("<td>").append(store).append("</td>");
        //     html.append("<td>").append(item.getITEMNO() != null ? item.getITEMNO() : "").append("</td>");
        //     html.append("<td>").append(item.getITEMDESC() != null ? item.getITEMDESC() : "").append("</td>");
        //     html.append("<td>").append(item.getEXPIRY() != null ? item.getEXPIRY() : "").append("</td>");
        //     html.append("<td style='padding:0 2rem;'>").append(item.getPACK() != null ? item.getPACK() : "").append("</td>");
        //     html.append("<td>").append(item.getTOTAL() != null ? item.getTOTAL() : "").append("</td>");
        //     html.append("</tr>");
        // }
        // html.append("</tbody>");

// End table
        html.append("</table>");

// Wrap in response
        Map<String, String> response = new HashMap<>();
        response.put("html", html.toString());
        return ResponseEntity.ok(response);

    }
    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.replace(",", ""));
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }
}
