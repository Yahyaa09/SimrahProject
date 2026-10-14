package com.project.RoutineBuilder.dto;

import com.project.RoutineBuilder.entity.Product;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponse {

    /** All recommended products in application order. */
    private List<Product> orderedRoutine;
    /** Morning routine. Equals orderedRoutine when no clashes had to be resolved. */
    private List<Product> amRoutine;
    /** Evening routine. Empty when no clashes had to be resolved. */
    private List<Product> pmRoutine;
    private ClashResponse safetyCheck;
    /** Explanations of clashes the builder resolved by splitting products into AM / PM. */
    private List<String> adjustments;
    /** Products swapped or removed because they contain one of the user's allergens. */
    private List<String> allergyAdjustments;
    /** Human-readable summary of the quiz answers used to shape the routine. */
    private String profileSummary;
    /** Extra notes explaining the customisation choices. */
    private List<String> personalizationNotes;
}
