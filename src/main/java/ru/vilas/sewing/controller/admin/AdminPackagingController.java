package ru.vilas.sewing.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vilas.sewing.dto.ChartCuttingDto;
import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.CustomerRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.OperationsListSpecialService;
import ru.vilas.sewing.service.admin.AdminCuttingService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/packagingProcess")
public class AdminPackagingController {
    private final WarehouseRepository warehouseRepository;
    private final AdminCuttingService adminCuttingService;
    private final CustomerRepository customerRepository;
    private final OperationsListSpecialService operationsListSpecialService;

    public AdminPackagingController(WarehouseRepository warehouseRepository, AdminCuttingService adminCuttingService, CustomerRepository customerRepository, OperationsListSpecialService operationsListSpecialService) {
        this.warehouseRepository = warehouseRepository;
        this.adminCuttingService = adminCuttingService;
        this.customerRepository = customerRepository;
        this.operationsListSpecialService = operationsListSpecialService;
    }

    @GetMapping
    public String packagingProcess(Model model,
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
        List<WarehouseDto> warehouses = adminCuttingService.showAllActiveTasksCut(customerId, categoryId, selectedNameMaterial);

        model.addAttribute("nameMaterials", nameMaterials);
        model.addAttribute("categories", categories);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedNameMaterial",selectedNameMaterial);

        return "admin/packagingProcess";
    }
    @GetMapping("/show/{warehouseId}")
    public String showCuttingProcess(@PathVariable Long warehouseId, Model model,
                                     @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                     @RequestParam(name = "seamstress", required = false) Long seamstressId) {

        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = warehouseRepository.findEndWorkById(warehouseId);
        }
        if (startDate == null) {
            startDate = warehouseRepository.findStartWorkById(warehouseId);
        }

        // инициализируем  поля при первой загрузки формы
        if(seamstressId == null){
            seamstressId = 0L;
        }
        //********************************************************************************
        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(warehouseId);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName = warehouseRepository.findCategoryById(warehouseId).getName();
        String materialName = warehouseRepository.findNameMaterialById(warehouseId);
        WarehouseDto warehouseDto = operationsListSpecialService.showCuttingWarehouse(warehouseId);
        List<SizeByDateDto> sizeByDateDto = adminCuttingService.showCuttingProcess(warehouseId,startDate,endDate);
        List<User> seamstresses = adminCuttingService.showSeamstress(warehouseId);
        List<ChartCuttingDto> chartCuttingDto = adminCuttingService.showChartPackagingSeamstress(warehouseId,startDate,endDate,seamstressId);

        //***************************************************************************

        model.addAttribute("customersName", customersName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("materialName", materialName);
        model.addAttribute("warehouseDto", warehouseDto);
        model.addAttribute("sizeByDateDto", sizeByDateDto);
        model.addAttribute("chartCuttingDto", chartCuttingDto);
        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("selectedSeamstressId", seamstressId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);


        return "admin/showPackagingProcess";

    }
}
