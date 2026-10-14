package com.project.RoutineBuilder.service;

import com.project.RoutineBuilder.dto.AllergenOption;
import com.project.RoutineBuilder.entity.Product;
import com.project.RoutineBuilder.model.Allergen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AllergyService {

    public List<AllergenOption> commonAllergens() {
        return Arrays.stream(Allergen.values())
                .map(a -> new AllergenOption(a.name(), a.getLabel(), a.getDescription()))
                .toList();
    }

    /**
     * Parses a comma-separated allergy string. Each entry may be an allergen key
     * (e.g. TREE_NUTS), a label or alias (e.g. "nuts", "aspirin"), or any free-text
     * ingredient name, which is then matched literally.
     */
    public List<AllergyRule> parse(String allergies) {
        if (allergies == null || allergies.isBlank()) {
            return List.of();
        }
        Map<String, AllergyRule> rules = new LinkedHashMap<>();
        for (String raw : allergies.split("[,;\\n]")) {
            String token = normalize(raw);
            if (token.length() < 2) {
                continue;
            }
            Optional<Allergen> allergen = resolve(token);
            if (allergen.isPresent()) {
                Allergen a = allergen.get();
                rules.putIfAbsent(a.name(), new AllergyRule(a.getLabel(), a.getIngredientKeywords()));
            } else if (token.length() >= 3) {
                rules.putIfAbsent(token, new AllergyRule(raw.trim(), List.of(token)));
            }
        }
        return new ArrayList<>(rules.values());
    }

    /** Returns the first allergen found in the product, if any. */
    public Optional<AllergyHit> findHit(Product product, List<AllergyRule> rules) {
        if (product == null || rules.isEmpty()) {
            return Optional.empty();
        }
        String haystack = normalize(String.join(" ",
                nullToEmpty(product.getName()),
                nullToEmpty(product.getActiveIngredient()),
                nullToEmpty(product.getIngredients())));
        for (AllergyRule rule : rules) {
            for (String keyword : rule.keywords()) {
                if (haystack.contains(keyword)) {
                    return Optional.of(new AllergyHit(rule.label(), keyword));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Allergen> resolve(String token) {
        for (Allergen allergen : Allergen.values()) {
            if (token.equals(normalize(allergen.name()))
                    || token.equals(normalize(allergen.name().replace('_', ' ')))
                    || token.equals(normalize(allergen.getLabel()))
                    || allergen.getAliases().contains(token)
                    || allergen.getIngredientKeywords().contains(token)) {
                return Optional.of(allergen);
            }
        }
        return Optional.empty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record AllergyRule(String label, List<String> keywords) {
    }

    public record AllergyHit(String label, String keyword) {

        public String describe() {
            return label.equalsIgnoreCase(keyword) ? keyword : keyword + " (" + label + ")";
        }
    }
}
