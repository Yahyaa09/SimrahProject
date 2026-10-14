package com.project.RoutineBuilder.repository;

import com.project.RoutineBuilder.entity.ClashRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClashRuleRepository extends JpaRepository<ClashRule, Long> {

    List<ClashRule> findAll();
}
