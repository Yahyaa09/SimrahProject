package com.project.RoutineBuilder.model;

import java.util.List;

/** Common skincare allergens and the ingredient keywords that indicate them. */
public enum Allergen {

    FRAGRANCE("Fragrance / Parfum", "Added scents, a leading cause of contact dermatitis",
            List.of("fragrance", "parfum", "linalool", "limonene"),
            List.of("perfume", "scent", "scented")),
    ESSENTIAL_OILS("Essential Oils", "Lavender, citrus, tea tree and other plant oils",
            List.of("essential oil", "lavender oil", "citrus oil", "tea tree oil", "peppermint oil", "eucalyptus oil"),
            List.of("lavender", "tea tree", "citrus")),
    TREE_NUTS("Tree Nuts", "Walnut shell, almond oil, shea butter, argan oil",
            List.of("walnut", "almond", "shea", "argan", "macadamia", "juglans"),
            List.of("nut", "nuts", "tree nut")),
    SALICYLATES("Salicylates / Aspirin", "Salicylic acid (BHA), willow bark",
            List.of("salicylic acid", "salicylate", "willow bark"),
            List.of("aspirin", "bha", "salicylic")),
    SULFATES("Sulfates (SLS / SLES)", "Harsh foaming cleansers",
            List.of("sodium lauryl sulfate", "sodium laureth sulfate"),
            List.of("sls", "sles", "sulfate", "sulphate")),
    PARABENS("Parabens", "Methylparaben, propylparaben preservatives",
            List.of("paraben"),
            List.of("parabens")),
    PRESERVATIVES_MI("Methylisothiazolinone (MI)", "Common preservative allergen",
            List.of("methylisothiazolinone", "methylchloroisothiazolinone"),
            List.of("mi", "mci", "isothiazolinone")),
    LANOLIN("Lanolin", "Wool-derived moisturising wax",
            List.of("lanolin"),
            List.of("wool", "wool wax")),
    SOY("Soy", "Soybean oil and extracts",
            List.of("soy", "glycine soja"),
            List.of("soya", "soybean")),
    DRYING_ALCOHOL("Drying Alcohol", "Alcohol denat., ethanol",
            List.of("alcohol denat", "denatured alcohol", "ethanol", "sd alcohol"),
            List.of("alcohol"));

    private final String label;
    private final String description;
    private final List<String> ingredientKeywords;
    private final List<String> aliases;

    Allergen(String label, String description, List<String> ingredientKeywords, List<String> aliases) {
        this.label = label;
        this.description = description;
        this.ingredientKeywords = ingredientKeywords;
        this.aliases = aliases;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getIngredientKeywords() {
        return ingredientKeywords;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
