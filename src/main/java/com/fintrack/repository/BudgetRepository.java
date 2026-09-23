package com.fintrack.repository;

import com.fintrack.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fintrack.entity.User;
import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserAndMonth(User user, YearMonth month);
}
