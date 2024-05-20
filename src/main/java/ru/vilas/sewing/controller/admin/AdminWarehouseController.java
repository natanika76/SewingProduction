package ru.vilas.sewing.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.model.SizeByDate;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.repository.CategoryRepository;
import ru.vilas.sewing.repository.CustomerRepository;
import ru.vilas.sewing.repository.SizeByDateRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.CategoryService;
import ru.vilas.sewing.service.CustomerService;
import ru.vilas.sewing.service.admin.AdminWarehouseService;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Controller
@RequestMapping("/admin/warehouseProcess")
public class AdminWarehouseController {
    private final CustomerService customerService;
    private final CategoryService categoryService;
    private final AdminWarehouseService adminWarehouseService;
    private final WarehouseRepository warehouseRepository;
    private  final CategoryRepository categoryRepository;


    public AdminWarehouseController(CustomerService customerService, CategoryService categoryService, AdminWarehouseService adminWarehouseService, WarehouseRepository warehouseRepository, CategoryRepository categoryRepository) {
        this.customerService = customerService;
        this.categoryService = categoryService;
        this.adminWarehouseService = adminWarehouseService;
        this.warehouseRepository = warehouseRepository;
        this.categoryRepository = categoryRepository;

    }

    @GetMapping
    public String showWarehouse(Model model,
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
        //получаем категории по заказчику
        List<Category> categories = warehouseRepository.findCategoriesByCustomerId(customerId);
        List<String> nameMaterials = warehouseRepository.findNameMaterialByCustomerIdAndCategoryId(customerId, categoryId);
        List<WarehouseDto> warehouse = adminWarehouseService.showAllActiveTasksCut(customerId, categoryId, selectedNameMaterial);

        model.addAttribute("nameMaterials", nameMaterials);
        model.addAttribute("categories", categories);
        model.addAttribute("warehouses", warehouse);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedNameMaterial",selectedNameMaterial);
        return "admin/warehouseProcess";

    }

    @PostMapping("/admin/warehouseProcess/{id}")
    public ResponseEntity<?> archiveWarehouseProcess(@PathVariable Long id, @RequestBody WarehouseDto dto) {
        // Получите запись по ее идентификатору из репозитория
        Warehouse warehouseProcess = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse process not found with id: " + id));// Если объект не найден кинет исключение
        // Установите значение атрибута archive на true
        warehouseProcess.setArchive(true);
        // Сохраните изменения в репозитории
        warehouseRepository.save(warehouseProcess);
        // Отправьте ответ об успешном выполнении операции
        return ResponseEntity.ok().build();
    }

    @GetMapping("/new")
    public String addWarehouseProcess(Model model,
                                      @RequestParam(name = "customerId", required = false) Long customerId,
                                      @RequestParam(name = "categoryId", required = false) Long categoryId) {
        List<Customer> customers = customerService.getAllCustomers();
        customers.sort(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER));

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

        Warehouse warehouse = new Warehouse(); // Создаем объект Warehouse
        WarehouseDto warehouseDto = new WarehouseDto();

        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("categories", allCategories);
        model.addAttribute("customers", customers);
        model.addAttribute("warehouseData", warehouse);
        model.addAttribute("warehouseDto", warehouseDto);


        return "admin/addWarehouseProcess";
    }

    @PostMapping("/new")
    public String saveNewWarehouse(@ModelAttribute WarehouseDto warehouseDto, Model model) {
        Boolean isSave = adminWarehouseService.checkingForExistence(warehouseDto);
        if(isSave == false){
         model.addAttribute("error", "Невозможно добавить новое задание, так как есть активное задание с таким заказчиком, категорией и материалом");
            return "redirect:/admin/warehouseProcess/new?error=true";
        } else {
            Warehouse warehouse = adminWarehouseService.convertToEntity(warehouseDto);
            Long warehouseId = warehouseRepository.save(warehouse).getId();
            adminWarehouseService.saveSizeByDate(warehouseDto,warehouseId);
            model.addAttribute("message","Задание создано");
            return "redirect:/admin/warehouseProcess/new?success=true";
        }
    }
    @GetMapping("/show/{id}")
    public String showWarehouseProcess(@PathVariable Long id, Model model) {
        Warehouse warehouse = adminWarehouseService.getWarehouseById(id);
        List<SizeByDateDto> sizeByDate =adminWarehouseService.getSizeByDateById(id);

        model.addAttribute("sizeByDates", sizeByDate);
        model.addAttribute("warehouseData", warehouse);

        return "admin/showWarehouseProcess";
    }
    @GetMapping("/edit/{id}")
    public String editWarehouseProcess(@PathVariable Long id,Model model){
        Long customerId = warehouseRepository.findCustomerIdByWarehouseId(id);
        List<Category> categories = categoryRepository.findByCustomerId(customerId);
        Warehouse warehouse = adminWarehouseService.getWarehouseById(id);
        List<SizeByDateDto> sizeByDate =adminWarehouseService.getSizeByDateById(id);

        model.addAttribute("categories", categories);
        model.addAttribute("sizeByDates", sizeByDate);
        model.addAttribute("warehouseData", warehouse);
        return "admin/editWarehouseProcess";
    }
    @PostMapping("/edit")
    public String editWarehouseProcess(@ModelAttribute WarehouseDto warehouseDto, Long id, Model model){
     Boolean isSave = adminWarehouseService.checkingForEdit(warehouseDto, id);
        if(isSave == false){
            model.addAttribute("error", "Невозможно изменить задание, так как есть активное задание с таким заказчиком, категорией и материалом");
            return "redirect:/admin/warehouseProcess?error=true";
         } else {
           adminWarehouseService.updatedWarehouse(warehouseDto);
           model.addAttribute("message","Задание изменено");
            return "redirect:/admin/warehouseProcess?success=true";
         }
    }

    @GetMapping("/archive/{id}")
    public String sendToArchive(@PathVariable Long id){
        warehouseRepository.sendToArchive(id);
        return "redirect:/admin/warehouseProcess";
    }
}




