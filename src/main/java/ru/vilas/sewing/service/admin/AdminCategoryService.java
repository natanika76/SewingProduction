package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.model.Category;

import java.util.List;

public interface AdminCategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    void saveCategory(Category category);

    void deleteCategory(Long id);
}
