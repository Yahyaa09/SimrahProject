package com.project.RoutineBuilder.controller;

import com.project.RoutineBuilder.dto.AllergenOption;
import com.project.RoutineBuilder.dto.QuizRequest;
import com.project.RoutineBuilder.dto.RoutineResponse;
import com.project.RoutineBuilder.dto.SkinAnalysisResponse;
import com.project.RoutineBuilder.service.AllergyService;
import com.project.RoutineBuilder.service.RoutineBuilderService;
import com.project.RoutineBuilder.service.SkinAnalysisService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/routine")
@CrossOrigin(origins = "http://localhost:5173") // Allows React (Vite dev server) to call this API
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineBuilderService routineBuilderService;
    private final SkinAnalysisService skinAnalysisService;
    private final AllergyService allergyService;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @PostMapping("/build")
    public ResponseEntity<RoutineResponse> buildRoutine(@RequestBody QuizRequest request) {
        return ResponseEntity.ok(routineBuilderService.buildRoutine(request.getPrimaryConcern(), request.getAllergies()));
    }

    @GetMapping("/allergens")
    public List<AllergenOption> commonAllergens() {
        return allergyService.commonAllergens();
    }

    @PostMapping(value = "/analyze-skin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SkinAnalysisResponse> analyzeSkin(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "allergies", required = false) String allergies) throws IOException {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("Please choose an image to upload.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
        return ResponseEntity.ok(skinAnalysisService.analyze(image, allergies));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
