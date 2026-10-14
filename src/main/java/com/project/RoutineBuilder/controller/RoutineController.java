package com.project.RoutineBuilder.controller;

import com.project.RoutineBuilder.dto.QuizRequest;
import com.project.RoutineBuilder.dto.RoutineResponse;
import com.project.RoutineBuilder.service.RoutineBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routine")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineBuilderService routineBuilderService;

    @PostMapping("/build")
    public ResponseEntity<RoutineResponse> buildRoutine(@RequestBody QuizRequest request) {
        return ResponseEntity.ok(routineBuilderService.buildRoutine(request.getPrimaryConcern()));
    }
}
