package com.project.RoutineBuilder.service;

import com.project.RoutineBuilder.dto.ClashResponse;
import com.project.RoutineBuilder.entity.ClashRule;
import com.project.RoutineBuilder.entity.Product;
import com.project.RoutineBuilder.repository.ClashRuleRepository;
import java.util.ArrayList;
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

        List<ClashRule> clashes = findClashes(selectedProducts);
        if (!clashes.isEmpty()) {
            return new ClashResponse(false, clashes.get(0).getAlertMessage());
        }
        return new ClashResponse(true, "Routine is safe.");
    }

    public List<ClashRule> findClashes(List<Product> products) {
        Set<String> ingredients = new HashSet<>();
        for (Product product : products) {
            if (product != null && product.getActiveIngredient() != null) {
                ingredients.add(normalize(product.getActiveIngredient()));
            }
        }

        List<ClashRule> clashes = new ArrayList<>();
        for (ClashRule rule : clashRuleRepository.findAll()) {
            if (ingredients.contains(normalize(rule.getIngredientOne()))
                    && ingredients.contains(normalize(rule.getIngredientTwo()))) {
                clashes.add(rule);
            }
        }
        return clashes;
    }

    public boolean hasIngredient(Product product, String ingredient) {
        return product != null && normalize(product.getActiveIngredient()).equals(normalize(ingredient));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
