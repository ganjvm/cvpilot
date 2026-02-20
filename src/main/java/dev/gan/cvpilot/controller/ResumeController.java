package dev.gan.cvpilot.controller;

import dev.gan.cvpilot.dto.ApiResponse;
import dev.gan.cvpilot.dto.resume.ResumeParseResponse;
import dev.gan.cvpilot.service.ResumeParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeParseService resumeParseService;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeParseResponse>> parseResume(@RequestParam("file") MultipartFile file) {
        ResumeParseResponse result = resumeParseService.parse(file);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
