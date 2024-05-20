package ru.vilas.sewing.service;

import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);

    void saveCategory(Category category);

    void deleteCategory(Long id);
}

