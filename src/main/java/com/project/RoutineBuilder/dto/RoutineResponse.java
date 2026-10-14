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

    private List<Product> orderedRoutine;
    private ClashResponse safetyCheck;
}
