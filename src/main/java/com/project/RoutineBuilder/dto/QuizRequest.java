package com.project.RoutineBuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    private String primaryConcern;
    private String severity;
    private String allergies;
}
