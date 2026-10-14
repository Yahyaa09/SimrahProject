package com.project.RoutineBuilder.service;

import com.project.RoutineBuilder.dto.ClashResponse;
import com.project.RoutineBuilder.dto.RoutineResponse;
import com.project.RoutineBuilder.entity.Product;
import com.project.RoutineBuilder.repository.ProductRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutineBuilderService {

    private final ProductRepository productRepository;
    private final MatchClashService matchClashService;

    public RoutineResponse buildRoutine(String primaryConcern) {
        List<Product> products = productRepository.findByTargetConcern(primaryConcern);
        products.sort(Comparator.comparing(Product::getViscosityRank, Comparator.nullsLast(Integer::compareTo)));

        ClashResponse safetyCheck = matchClashService.validateRoutine(products);
        return new RoutineResponse(products, safetyCheck);
    }
}
