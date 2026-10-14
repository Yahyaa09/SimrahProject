package com.project.RoutineBuilder.config;

import com.project.RoutineBuilder.entity.ClashRule;
import com.project.RoutineBuilder.entity.Product;
import com.project.RoutineBuilder.repository.ClashRuleRepository;
import com.project.RoutineBuilder.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seeds demo products and clash rules when the tables are empty, so the app works
 * out of the box (H2) and on a fresh Supabase database without duplicating rows.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ClashRuleRepository clashRuleRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    // Body Acne -> intentionally triggers the Salicylic Acid + Physical Scrub clash
                    product("Salicylic Acid Body Wash", "Cleanser", 1, "Salicylic Acid", "Body Acne"),
                    product("Walnut Shell Body Scrub", "Exfoliant", 2, "Physical Scrub", "Body Acne"),
                    product("Niacinamide 10% Serum", "Serum", 3, "Niacinamide", "Body Acne"),
                    product("Oil-Free Gel Moisturizer", "Moisturizer", 4, "Ceramides", "Body Acne"),

                    // Hyperpigmentation -> safe routine
                    product("Gentle Hydrating Cleanser", "Cleanser", 1, "Glycerin", "Hyperpigmentation"),
                    product("Vitamin C Brightening Serum", "Serum", 3, "Vitamin C", "Hyperpigmentation"),
                    product("Barrier Repair Moisturizer", "Moisturizer", 4, "Ceramides", "Hyperpigmentation"),
                    product("Mineral Sunscreen SPF 50", "Sunscreen", 5, "Zinc Oxide", "Hyperpigmentation"),

                    // Strawberry Skin -> triggers the Glycolic Acid + Retinol clash
                    product("Glycolic Acid Body Cleanser", "Cleanser", 1, "Glycolic Acid", "Strawberry Skin"),
                    product("Retinol Body Serum", "Serum", 3, "Retinol", "Strawberry Skin"),
                    product("Urea 10% Body Lotion", "Moisturizer", 4, "Urea", "Strawberry Skin")
            ));
            log.info("Seeded demo products");
        }

        if (clashRuleRepository.count() == 0) {
            clashRuleRepository.saveAll(List.of(
                    rule("Salicylic Acid", "Physical Scrub",
                            "Clash: Salicylic Acid + Physical Scrub can over-exfoliate and irritate skin. Use them on alternate days."),
                    rule("Glycolic Acid", "Retinol",
                            "Clash: Glycolic Acid + Retinol together may cause redness and peeling. Use AHA in the AM and Retinol in the PM."),
                    rule("Vitamin C", "Retinol",
                            "Clash: Vitamin C + Retinol can destabilise each other. Use Vitamin C in the AM and Retinol in the PM."),
                    rule("Benzoyl Peroxide", "Retinol",
                            "Clash: Benzoyl Peroxide can deactivate Retinol. Do not layer them in the same routine.")
            ));
            log.info("Seeded demo clash rules");
        }
    }

    private Product product(String name, String category, int rank, String active, String concern) {
        return new Product(null, name, category, rank, active, concern);
    }

    private ClashRule rule(String one, String two, String message) {
        return new ClashRule(null, one, two, message);
    }
}
