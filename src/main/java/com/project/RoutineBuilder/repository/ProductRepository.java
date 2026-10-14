package com.project.RoutineBuilder.repository;

import com.project.RoutineBuilder.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByTargetConcern(String targetConcern);
}
