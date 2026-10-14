package com.project.RoutineBuilder.service;

import com.project.RoutineBuilder.dto.ClashResponse;
import com.project.RoutineBuilder.entity.ClashRule;
import com.project.RoutineBuilder.entity.Product;
import com.project.RoutineBuilder.repository.ClashRuleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchClashService {

    private final ClashRuleRepository clashRuleRepository;

    public ClashResponse validateRoutine(List<Product> selectedProducts) {
        if (selectedProducts == null || selectedProducts.isEmpty()) {
            return new ClashResponse(true, "No products selected.");
        }

        Set<String> ingredients = new HashSet<>();
        for (Product product : selectedProducts) {
            if (product != null && product.getActiveIngredient() != null) {
                ingredients.add(normalize(product.getActiveIngredient()));
            }
        }

        for (ClashRule rule : clashRuleRepository.findAll()) {
            String ingredientOne = normalize(rule.getIngredientOne());
            String ingredientTwo = normalize(rule.getIngredientTwo());
            if (ingredients.contains(ingredientOne) && ingredients.contains(ingredientTwo)) {
                return new ClashResponse(false, rule.getAlertMessage());
            }
        }

        return new ClashResponse(true, "Routine is safe.");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
