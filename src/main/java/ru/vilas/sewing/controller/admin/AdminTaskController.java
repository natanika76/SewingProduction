package ru.vilas.sewing.controller.admin;

import jakarta.validation.Valid;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.service.admin.AdminCategoryService;
import ru.vilas.sewing.service.admin.AdminTaskService;
import ru.vilas.sewing.model.Category;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    private final AdminCategoryService adminCategoryService;

    public AdminTaskController(AdminTaskService adminTaskService, AdminCategoryService adminCategoryService, ConversionService conversionService) {
        this.adminTaskService = adminTaskService;
        this.adminCategoryService = adminCategoryService;
    }


    @GetMapping
    public String getAllTasks(Model model) {
        List<Task> tasks = adminTaskService.getAllTasks();
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);
        model.addAttribute("tasks", tasks);
        return "admin/taskList";
    }

    @GetMapping("/category/{categoryId}")
    public String getTasksByCategory(@PathVariable Long categoryId, Model model) {
        List<Task> tasks;
        if (categoryId != null) {
            tasks = adminTaskService.getTasksByCategoryId(categoryId);
        } else {
            tasks = adminTaskService.getAllTasks();
        }

        int totalTimeInSeconds = tasks.stream()
                .mapToInt(Task::getTimeInSeconds)
                .sum();

        BigDecimal totalCost = tasks.stream()
                .map(Task::getCostPerPiece)
                .map(cost -> cost.setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("tasks", tasks);
        model.addAttribute("totalTimeInSeconds", totalTimeInSeconds);
        model.addAttribute("totalCost", totalCost);
        return "admin/fragments/tasksTable"; // Возвращаем только часть HTML для таблицы задач
    }

    // Добавление задачи
    @GetMapping("/new")
    public String showTaskForm(Model model) {
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);
        model.addAttribute("task", new Task());
        return "admin/addTask";
    }


    @PostMapping("/new")
    public String addTask(@Valid @ModelAttribute Task task, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Обработка ошибок валидации
            model.addAttribute("validationErrors", bindingResult.getAllErrors());
            List<Category> categories = adminCategoryService.getAllCategories()
                    .stream()
                    .filter(Category::isActive) // Фильтрация по активным категориям
                    .collect(Collectors.toList());
            model.addAttribute("categories", categories);
            model.addAttribute("task", task);
            return "admin/addTask";
        }
        adminTaskService.saveTask(task);

        return "redirect:/admin/tasks";
    }

    @GetMapping("/edit/{taskId}")
    public String editTask(@PathVariable Long taskId, Model model) {
        Task task = adminTaskService.getTaskById(taskId);
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);
        model.addAttribute("task", task);
        return "admin/editTask";
    }

    @PostMapping("/edit/{taskId}")
    public String updateTask(@PathVariable Long taskId, @ModelAttribute Task updatedTask) {
        Task existingTask = adminTaskService.getTaskById(taskId);

        // Обновление значений
        existingTask.setName(updatedTask.getName());
        existingTask.setTimeInSeconds(updatedTask.getTimeInSeconds());
        existingTask.setCostPerPiece(updatedTask.getCostPerPiece());
        existingTask.setNormPerShift(updatedTask.getNormPerShift());
        existingTask.setCategory(updatedTask.getCategory());
        existingTask.setTaskType(updatedTask.getTaskType());

        // Сохранение обновленной задачи
        adminTaskService.saveTask(existingTask);

        return "redirect:/admin/tasks";
    }

    // Удаление задачи
    @GetMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId) {
        adminTaskService.deleteTask(taskId);
        return "redirect:/admin/tasks";
    }
}

