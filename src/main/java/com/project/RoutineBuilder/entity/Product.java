package com.project.RoutineBuilder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    private Integer viscosityRank;

    private String activeIngredient;

    private String targetConcern;

    /** Full INCI-style ingredient list, used for allergy screening. */
    @Column(length = 1000)
    private String ingredients;

    /** Alternative products are only used as allergy-safe substitutes, never recommended by default. */
    @JsonIgnore
    private Boolean alternative;
}
