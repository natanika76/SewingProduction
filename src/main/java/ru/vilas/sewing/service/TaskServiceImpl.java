package ru.vilas.sewing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.repository.TaskRepository;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Task;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getTasksByCategory(Category category) {

        return taskRepository.findByCategory(category).stream()
                .filter(Task::getActive)
                .collect(Collectors.toList());
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
}
