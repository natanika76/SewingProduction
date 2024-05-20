package ru.vilas.sewing.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vilas.sewing.dto.EarningsDto;
import ru.vilas.sewing.dto.SeamstressDto;
import ru.vilas.sewing.dto.WorkedDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.service.CategoryServiceImpl;
import ru.vilas.sewing.service.OperationDataService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/workedout")
public class WorkedOutController {

    private final OperationDataService operationDataService;
    private final CategoryServiceImpl categoryService;

    public WorkedOutController(OperationDataService operationDataService, CategoryServiceImpl categoryService) {
        this.operationDataService = operationDataService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getSeparateEarningsReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "category", required = false) Long category,
            Model model) {

        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = LocalDate.now().with(DayOfWeek.THURSDAY);
        }
        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        List<Category> categories = categoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);


        List<WorkedDto> workedDtos = operationDataService.getWorkedDtosList(startDate, endDate);

        List<WorkedDto> workedDtosList;

        if (category == null) {
            workedDtosList = workedDtos.stream().filter(dto -> dto.getCategory().getId().equals(categories.get(0).getId()))
                    .collect(Collectors.toList());
        } else {
            workedDtosList = workedDtos.stream().filter(dto -> dto.getCategory().getId().equals(category))
                    .collect(Collectors.toList());
        }

        model.addAttribute("categories", categories);
        model.addAttribute("workedList", workedDtosList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedCategoryId", category);

        return "admin/workedOutReport";
    }
}
