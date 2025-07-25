package com.tabulaweb.controller;

import java.io.InputStream;
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
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@RestController
public class UploadController {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> handleUpload(@RequestParam("file") MultipartFile file, @RequestParam("fileTitle") String title) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<table id='extractedTable' class='display'><tbody>");

        try (InputStream in = file.getInputStream(); PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(in))) {
            // Declare the variable before the if-else block
            BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
            SpreadsheetExtractionAlgorithm spre = new SpreadsheetExtractionAlgorithm();

            PageIterator pi = new ObjectExtractor(document).extract();
            while (pi.hasNext()) {
                Page page = pi.next();
                List<Table> tables = "breif".equals(title) ? spre.extract(page) : sea.extract(page);  // sea is ExtractionAlgorithm
                for (Table table : tables) {
                    List<List<RectangularTextContainer>> rows = table.getRows();
                    for (List<RectangularTextContainer> row : rows) {
                        html.append("<tr>");
                        for (RectangularTextContainer cell : row) {
                            String text = cell.getText().replace("\r", " ");
                            html.append("<td>").append(text).append("</td>");
                        }
                        html.append("</tr>");
                    }
                }
            }
        }
        html.append(
                "</tbody></table>");
        Map<String, String> response = new HashMap<>();
        response.put("html", html.toString());
        response.put("title", title);
        return ResponseEntity.ok(response);
    }
}
