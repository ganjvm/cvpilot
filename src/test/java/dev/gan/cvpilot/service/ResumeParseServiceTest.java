package dev.gan.cvpilot.service;

import dev.gan.cvpilot.dto.resume.ResumeParseResponse;
import dev.gan.cvpilot.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ResumeParseServiceTest {

    private ResumeParseService service;

    @BeforeEach
    void setUp() {
        service = new ResumeParseService();
    }

    // --- PDF tests ---

    @Test
    void parsePdf_realFile_extractsText() throws IOException {
        MockMultipartFile file = loadTestFile("resume/resume.pdf", "resume.pdf", "application/pdf");

        ResumeParseResponse response = service.parse(file);

        assertEquals("pdf", response.detectedFormat());
        assertTrue(response.characterCount() >= 50, "Extracted text should have at least 50 chars");
        assertEquals(response.text().length(), response.characterCount());
        assertFalse(response.text().isBlank(), "Extracted text should not be blank");
    }

    // --- DOCX tests ---

    @Test
    void parseDocx_realFile_extractsText() throws IOException {
        MockMultipartFile file = loadTestFile("resume/resume.docx", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        ResumeParseResponse response = service.parse(file);

        assertEquals("docx", response.detectedFormat());
        assertTrue(response.characterCount() >= 50, "Extracted text should have at least 50 chars");
        assertEquals(response.text().length(), response.characterCount());
        assertFalse(response.text().isBlank(), "Extracted text should not be blank");
    }

    // --- Format detection by extension ---

    @Test
    void parse_uppercaseExtension_detected() throws IOException {
        MockMultipartFile file = loadTestFile("resume/resume.pdf", "Resume.PDF", "application/pdf");

        ResumeParseResponse response = service.parse(file);

        assertEquals("pdf", response.detectedFormat());
    }

    // --- Unsupported file type ---

    @Test
    void parse_unsupportedExtension_throwsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain",
                "some text content that is long enough to pass min check".getBytes());

        ApiException ex = assertThrows(ApiException.class, () -> service.parse(file));
        assertEquals("UNSUPPORTED_FILE_TYPE", ex.getErrorCode());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getHttpStatus());
    }

    @Test
    void parse_nullFilename_throwsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", null, "application/pdf",
                "some content".getBytes());

        ApiException ex = assertThrows(ApiException.class, () -> service.parse(file));
        assertEquals("UNSUPPORTED_FILE_TYPE", ex.getErrorCode());
    }

    // --- Empty / too short content ---

    @Test
    void parse_emptyPdf_throwsFileEmpty() {
        // A minimal valid PDF with no text content
        byte[] emptyPdfBytes = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                "2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n" +
                "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n" +
                "xref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n" +
                "0000000115 00000 n \ntrailer<</Size 4/Root 1 0 R>>\nstartxref\n190\n%%EOF")
                .getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", emptyPdfBytes);

        ApiException ex = assertThrows(ApiException.class, () -> service.parse(file));
        assertTrue(ex.getErrorCode().equals("FILE_EMPTY") || ex.getErrorCode().equals("FILE_PARSE_ERROR"),
                "Should throw FILE_EMPTY or FILE_PARSE_ERROR for empty PDF, got: " + ex.getErrorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
    }

    // --- Corrupted file ---

    @Test
    void parse_corruptedPdf_throwsParseError() {
        MockMultipartFile file = new MockMultipartFile("file", "broken.pdf", "application/pdf",
                "this is not a real pdf file content at all".getBytes());

        ApiException ex = assertThrows(ApiException.class, () -> service.parse(file));
        assertTrue(ex.getErrorCode().equals("FILE_PARSE_ERROR") || ex.getErrorCode().equals("FILE_EMPTY"),
                "Should throw FILE_PARSE_ERROR or FILE_EMPTY for corrupted PDF, got: " + ex.getErrorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
    }

    @Test
    void parse_corruptedDocx_throwsParseError() {
        MockMultipartFile file = new MockMultipartFile("file", "broken.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "this is not a real docx file".getBytes());

        ApiException ex = assertThrows(ApiException.class, () -> service.parse(file));
        assertEquals("FILE_PARSE_ERROR", ex.getErrorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
    }

    // --- Max chars truncation ---

    @Test
    void parse_characterCountMatchesTextLength() throws IOException {
        MockMultipartFile file = loadTestFile("resume/resume.pdf", "resume.pdf", "application/pdf");

        ResumeParseResponse response = service.parse(file);

        assertEquals(response.text().length(), response.characterCount());
        assertTrue(response.characterCount() <= 20_000, "Text should be capped at 20,000 chars");
    }

    // --- Whitespace normalization ---

    @Test
    void parse_realPdf_noExcessiveWhitespace() throws IOException {
        MockMultipartFile file = loadTestFile("resume/resume.pdf", "resume.pdf", "application/pdf");

        ResumeParseResponse response = service.parse(file);

        assertFalse(response.text().contains("\t"), "Tabs should be normalized to spaces");
        assertFalse(response.text().contains("  "), "Consecutive spaces should be collapsed");
        assertFalse(response.text().contains("\n\n\n"), "3+ consecutive newlines should be collapsed");
        assertFalse(response.text().startsWith(" "), "Text should be stripped");
        assertFalse(response.text().endsWith(" "), "Text should be stripped");
    }

    // --- Helper ---

    private MockMultipartFile loadTestFile(String resourcePath, String filename, String contentType) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Test resource not found: " + resourcePath);
            return new MockMultipartFile("file", filename, contentType, is.readAllBytes());
        }
    }
}
