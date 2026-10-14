package com.project.RoutineBuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkinAnalysisResponse {

    private String detectedConcern;
    private double confidence;
    /** "huggingface" when the real AI model was used, otherwise "heuristic". */
    private String source;
    private RoutineResponse routine;
}
