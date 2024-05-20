package ru.vilas.sewing.controller.admin;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.CuttingDto;
import ru.vilas.sewing.dto.ReportsDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.repository.CustomerRepository;
import ru.vilas.sewing.repository.CuttingRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.admin.AdminReportsService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminReportsController {
    private final AdminReportsService adminReportsService;
    private final WarehouseRepository warehouseRepository;
    private final CuttingRepository cuttingRepository;
    public AdminReportsController(AdminReportsService adminReportsService, WarehouseRepository warehouseRepository, CuttingRepository cuttingRepository) {
        this.adminReportsService = adminReportsService;
        this.warehouseRepository = warehouseRepository;

        this.cuttingRepository = cuttingRepository;
    }
    @GetMapping("/reports")
    public String reports() {  return "admin/reports"; }

    @GetMapping("/reportsCutting")
    public String reportsCutting() { return "admin/reportsCutting"; }

    @GetMapping("/editReportsCutting")
    public String editReportsCutting(Model model,
                                     @RequestParam(name = "customer", required = false) Long customerId,
                                     @RequestParam(name = "category", required = false) Long categoryId) {

        // инициализируем поля при первой загрузки формы
        if (customerId == null) {
            customerId = 0L;
        }
        if (categoryId == null) {
            categoryId = 0L;
        }
        List<Customer> customers = warehouseRepository.findDistinctCustomersFromNonArchivedWarehouses();
        customers.sort(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER));
        List<Category> categories = warehouseRepository.findCategoriesByCustomerId(customerId);

        List<Long> warehouseId = adminReportsService.getWarehouseId(customerId, categoryId);
        List<ReportsDto> reportsDtos = adminReportsService.getCuttingDtos(warehouseId);
        model.addAttribute("categories", categories);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("reportsDtos", reportsDtos);

        return "admin/editReportsCutting";
    }

        @GetMapping("/editReportsCuttingId/{cuttingId}")
        public String editReportsCuttingId (@PathVariable Long cuttingId, Model model){
        ReportsDto reportsDto = adminReportsService.editCutting(cuttingId);
        //**** Получение дат начала и конца периода *********************************************************
            LocalDate startWork = warehouseRepository.findStartWorkById(reportsDto.getWarehouseId());
            LocalDate endWork = warehouseRepository.findEndWorkById(reportsDto.getWarehouseId());

            model.addAttribute("startWork", startWork);
            model.addAttribute("endWork", endWork);
            model.addAttribute("reports", reportsDto);
        return "admin/editReportsCuttingId";
        }

    @PostMapping("/editReportsCuttingId")
    public String editReportsCuttingId(@ModelAttribute ReportsDto reportDto) {

        Long cuttingId = reportDto.getCuttingId();
        Integer quantity = reportDto.getQuantity();
        LocalDate dateWork = reportDto.getDateWork();
        cuttingRepository.updateQuantityAndDateWorkById(cuttingId,quantity,dateWork);
        return "redirect:/admin/editReportsCutting";
    }

    @PostMapping("/deleteReportById/{cuttingId}")
    public String deleteReportById(@PathVariable Long cuttingId) {
        cuttingRepository.deleteById(cuttingId);
        return "redirect:/admin/editReportsCutting";
    }
    }