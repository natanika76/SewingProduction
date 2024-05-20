package ru.vilas.sewing.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.CustomerRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.CustomerService;
import ru.vilas.sewing.service.OperationsListSpecialService;
import ru.vilas.sewing.service.admin.AdminCuttingService;
import ru.vilas.sewing.service.admin.AdminShipmentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/shipmentProcess")
public class AdminShipmentController {
    private final CustomerService customerService;
    private  final CustomerRepository customerRepository;
    private final AdminCuttingService adminCuttingService;
    private final WarehouseRepository warehouseRepository;
    private final OperationsListSpecialService operationsListSpecialService;
    private final AdminShipmentService adminShipmentService;

    public AdminShipmentController(CustomerService customerService, CustomerRepository customerRepository, AdminCuttingService adminCuttingService, WarehouseRepository warehouseRepository, OperationsListSpecialService operationsListSpecialService, AdminShipmentService adminShipmentService) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.adminCuttingService = adminCuttingService;
        this.warehouseRepository = warehouseRepository;
        this.operationsListSpecialService = operationsListSpecialService;
        this.adminShipmentService = adminShipmentService;
    }


    @GetMapping
    public String shipmentProcess(Model model,
                                  @RequestParam(name = "customer", required = false) Long customerId,
                                  @RequestParam(name = "category", required = false) Long categoryId,
                                  @RequestParam(name = "nameMaterial", required = false) String selectedNameMaterial) {
        // инициализируем поля при первой загрузки формы
        if(customerId == null){
            customerId = 0L;
        }
        if(categoryId == null){
            categoryId = 0L;
        }
        if (selectedNameMaterial == null) {
            selectedNameMaterial = "";
        }
        List<Customer> customers = warehouseRepository.findDistinctCustomersFromNonArchivedWarehouses();
        customers.sort(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER));
        List<Category> categories = warehouseRepository.findCategoriesByCustomerId(customerId);
        List<String> nameMaterials = warehouseRepository.findNameMaterialByCustomerIdAndCategoryId(customerId, categoryId);
        List<WarehouseDto> warehouseDtos = adminShipmentService.showAllActiveTasksCutEndShipment(customerId, categoryId, selectedNameMaterial);

        model.addAttribute("nameMaterials", nameMaterials);
        model.addAttribute("categories", categories);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedNameMaterial",selectedNameMaterial);
        model.addAttribute("warehousesDtos", warehouseDtos);

        return "admin/shipmentProcess";
    }

    @GetMapping("/add/{id}")
    public String addShipmentProcess(@PathVariable Long id, Model model,
                                     @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Если параметры не переданы, устанавливаем значения даты по умолчанию
        if (date == null) {
            date = LocalDate.now();
        }
        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(id);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName =warehouseRepository.findCategoryById(id).getName();
        String materialName = warehouseRepository.findNameMaterialById(id);
        WarehouseDto warehouseDto = adminShipmentService.showShipmentWarehouse(id);
        List<SizeByDateDto> sizeByDateDto = adminShipmentService.addShipmentProcess(id);

        model.addAttribute("sizeByDateDto", sizeByDateDto);
        model.addAttribute("warehouseDto", warehouseDto);
        model.addAttribute("date", date);
        model.addAttribute("customersName", customersName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("materialName", materialName);
        model.addAttribute("warehouse", id);
        return "admin/addShipmentProcess";
    }
    @PostMapping("/add")
    public String saveAddShipmentProcess(Model model,
                                         @ModelAttribute("warehouse") Long warehouse,
                                         @RequestParam("sizeByDateIds") List<Long> sizeByDateIds,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                         @RequestParam("userInput") List<Integer> userInputs){


            List<CuttingDto> cuttingDtos = new ArrayList<>();
            for (int i = 0; i < userInputs.size(); i++) {
                CuttingDto cuttingDto = new CuttingDto();
                Integer userInput = userInputs.get(i);
                if (userInput == null){
                    userInput = 0;
                }
                cuttingDto.setQuantity(userInput);
                cuttingDto.setDateWork(date);
                cuttingDto.setWarehouseId(warehouse);
                cuttingDto.setSizeByDateId(sizeByDateIds.get(i));
                cuttingDtos.add(cuttingDto);
            }
            //boolean saveSuccessful = adminCuttingService.savePackagingFromDto(cuttingDtos);
            boolean saveSuccessful = adminShipmentService.saveShipmentFromDto(cuttingDtos);

            if (saveSuccessful) {
                // Если сохранение прошло успешно, добавляем сообщение в модель
                model.addAttribute("message", "Данные успешно сохранены");
            } else {
                // Если сохранение не удалось, можно добавить соответствующее сообщение об ошибке
                model.addAttribute("message", "Ошибка при сохранении данных");
            }

            return "admin/addShipmentProcess";
    }
    @GetMapping("/show/{id}")
    public String showAnaliticsShipmentProcess(@PathVariable Long id,Model model){

        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(id);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName =warehouseRepository.findCategoryById(id).getName();
        String materialName = warehouseRepository.findNameMaterialById(id);
        WarehouseDto warehouseDto = adminShipmentService.showShipmentWarehouse(id);
        List<AnalyticsDto> analyticsDtos = adminShipmentService.showAnalytics(id);
        AnalyticsDto totalAnalyticsDto = adminShipmentService.showTotalAnalytics(id);
        Integer workingPeriod = adminShipmentService.workingPeriod(id);
        Integer workingPeriodCurrentDate = adminShipmentService.periodCurrentDate(id);
        Integer shipmentForecast = adminShipmentService.shipmentForecastForTheEndDate(id);
        Integer forecastDays = adminShipmentService.forecastOfNumberDays(id);
        Integer recommendedShipmentRatePerDay = adminShipmentService.recommendedShipmentRatePerDay(id);
        List<AnalyticsDto> chartShowAnalytics = adminShipmentService.chartShowAnalytics(id);

        model.addAttribute("chartShowAnalytics",chartShowAnalytics);
        model.addAttribute("recommendedShipmentRatePerDay", recommendedShipmentRatePerDay);
        model.addAttribute("shipmentForecast", shipmentForecast);
        model.addAttribute("forecastDays", forecastDays);
        model.addAttribute("workingPeriodCurrentDate", workingPeriodCurrentDate);
        model.addAttribute("workingPeriod", workingPeriod);
        model.addAttribute("totalAnalyticsDto", totalAnalyticsDto);
        model.addAttribute("warehouseDto", warehouseDto);
        model.addAttribute("analyticsDtos", analyticsDtos);
        model.addAttribute("customersName", customersName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("materialName", materialName);
        model.addAttribute("warehouse", id);
        return "admin/showAnalyticsShipmentProcess";
    }
    @GetMapping("/chart/{warehouseId}")
    public String chartShipmentAnalyticsProcess(@PathVariable Long warehouseId,Model model,
                                                @RequestParam(name = "seamstress", required = false) Long seamstressId){
        if(seamstressId == null){
            seamstressId = 0L;
        }
       List<User> seamstresses = adminShipmentService.showSeamstressFull(warehouseId);
       LocalDate startDate = warehouseRepository.findStartWorkById(warehouseId);
       LocalDate endDate = warehouseRepository.findEndWorkById(warehouseId);
       LocalDate  currentDate = LocalDate.now();
       // Проверяем, что endDate не равен null и если конечная дата больше текущей, то конечную дату приравниваем к текущий (нельзя вводить будущую дату)
        if (endDate != null && endDate.compareTo(currentDate) > 0) {
            endDate = currentDate;
        }

        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(warehouseId);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName =warehouseRepository.findCategoryById(warehouseId).getName();
        String materialName = warehouseRepository.findNameMaterialById(warehouseId);
        WarehouseDto warehouseDto = adminShipmentService.showShipmentWarehouse(warehouseId);
        //Для графиков
        List<ChartTaskDto> chartTaskDtos = adminShipmentService.showChartTaskQuantitative(warehouseId, seamstressId);
        List<ChartTaskDto> chartTaskDtosHourly = adminShipmentService.showChartTaskHourly(warehouseId, seamstressId);
        List<ChartTaskDto> chartTaskDtosPackaging = adminShipmentService.showChartTaskPackaging(warehouseId,seamstressId);

        model.addAttribute("selectedSeamstressId", seamstressId);
        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("chartTaskDtos", chartTaskDtos);
        model.addAttribute("customersName", customersName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("materialName", materialName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("warehouseDto", warehouseDto);
        model.addAttribute("chartTaskDtosHourly", chartTaskDtosHourly);
        model.addAttribute("chartTaskDtosPackaging", chartTaskDtosPackaging);

        return "admin/chartShipmentAnalyticsProcess";
    }

}