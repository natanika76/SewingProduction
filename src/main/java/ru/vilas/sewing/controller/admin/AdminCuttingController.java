package ru.vilas.sewing.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.ChartCuttingDto;
import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.CustomerRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.CustomerService;
import ru.vilas.sewing.service.OperationsListSpecialService;
import ru.vilas.sewing.service.admin.AdminCuttingService;
import ru.vilas.sewing.service.admin.AdminWarehouseService;

import java.time.LocalDate;
import java.util.*;



@Controller
@RequestMapping("/admin/cuttingProcess")
public class AdminCuttingController {
    private final AdminCuttingService adminCuttingService;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;

    private final OperationsListSpecialService operationsListSpecialService;


    public AdminCuttingController(AdminCuttingService adminCuttingService, WarehouseRepository warehouseRepository, CustomerRepository customerRepository, OperationsListSpecialService operationsListSpecialService) {

        this.adminCuttingService = adminCuttingService;
        this.warehouseRepository = warehouseRepository;
        this.customerRepository = customerRepository;
        this.operationsListSpecialService = operationsListSpecialService;

    }

    @GetMapping
    public String cuttingProcess(Model model,
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


        return "admin/cuttingProcess";
    }

    @GetMapping("/show/{id}")
    public String showCuttingProcess(@PathVariable Long id,Model model,
                                     @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                     @RequestParam(name = "seamstress", required = false) Long seamstressId) {

        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = warehouseRepository.findEndWorkById(id);
        }
        if (startDate == null) {
            startDate = warehouseRepository.findStartWorkById(id);
        }

        // инициализируем  поля при первой загрузки формы
        if(seamstressId == null){
            seamstressId = 0L;
        }
        //********************************************************************************
        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(id);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName = warehouseRepository.findCategoryById(id).getName();
        String materialName = warehouseRepository.findNameMaterialById(id);
        WarehouseDto warehouseDto = operationsListSpecialService.showCuttingWarehouse(id);
        List<SizeByDateDto> sizeByDateDto = adminCuttingService.showCuttingProcess(id,startDate,endDate);
        List<User> seamstresses = adminCuttingService.showSeamstress(id);
        List<ChartCuttingDto> chartCuttingDto = adminCuttingService.showChartSeamstress(id,startDate,endDate,seamstressId);

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


            return "admin/showCuttingProcess";

    }
}