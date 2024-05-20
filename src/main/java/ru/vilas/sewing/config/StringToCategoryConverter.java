package ru.vilas.sewing.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.repository.CategoryRepository;

//@Component
public class StringToCategoryConverter implements Converter<String, Category> {

    private final CategoryRepository categoryRepository;

    public StringToCategoryConverter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category convert(String categoryId) {
        try {
            Long id = Long.parseLong(categoryId);
            return categoryRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Если не удается преобразовать в Long, попробуйте найти по имени
            return (Category) categoryRepository.findByName(categoryId).orElse(null);
        }
    }
}

