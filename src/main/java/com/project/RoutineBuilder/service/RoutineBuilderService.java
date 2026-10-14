package com.project.RoutineBuilder.service;

import com.project.RoutineBuilder.dto.ClashResponse;
import com.project.RoutineBuilder.dto.RoutineResponse;
import com.project.RoutineBuilder.entity.ClashRule;
import com.project.RoutineBuilder.entity.Product;
import com.project.RoutineBuilder.repository.ProductRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutineBuilderService {

    private static final Comparator<Product> BY_VISCOSITY =
            Comparator.comparing(Product::getViscosityRank, Comparator.nullsLast(Integer::compareTo));

    private final ProductRepository productRepository;
    private final MatchClashService matchClashService;
    private final AllergyService allergyService;

    public RoutineResponse buildRoutine(String primaryConcern) {
        return buildRoutine(primaryConcern, null);
    }

    public RoutineResponse buildRoutine(String primaryConcern, String allergies) {
        List<Product> candidates = productRepository.findByTargetConcern(primaryConcern);
        List<Product> alternatives = candidates.stream().filter(this::isAlternative).toList();
        List<Product> primary = new ArrayList<>(candidates.stream().filter(p -> !isAlternative(p)).toList());
        primary.sort(BY_VISCOSITY);

        List<AllergyService.AllergyRule> rules = allergyService.parse(allergies);
        List<String> allergyAdjustments = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        for (Product product : primary) {
            Optional<AllergyService.AllergyHit> hit = allergyService.findHit(product, rules);
            if (hit.isEmpty()) {
                products.add(product);
                continue;
            }
            Optional<Product> substitute = alternatives.stream()
                    .filter(alt -> sameCategory(alt, product))
                    .filter(alt -> !products.contains(alt))
                    .filter(alt -> allergyService.findHit(alt, rules).isEmpty())
                    .findFirst();
            if (substitute.isPresent()) {
                products.add(substitute.get());
                allergyAdjustments.add("Swapped \"" + product.getName() + "\" for \"" + substitute.get().getName()
                        + "\" because it contains " + hit.get().describe() + ".");
            } else {
                allergyAdjustments.add("Removed \"" + product.getName() + "\" because it contains "
                        + hit.get().describe() + " and no allergy-safe alternative is available.");
            }
        }
        products.sort(BY_VISCOSITY);

        return resolveClashes(products, allergyAdjustments);
    }

    private RoutineResponse resolveClashes(List<Product> products, List<String> allergyAdjustments) {
        List<ClashRule> clashes = matchClashService.findClashes(products);
        if (clashes.isEmpty()) {
            return new RoutineResponse(products, products, List.of(),
                    matchClashService.validateRoutine(products), List.of(), allergyAdjustments);
        }

        // Resolve each clash by moving one product of the pair to the evening routine.
        Set<Product> pmOnly = new LinkedHashSet<>();
        List<String> adjustments = new ArrayList<>();
        for (ClashRule clash : clashes) {
            Optional<Product> one = find(products, clash.getIngredientOne());
            Optional<Product> two = find(products, clash.getIngredientTwo());
            if (one.isEmpty() || two.isEmpty() || pmOnly.contains(one.get()) || pmOnly.contains(two.get())) {
                continue;
            }
            Product moved = choosePmProduct(one.get(), two.get());
            pmOnly.add(moved);
            adjustments.add(clash.getAlertMessage().replaceFirst("^Clash:\\s*", "")
                    + " We moved \"" + moved.getName() + "\" to your PM routine.");
        }

        List<Product> am = products.stream().filter(p -> !pmOnly.contains(p)).toList();
        List<Product> pm = new ArrayList<>(pmOnly);
        for (Product product : am) {
            if (isAmOnly(product)) {
                continue;
            }
            List<Product> candidate = new ArrayList<>(pm);
            candidate.add(product);
            if (matchClashService.findClashes(candidate).isEmpty()) {
                pm.add(product);
            }
        }
        pm.sort(BY_VISCOSITY);

        ClashResponse amCheck = matchClashService.validateRoutine(am);
        ClashResponse pmCheck = matchClashService.validateRoutine(pm);
        ClashResponse safetyCheck;
        if (!amCheck.isSafe()) {
            safetyCheck = amCheck;
        } else if (!pmCheck.isSafe()) {
            safetyCheck = pmCheck;
        } else {
            safetyCheck = new ClashResponse(true, "Routine adjusted: clashing products were split into AM and PM.");
        }

        return new RoutineResponse(products, am, pm, safetyCheck, adjustments, allergyAdjustments);
    }

    private Optional<Product> find(List<Product> products, String ingredient) {
        return products.stream().filter(p -> matchClashService.hasIngredient(p, ingredient)).findFirst();
    }

    /** Prefer keeping cleansers in the AM, and move the stronger treatment to the PM. */
    private Product choosePmProduct(Product one, Product two) {
        if (isCategory(one, "Cleanser") && !isCategory(two, "Cleanser")) {
            return two;
        }
        if (isCategory(two, "Cleanser") && !isCategory(one, "Cleanser")) {
            return one;
        }
        return two;
    }

    private boolean isAlternative(Product product) {
        return Boolean.TRUE.equals(product.getAlternative());
    }

    private boolean sameCategory(Product a, Product b) {
        return a.getCategory() != null && a.getCategory().equalsIgnoreCase(b.getCategory());
    }

    private boolean isAmOnly(Product product) {
        return isCategory(product, "Sunscreen");
    }

    private boolean isCategory(Product product, String category) {
        return product.getCategory() != null && product.getCategory().equalsIgnoreCase(category);
    }
}
