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
                    product("Salicylic Acid Body Wash", "Cleanser", 1, "Salicylic Acid", "Body Acne",
                            "Water, Sodium Laureth Sulfate, Salicylic Acid, Glycerin, Fragrance"),
                    product("Walnut Shell Body Scrub", "Exfoliant", 2, "Physical Scrub", "Body Acne",
                            "Juglans Regia (Walnut) Shell Powder, Glycerin, Shea Butter, Fragrance"),
                    product("Niacinamide 10% Serum", "Serum", 3, "Niacinamide", "Body Acne",
                            "Water, Niacinamide, Zinc PCA, Phenoxyethanol"),
                    product("Oil-Free Gel Moisturizer", "Moisturizer", 4, "Ceramides", "Body Acne",
                            "Water, Ceramide NP, Hyaluronic Acid, Methylparaben"),
                    alternative("Benzoyl Peroxide 5% Body Wash", "Cleanser", 1, "Benzoyl Peroxide", "Body Acne",
                            "Water, Benzoyl Peroxide, Glycerin, Cocamidopropyl Betaine"),
                    alternative("Jojoba Bead Body Polish", "Exfoliant", 2, "Physical Scrub", "Body Acne",
                            "Jojoba Esters, Glycerin, Cellulose"),
                    alternative("Fragrance-Free Ceramide Lotion", "Moisturizer", 4, "Ceramides", "Body Acne",
                            "Water, Ceramide NP, Glycerin, Phenoxyethanol"),

                    // Hyperpigmentation -> safe routine
                    product("Gentle Hydrating Cleanser", "Cleanser", 1, "Glycerin", "Hyperpigmentation",
                            "Water, Glycerin, Lanolin, Glycine Soja (Soybean) Oil"),
                    product("Vitamin C Brightening Serum", "Serum", 3, "Vitamin C", "Hyperpigmentation",
                            "Water, Ascorbic Acid, Ferulic Acid, Alcohol Denat., Fragrance"),
                    product("Barrier Repair Moisturizer", "Moisturizer", 4, "Ceramides", "Hyperpigmentation",
                            "Water, Ceramide NP, Shea Butter, Lavender Oil"),
                    product("Mineral Sunscreen SPF 50", "Sunscreen", 5, "Zinc Oxide", "Hyperpigmentation",
                            "Zinc Oxide, Caprylic/Capric Triglyceride, Tocopherol"),
                    alternative("Micellar Cleansing Water", "Cleanser", 1, "Glycerin", "Hyperpigmentation",
                            "Water, Glycerin, Poloxamer 184"),
                    alternative("Alpha Arbutin 2% Serum", "Serum", 3, "Alpha Arbutin", "Hyperpigmentation",
                            "Water, Alpha Arbutin, Hyaluronic Acid"),
                    alternative("Squalane Barrier Cream", "Moisturizer", 4, "Squalane", "Hyperpigmentation",
                            "Squalane, Glycerin, Ceramide NP"),

                    // Strawberry Skin -> triggers the Glycolic Acid + Retinol clash
                    product("Glycolic Acid Body Cleanser", "Cleanser", 1, "Glycolic Acid", "Strawberry Skin",
                            "Water, Glycolic Acid, Sodium Lauryl Sulfate, Citrus Oil"),
                    product("Retinol Body Serum", "Serum", 3, "Retinol", "Strawberry Skin",
                            "Water, Retinol, Squalane, Methylisothiazolinone"),
                    product("Urea 10% Body Lotion", "Moisturizer", 4, "Urea", "Strawberry Skin",
                            "Water, Urea, Lanolin, Sweet Almond Oil"),
                    alternative("Lactic Acid Body Wash", "Cleanser", 1, "Lactic Acid", "Strawberry Skin",
                            "Water, Lactic Acid, Cocamidopropyl Betaine, Glycerin"),
                    alternative("Colloidal Oatmeal Lotion", "Moisturizer", 4, "Colloidal Oatmeal", "Strawberry Skin",
                            "Water, Avena Sativa Kernel Flour, Glycerin, Dimethicone")
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

    private Product product(String name, String category, int rank, String active, String concern,
                            String ingredients) {
        return new Product(null, name, category, rank, active, concern, ingredients, false);
    }

    private Product alternative(String name, String category, int rank, String active, String concern,
                                String ingredients) {
        return new Product(null, name, category, rank, active, concern, ingredients, true);
    }

    private ClashRule rule(String one, String two, String message) {
        return new ClashRule(null, one, two, message);
    }
}
