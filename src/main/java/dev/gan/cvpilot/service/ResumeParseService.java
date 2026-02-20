package dev.gan.cvpilot.service;

import dev.gan.cvpilot.dto.resume.ResumeParseResponse;
import dev.gan.cvpilot.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class ResumeParseService {

    private static final int MIN_CHARS = 50;
    private static final int MAX_CHARS = 20_000;

    public ResumeParseResponse parse(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE",
                    "Could not determine file type. Please upload a PDF or DOCX file.");
        }

        String lower = filename.toLowerCase();
        String format;
        String rawText;

        if (lower.endsWith(".pdf")) {
            format = "pdf";
            rawText = extractPdf(file);
        } else if (lower.endsWith(".docx")) {
            format = "docx";
            rawText = extractDocx(file);
        } else {
            throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE",
                    "Unsupported file type. Please upload a PDF or DOCX file.");
        }

        String text = normalizeWhitespace(rawText);

        if (text.length() < MIN_CHARS) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "FILE_EMPTY",
                    "The file contains too little text (minimum " + MIN_CHARS + " characters). Please check the file content.");
        }

        if (text.length() > MAX_CHARS) {
            text = text.substring(0, MAX_CHARS);
        }

        return new ResumeParseResponse(text, text.length(), format);
    }

    private String extractPdf(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             PDDocument doc = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("encrypt")) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "FILE_ENCRYPTED",
                        "The PDF file is encrypted. Please upload an unprotected file.");
            }
            log.warn("Failed to parse PDF file: {}", e.getMessage());
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "FILE_PARSE_ERROR",
                    "Failed to parse PDF file. Please ensure the file is not corrupted.");
        }
    }

    private String extractDocx(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append('\n');
                }
            }

            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String text = cell.getText();
                        if (text != null && !text.isBlank()) {
                            sb.append(text).append('\t');
                        }
                    }
                    sb.append('\n');
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to parse DOCX file: {}", e.getMessage());
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "FILE_PARSE_ERROR",
                    "Failed to parse DOCX file. Please ensure the file is not corrupted.");
        }
    }

    private String normalizeWhitespace(String text) {
        if (text == null) return "";
        return text
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .strip();
    }
}
