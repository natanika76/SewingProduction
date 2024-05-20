package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.model.Task;

import java.util.List;

public interface AdminTaskService {
    List<Task> getAllTasks();

    void saveTask(Task task);

    void deleteTask(Long taskId);

    Task getTaskById(Long taskId);

    List<Task> getTasksByCategoryId(Long categoryId);
}
