package com.fintrack.service;

import com.fintrack.dto.ExpenseDto;
import com.fintrack.entity.Category;
import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.mapper.ExpenseMapper;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private BudgetService budgetService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Category category;
    private Expense expense;
    private ExpenseDto expenseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Food");

        expense = new Expense();
        expense.setId(10L);
        expense.setUser(user);
        expense.setCategory(category);
        expense.setAmount(50.0);
        expense.setTitle("Lunch");
        expense.setDate(LocalDate.now());

        expenseDto = new ExpenseDto();
        expenseDto.setId(10L);
        expenseDto.setAmount(50.0);
        expenseDto.setTitle("Lunch");
        expenseDto.setCategoryName("Food");
        expenseDto.setDate(LocalDate.now());
    }

    @Test
    void testGetExpenseById_Success() {
        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);

        ExpenseDto result = expenseService.getExpenseById(10L, "user@example.com");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Lunch", result.getTitle());
    }

    @Test
    void testGetExpenseById_NotFound() {
        when(expenseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            expenseService.getExpenseById(10L, "user@example.com")
        );
    }

    @Test
    void testGetExpenseById_AccessDenied() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        expense.setUser(otherUser);

        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));

        assertThrows(BadRequestException.class, () -> 
            expenseService.getExpenseById(10L, "user@example.com")
        );
    }

    @Test
    void testCreateExpense_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(category));
        when(expenseMapper.toEntity(any(ExpenseDto.class))).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);

        ExpenseDto result = expenseService.createExpense(expenseDto, "user@example.com");

        assertNotNull(result);
        assertEquals("Lunch", result.getTitle());
    }

    @Test
    void testGetExpensesFiltered_Pagination() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUser(user)).thenReturn(Collections.singletonList(expense));
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);

        List<ExpenseDto> result = expenseService.getExpensesFiltered(
                "user@example.com", null, null, null, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lunch", result.get(0).getTitle());
    }
}
