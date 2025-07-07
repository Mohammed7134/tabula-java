// Standard Java I/O
package technology.tabula;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;

import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class ExtractTables {

    public static void main(String[] args) {
        try {
            InputStream in = new FileInputStream("my.pdf");
            try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(in))) {
                SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
                PageIterator pi = new ObjectExtractor(document).extract();
                while (pi.hasNext()) {
                    Page page = pi.next();
                    List<Table> tables = sea.extract(page);
                    for (Table table : tables) {
                        List<List<RectangularTextContainer>> rows = table.getRows();
                        for (List<RectangularTextContainer> cells : rows) {
                            for (RectangularTextContainer content : cells) {
                                String text = content.getText().replace("\r", " ");
                                System.out.print(text + "|");
                            }
                            System.out.println();
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("PDF file not found. Please check the path.");
        } catch (IOException e) {
            System.err.println("Error reading or parsing the PDF.");
        } catch (Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace();
        }

    }
}
