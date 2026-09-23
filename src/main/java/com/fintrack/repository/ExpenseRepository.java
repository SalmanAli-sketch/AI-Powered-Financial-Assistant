package com.fintrack.repository;

import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);
    List<Expense> findByUserAndCategory_Name(User user, String categoryName);
    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}
