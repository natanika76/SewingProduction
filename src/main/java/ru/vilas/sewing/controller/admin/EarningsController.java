package ru.vilas.sewing.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vilas.sewing.dto.EarningsDto;
import ru.vilas.sewing.dto.SeamstressDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.service.CategoryServiceImpl;
import ru.vilas.sewing.service.OperationDataService;
import ru.vilas.sewing.service.admin.AdminCustomerService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class EarningsController {

    private final OperationDataService operationDataService;
    private final CategoryServiceImpl categoryService;

    private final AdminCustomerService adminCustomerService;

    public EarningsController(OperationDataService operationDataService, CategoryServiceImpl categoryService, AdminCustomerService adminCustomerService) {
        this.operationDataService = operationDataService;
        this.categoryService = categoryService;
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public String getEarningsReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Если параметры не переданы, устанавливаем значения по умолчанию

        if (endDate == null) {
            endDate = LocalDate.now().with(DayOfWeek.THURSDAY);
        }

        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        List<String> headers = operationDataService.getDatesInPeriod(startDate, endDate);
        List<SeamstressDto> seamstressDtos = operationDataService.getSeamstressDtosList(startDate, endDate);

        seamstressDtos.forEach(seamstressDto -> System.out.println(seamstressDto.getEarnings()) );

        model.addAttribute("seamstressDtos", seamstressDtos);
        model.addAttribute("tableHeaders", headers);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/earningsReport";
    }

    @GetMapping("/earnings")
    public String getSeparateEarningsReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "category", required = false) Long category,
            @RequestParam(name = "customer", required = false) Long customerId,
            Model model) {

        // Если даты не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = LocalDate.now().with(DayOfWeek.THURSDAY);
        }
        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        // Получение списка всех категорий
        List<Category> allCategories = categoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());

        // Получение списка всех заказчиков
        List<Customer> customers = new ArrayList<>(adminCustomerService.getAllCustomers());

        List<Category> categoryList = new ArrayList<>();

        List<EarningsDto> earningsDtosList;

        // Создаем лист EarningsDto в зависимости от выставленного фильтра
        if ((category == null || category == 0) && (customerId == null || customerId == 0)) {
            earningsDtosList = operationDataService.getCommonEarningsDtosList(startDate, endDate);
        } else if ((category != null) && (category != 0)) {
            categoryList.add(categoryService.getCategoryById(category));
            earningsDtosList = operationDataService.getEarningsDtosList(startDate, endDate, categoryList);
        } else  {
            categoryList = allCategories.stream().filter(c -> Objects.equals(c.getCustomer().getId(), customerId)).toList();
            earningsDtosList = operationDataService.getEarningsDtosList(startDate, endDate, categoryList);
        }

        categoryList = null;

        BigDecimal salarySum = earningsDtosList.stream()
                .map(EarningsDto::getSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalAmountSum = earningsDtosList.stream()
                .map(EarningsDto::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);

        model.addAttribute("categories", allCategories);
        model.addAttribute("earningsList", earningsDtosList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedCategoryId", category);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("salarySum", salarySum);
        model.addAttribute("totalAmountSum", totalAmountSum);

        return "admin/fullEarningsReport";
    }

}
