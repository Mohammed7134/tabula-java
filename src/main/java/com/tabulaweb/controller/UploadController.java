package com.tabulaweb.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tabulaweb.model.CatalogueItem;
import com.tabulaweb.model.Expiry;
import com.tabulaweb.model.RequestedItem;
import com.tabulaweb.model.ReturnedItem;
import com.tabulaweb.model.briefItem;
import com.tabulaweb.parser.DetailedParser;
import com.tabulaweb.parser.ExpiryParser;
import com.tabulaweb.parser.RequestedParser;
import com.tabulaweb.parser.briefParser;
import com.tabulaweb.utils.DeductReturned;
import com.tabulaweb.utils.TalabiyaChecker;
import com.tabulaweb.utils.TalabiyaProcessor;
import com.tabulaweb.utils.UpdateExpiryDate;

@RestController
public class UploadController {
// last version
    @PostMapping(value = "/process-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> handleUploadprocessAll(
            @RequestParam("expiries") MultipartFile expiries,
            @RequestParam("detailed") MultipartFile detailed,
            @RequestParam("brief") MultipartFile brief,
            @RequestParam(value = "requested", required = false) MultipartFile requested,
            @RequestParam(value = "urgent", required = false) boolean urgent
    ) throws Exception {
        List<Expiry> expiryList = ExpiryParser.parseExpiries(expiries.getInputStream());
        List<ReturnedItem> returnedItems = DetailedParser.parseDetailed(detailed.getInputStream());
        List<briefItem> briefItems = briefParser.parsebrief(brief.getInputStream());
        briefItems = DeductReturned.deductReturnedMedicines(returnedItems, briefItems);
        briefItems = UpdateExpiryDate.updateExpiryDates(briefItems, expiryList);
        StringBuilder html = new StringBuilder();
        List<CatalogueItem> catalogueItems;
        if (requested == null || requested.isEmpty()) {
                catalogueItems = TalabiyaProcessor.writeTalabiya(briefItems, expiryList, urgent);
        } else {
                List<RequestedItem> requestedItems = RequestedParser.parseRequested(requested.getInputStream());
                catalogueItems = TalabiyaChecker.checkTalabiya(briefItems, expiryList, requestedItems);
        }
                System.out.println("Processed " + catalogueItems.size() + " catalogue items.");
                if(!urgent) {
                        // Start table
                        html.append("<table id='catalogueTable' class='display'>");
                        // ===== THEAD =====
                        html.append("<thead>");
                        html.append("<tr>");
                        String[] headers = {"STORE", "ITEMNO", "ITEMDESC", "EXPIRY", "PACK", "TOTAL", "NOTE"};
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
                        html.append("<tbody>");
                        for (CatalogueItem item : catalogueItems) {
                        // Calculate STORE same way as in JS
                        String store = "";
                        String expiry = item.getEXPIRY();
                        String display = expiry; // keep as is
                        String sortable = "";
                        if (item.getITEMNO() != null && item.getITEMNO().length() >= 8) {
                                store = item.getITEMNO().substring(6, 8);
                        }
                        if (expiry != null && !expiry.isBlank()) {
                                DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                DateTimeFormatter outputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                LocalDate parsed = LocalDate.parse(expiry, inputFmt);
                                sortable = parsed.format(outputFmt);
                        }

                        html.append("<tr>");
                        html.append("<td>").append(store).append("</td>");
                        html.append("<td>").append(item.getITEMNO() != null ? item.getITEMNO() : "").append("</td>");
                        html.append("<td>").append(item.getITEMDESC() != null ? item.getITEMDESC() : "").append("</td>");
                        html.append("<td").append(!sortable.isEmpty() ? " data-order='" + sortable + "'" : "").append(">").append(display != null ? display : "").append("</td>");
                        html.append("<td style='padding:0 2rem;'>").append(item.getPACK() != null ? item.getPACK() : "").append("</td>");
                        html.append("<td>").append(item.getTOTAL() != null ? item.getTOTAL() : "").append("</td>");
                        html.append("<td>").append(item.getNOTE() != null ? item.getNOTE() : "").append("</td>");
                        html.append("</tr>");
                        }
                        html.append("</tbody>");

                        // End table
                        html.append("</table>");
                } else {
                        // Start table
                        html.append("<table id='catalogueTable' class='display'>");
                        // ===== THEAD =====
                        html.append("<thead>");
                        html.append("<tr>");
                        String[] headers = {"ITEMNO", "ITEMDESC", "EXPIRY", "STOCK", "MINIMUM", "MOVED", "ALTERNATIVE?", "TOTAL", "NOTE"};
                        for (String header : headers) {
                                html.append("<th");
                                html.append(">").append(header).append("</th>");
                        }
                        html.append("</tr>");
                        html.append("</thead>");

                        // ===== TBODY =====
                        html.append("<tbody>");
                        for (CatalogueItem item : catalogueItems) {
                                String expiry = item.getEXPIRY();
                                String display = expiry; // keep as is
                                String sortable = "";
                                
                                if (expiry != null && !expiry.isBlank()) {
                                        DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                        DateTimeFormatter outputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                        LocalDate parsed = LocalDate.parse(expiry, inputFmt);
                                        sortable = parsed.format(outputFmt);
                                }

                                html.append("<tr>");
                                html.append("<td>").append(item.getITEMNO() != null ? item.getITEMNO() : "").append("</td>");
                                html.append("<td>").append(item.getITEMDESC() != null ? item.getITEMDESC() : "").append("</td>");
                                html.append("<td").append(!sortable.isEmpty() ? " data-order='" + sortable + "'" : "").append(">").append(display != null ? display : "").append("</td>");
                                html.append("<td>").append(item.getSTOCK()).append("</td>");
                                html.append("<td>").append(item.getMINIMUM() != null ? item.getMINIMUM() : "").append("</td>");
                                html.append("<td>").append(item.getMOVEMENT()).append("</td>");
                                html.append("<td>").append(item.getALTERNATIVE() != null ? item.getALTERNATIVE() : "").append("</td>");
                                html.append("<td>").append(item.getTOTAL() != null ? item.getTOTAL() : "").append("</td>");
                                html.append("<td>").append(item.getNOTE() != null ? item.getNOTE() : "").append("</td>");
                                html.append("</tr>");
                        }
                        html.append("</tbody>");

                        // End table
                        html.append("</table>");
                }
                // Wrap in response
                Map<String, String> response = new HashMap<>();
                response.put("html", html.toString());
                return ResponseEntity.ok(response);
    }
}
