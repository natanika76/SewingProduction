package ru.vilas.sewing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.service.CategoryService;
import ru.vilas.sewing.service.TaskService;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CategoryService categoryService;

    @Autowired
    public TaskController(TaskService taskService, CategoryService categoryService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
    }



//    @GetMapping("/category/{categoryId}")
//    public String getTasksForCategory(@PathVariable Long categoryId, Model model) {
//        List<Task> tasks = taskService.getTasksForCategory(categoryId);
//        List<OperationData> operationsToday = operationService.getOperationsForToday();
//
//        model.addAttribute("tasks", tasks);
//        model.addAttribute("operationsToday", operationsToday);
//
//        return "tasks/categoryTasks";
//    }

//    @PostMapping("/saveOperations")
//    @ResponseBody
//    public String saveOperations(@RequestBody List<OperationDataDto> operations) {
//        operationService.saveOperationsForToday(operations);
//        return "Changes saved!";
//    }

    @GetMapping("/{taskId}")
    public String getTaskDetails(@PathVariable Long taskId, Model model) {
        Task task = taskService.getTaskById(taskId);
        model.addAttribute("task", task);
        return "taskDetails";
    }
}
