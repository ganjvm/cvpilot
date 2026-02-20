package dev.gan.cvpilot.controller;

import dev.gan.cvpilot.dto.ApiResponse;
import dev.gan.cvpilot.dto.analysis.AnalysisRequest;
import dev.gan.cvpilot.dto.analysis.FullAnalysisResponse;
import dev.gan.cvpilot.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/match")
    public ResponseEntity<ApiResponse<FullAnalysisResponse>> analyzeMatch(@Valid @RequestBody AnalysisRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FullAnalysisResponse result = analysisService.analyzeMatch(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
