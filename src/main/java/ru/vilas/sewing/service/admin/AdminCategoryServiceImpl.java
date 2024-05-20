package ru.vilas.sewing.service.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;


    public AdminCategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    @Override
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if(category != null){
            category.setActive(false);
            categoryRepository.save(category);
        }
    }
}

