package ru.vilas.sewing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.SeamstressDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.service.CategoryService;
import ru.vilas.sewing.service.OperationDataService;
import ru.vilas.sewing.service.TaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getAllCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);
        model.addAttribute("requestURI", "/categories");
        return "categories";
    }

    @GetMapping("/{categoryId}")
    public String getCategoryDetails(@PathVariable Long categoryId, Model model) {
        Category category = categoryService.getCategoryById(categoryId);
        model.addAttribute("category", category);
        return "categoryDetails";
    }
}


