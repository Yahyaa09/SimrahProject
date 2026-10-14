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
    private String skinType;
    private String sensitivityLevel;
    private String mainGoal;
    private String routineTime;
    private String climate;
    private String budget;
    private String allergies;
}
