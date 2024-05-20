package ru.vilas.sewing.service;

import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Task;

import java.util.List;

public interface TaskService {
    List<Task> getTasksByCategory(Category category);

    Task getTaskById(Long id);
}
