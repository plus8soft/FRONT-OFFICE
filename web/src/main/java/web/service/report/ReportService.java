/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private static final FontProgram FONT = readFont();

    private static FontProgram readFont() {
        try {
            return PdfFontFactory
                    .createFont(Files.readAllBytes(Paths.get(ReportService.class.getResource("/font/calibri.ttf").toURI())), PdfEncodings.IDENTITY_H,
                                true).getFontProgram();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public byte[] buildReportBytes(Consumer<Document> consumer) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Document document = new Document(new PdfDocument(new PdfWriter(outputStream)), PageSize.A4)
                     .setFont(PdfFontFactory.createFont(FONT, PdfEncodings.IDENTITY_H, true))) {
            consumer.accept(document);
            document.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String buildReport(byte[] report) {
        try {
            return Files.write(Files.createTempFile(null, null), report).getFileName().toString().replaceFirst("[.][^.]+$", "");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String buildReport(Consumer<Document> consumer) {
        return buildReport(buildReportBytes(consumer));
    }

    public InputStream getReport(String id) {
        try {
            return Files.newInputStream(Paths.get(System.getProperty("java.io.tmpdir"), id + ".tmp"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
