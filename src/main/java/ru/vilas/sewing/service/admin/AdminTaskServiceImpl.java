package ru.vilas.sewing.service.admin;

import org.springframework.stereotype.Service;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTaskServiceImpl implements AdminTaskService {

    private final TaskRepository taskRepository;

    public AdminTaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getAllTasks() {

        return taskRepository.findAll().stream()
                .filter(Task::getActive)
                .collect(Collectors.toList());
    }

    @Override
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if(task != null){
            task.setActive(false);
            taskRepository.save(task);
        }
    }

    @Override
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(new Task());
    }

    @Override
    public List<Task> getTasksByCategoryId(Long categoryId) {
        if (categoryId != null) {
            return taskRepository.findByCategoryIdOrderByName(categoryId).stream()
                    .filter(Task::getActive)
                    .collect(Collectors.toList());
        } else {
            return getAllTasks();
        }
    }


}
