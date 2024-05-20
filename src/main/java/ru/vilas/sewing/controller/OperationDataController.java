package ru.vilas.sewing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.InOperationDto;
import ru.vilas.sewing.dto.OperationDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.repository.CategoryRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.CategoryService;
import ru.vilas.sewing.service.CustomUserDetailsService;
import ru.vilas.sewing.service.OperationDataService;
import ru.vilas.sewing.service.TaskService;
import ru.vilas.sewing.service.admin.AdminWarehouseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static ru.vilas.sewing.dto.TaskTypes.*;

@Controller
@RequestMapping("/operations")
public class  OperationDataController {

    private final OperationDataService operationDataService;
    private final CategoryService categoryService;
    private final TaskService taskService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AdminWarehouseService adminWarehouseService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public OperationDataController(OperationDataService operationDataService, CategoryService categoryService, TaskService taskService, CustomUserDetailsService customUserDetailsService, AdminWarehouseService adminWarehouseService, CategoryRepository categoryRepository) {
        this.operationDataService = operationDataService;
        this.categoryService = categoryService;
        this.taskService = taskService;
        this.customUserDetailsService = customUserDetailsService;
        this.adminWarehouseService = adminWarehouseService;

        this.categoryRepository = categoryRepository;
    }

//    @PostMapping("/save")
//    public String saveOperationData(@ModelAttribute OperationData operationData) {
//        operationDataService.saveOperationData(operationData);
//        return "redirect:/tasks/" + operationData.getTask().getId();
//    }

    @GetMapping("/{operationDataId}")
    public String getOperationDataDetails(@PathVariable Long operationDataId, Model model) {
        OperationData operationData = operationDataService.getOperationDataById(operationDataId);
        model.addAttribute("operationData", operationData);
        return "operationDataDetails";
    }
    @GetMapping("/material/{categoryId}")
    public String getMaterialDetails(@PathVariable("categoryId") Long categoryId, Model model) {
    List<String> nameMaterials = adminWarehouseService.showNameMaterialByCategory(categoryId);
    if (nameMaterials.size() == 0) {
        nameMaterials.add("Материал не задан");
    }



    model.addAttribute("categoryId", categoryId);
    model.addAttribute("nameMaterials", nameMaterials);
        return "material";
}
    @GetMapping("/category/{categoryId}")
    public String getTasksForCategory(@PathVariable Long categoryId, Model model, Authentication authentication, //){
                                     @RequestParam(required = false) String nameMaterial){

        Object principal =  authentication.getPrincipal();
        String userName = null;
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }

        // Получаем идентификатор пользователя
        Long currentUserId = customUserDetailsService.getUserIdByUsername(userName);

        Category category = categoryService.getCategoryById(categoryId);
        List<Task> tasks = taskService.getTasksByCategory(category);


        List<OperationDto> operationDtos = tasks.stream()
                .map(task -> operationDataService.convertToOperationDto(task, currentUserId, nameMaterial))
                .toList();

      // Integer sumTimes = operationDtos.stream().map(OperationDto::getTimeInSeconds).mapToInt(Integer::intValue).sum();


        List<OperationDto> quantitativeTasks = operationDtos.stream().filter(t -> t.getTaskType().equals(QUANTITATIVE)).toList();
        List<OperationDto> hourlyTasks = operationDtos.stream().filter(t -> t.getTaskType().equals(HOURLY)).toList();
        List<OperationDto> packagingTasks = operationDtos.stream().filter(t -> t.getTaskType().equals(PACKAGING)).toList();

        BigDecimal hourlyRate = customUserDetailsService.getCurrentUser().getHourlyRate();
        String categoryName = categoryRepository.findNameById(categoryId);

        model.addAttribute("categoryName", categoryName);
        model.addAttribute("nameMaterial", nameMaterial);
        model.addAttribute("quantitativeTasks", quantitativeTasks);
        model.addAttribute("hourlyTasks", hourlyTasks);
        model.addAttribute("packagingTasks", packagingTasks);
        model.addAttribute("hourlyRate", hourlyRate);

        return "tasks";
    }
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOperations(@RequestBody List<InOperationDto> inOperationDtos){
        operationDataService.saveOrUpdateOperations(inOperationDtos);
    }

}
