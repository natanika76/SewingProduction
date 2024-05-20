package ru.vilas.sewing.controller.admin;

import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.TaskTypes;
import ru.vilas.sewing.dto.WorkedByDate;
import ru.vilas.sewing.dto.WorkedDto;
import ru.vilas.sewing.dto.WorkedOperationDto;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.repository.OperationDataRepository;
import ru.vilas.sewing.service.*;
import ru.vilas.sewing.service.admin.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/operations")
public class AdminOperationController {
    private final CustomerService customerService;
    private final AdminOperationService operationService;

    private final CategoryService categoryService;

    private final SeamstressService userService;

    private final AdminCategoryService adminCategoryService;

    private final AdminTaskService adminTaskService;

    private final SeamstressService seamstressService;
    private final AdminWarehouseService adminWarehouseService;
    private final OperationDataService operationDataService;
    private final OperationsListSpecialServiceImpl operationsListSpecialService;
    private final OperationDataRepository operationDataRepository;

    public AdminOperationController(CustomerService customerService, AdminOperationService operationService, CategoryService categoryService, SeamstressService userService, AdminCategoryService adminCategoryService, AdminTaskService adminTaskService, SeamstressService seamstressService, AdminWarehouseService adminWarehouseService, OperationDataService operationDataService, OperationsListSpecialServiceImpl operationsListSpecialService, OperationDataRepository operationDataRepository) {
        this.customerService = customerService;
        this.operationService = operationService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.adminCategoryService = adminCategoryService;
        this.adminTaskService = adminTaskService;
        this.seamstressService = seamstressService;
        this.adminWarehouseService = adminWarehouseService;
        this.operationDataService = operationDataService;
        this.operationsListSpecialService = operationsListSpecialService;
        this.operationDataRepository = operationDataRepository;
    }

    @GetMapping
    public String showOperationsPage(Model model,
                 @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                 @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                 @RequestParam(name = "customer", required = false) Long customerId,
                 @RequestParam(name = "category", required = false) Long categoryId,
                 @RequestParam(name = "seamstress", required = false) Long seamstressId){

        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        //Собираем лист категорий в зависимости от фильтров
        List<Category> allCategories = categoryService.getAllCategories();
        List<Category> categories = new ArrayList<>();
        if ((customerId == null || customerId == 0) && (categoryId == null || categoryId == 0)) {
            categories = allCategories;
        } else if (customerId != null && customerId != 0 && (categoryId == null || categoryId == 0)) {
            categories = allCategories.stream().filter(c -> Objects.equals(c.getCustomer().getId(), customerId)).toList();
        } else {
            categories.add(categoryService.getCategoryById(categoryId));
        }


        List<WorkedOperationDto> operations = operationService.findByCategoryListAndSeamstressIdAndDateBetween(categories, seamstressId, startDate, endDate);

        List<User> seamstresses = userService.getAllSeamstresses();
        List<Customer> customers = customerService.getAllCustomers();
        customers.sort(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER));

        model.addAttribute("dates", operationService.getDatesBetween(startDate, endDate));
        model.addAttribute("operations", operations);

        model.addAttribute("customers", customers);
        model.addAttribute("categories", allCategories);
        model.addAttribute("seamstresses", seamstresses);

        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSeamstressId", seamstressId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);


        return "admin/operationsList";
    }

    @GetMapping("/delete/{id}")
    public String deleteOperation(@PathVariable Long id) {
        operationService.deleteOperation(id);
        return "redirect:/admin/operations";
    }

    // Добавление задачи
    @GetMapping("/new")
    public String showOperationForm(Model model) {
        List<User> seamstresses = seamstressService.getAllSeamstresses();
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        List<Task> tasks = adminTaskService.getAllTasks();
        List<String> nameMaterials = adminWarehouseService.showNameMaterial();

        model.addAttribute("nameMaterials", nameMaterials);
        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("categories", categories);
        model.addAttribute("tasks", tasks);
        model.addAttribute("operationData", new OperationData());
        return "admin/addOperation";
    }

    @PostMapping("/new")
    public String addNewOperation(@ModelAttribute("operationData") @Valid OperationData operationData,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Если есть ошибки валидации, возвращаем пользователя на форму с сообщениями об ошибках
            return "admin/addOperation";
        }

        // Сохраняем выполненную задачу в базе данных
        operationService.saveOperationData(operationData);

        // Перенаправляем пользователя на страницу с выполненными задачами
        return "redirect:/admin/operations";
    }

    @GetMapping("/edit/{id}")
    public String editOperation(@PathVariable Long id, Model model) {
        OperationData operationData = operationService.getOperationById(id);
        List<User> seamstresses = seamstressService.getAllSeamstresses();
        List<Task> tasks = adminTaskService.getAllTasks();
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        List<String> nameMaterials = adminWarehouseService.showNameMaterial();

        model.addAttribute("nameMaterials", nameMaterials);
        model.addAttribute("operationData", operationData);
        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categories);

        return "admin/editOperation";
    }

    @PostMapping("/edit/{id}")
    public String editOperation(@PathVariable Long id, @ModelAttribute OperationData operationData, BindingResult bindingResult, Model model) {
        // Валидация данных
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", bindingResult.getAllErrors());
            List<User> seamstresses = seamstressService.getAllSeamstresses();
            List<Task> tasks = adminTaskService.getAllTasks();
            List<Category> categories = adminCategoryService.getAllCategories()
                    .stream()
                    .filter(Category::isActive) // Фильтрация по активным категориям
                    .collect(Collectors.toList());
            List<String> nameMaterials = adminWarehouseService.showNameMaterial();

            model.addAttribute("nameMaterials", nameMaterials);
            model.addAttribute("operationData", operationData);
            model.addAttribute("seamstresses", seamstresses);
            model.addAttribute("tasks", tasks);
            model.addAttribute("categories", categories);

            return "admin/editOperation";
        }

        // Внесение изменений
        operationService.editOperation(id, operationData);

        return "redirect:/admin/operations"; // Перенаправление на страницу с выполненными задачами
    }

}

