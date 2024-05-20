package ru.vilas.sewing.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.repository.CustomerRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import ru.vilas.sewing.service.*;
import ru.vilas.sewing.service.admin.AdminCuttingService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Controller
@SessionAttributes({"previousCustomers", "previousCategories"})
@RequestMapping("/categories")
public class OneSeamstressController {
    private final OperationDataService operationDataService;
    private final CategoryService categoryService;
    private final CustomUserDetailsService customUserDetailsService;
    private final WarehouseRepository warehouseRepository;
    private final OperationsListSpecialService operationsListSpecialService;
    private final CustomerRepository customerRepository;
    private final AdminCuttingService adminCuttingService;

    public OneSeamstressController(OperationDataService operationDataService, CustomUserDetailsService customUserDetailsService, WarehouseRepository warehouseRepository, OperationsListSpecialService operationsListSpecialService, CustomerRepository customerRepository, CategoryService categoryService, AdminCuttingService adminCuttingService) {
        this.operationDataService = operationDataService;
        this.categoryService = categoryService;
        this.customUserDetailsService = customUserDetailsService;
        this.warehouseRepository = warehouseRepository;
        this.operationsListSpecialService = operationsListSpecialService;
        this.customerRepository = customerRepository;
        this.adminCuttingService = adminCuttingService;
    }

    @GetMapping("/seamstressReport")

    public String getSeamstressReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "category", required = false) Long category,
            Authentication authentication,
            Model model) {

        Object principal = authentication.getPrincipal();
        String userName = null;

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }

        // Получаем идентификатор пользователя
        Long currentUserId = customUserDetailsService.getUserIdByUsername(userName);


        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = LocalDate.now().with(DayOfWeek.THURSDAY);
        }

        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        List<String> headers = operationDataService.getDatesInPeriod(startDate, endDate);
        SeamstressDto seamstressDto = operationDataService.getSeamstressDto(startDate, endDate);

        List<Category> categories = categoryService.getAllCategories()
                .stream()
                .filter(Category::isActive)
                .collect(Collectors.toList());

        List<Category> categoryList = new ArrayList<>();
        if (category != null && category != 0) {
            categoryList.add(categoryService.getCategoryById(category));
        } else {
            categoryList = categories;
        }


        List<EarningsDto> earningsDtos = operationDataService.getEarningsDtosList(startDate, endDate, categoryList);

        List<EarningsDto> earningsDtosList;

        if (category == null || category == 0) {
            earningsDtosList = operationDataService.getCommonEarningsDtosList(startDate, endDate);
        } else {
            earningsDtosList = earningsDtos.stream().filter(dto -> dto.getCategory().getId().equals(category))
                    .collect(Collectors.toList());
        }

        BigDecimal salarySum = earningsDtosList.stream()
                .map(EarningsDto::getSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalAmountSum = earningsDtosList.stream()
                .map(EarningsDto::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);


        model.addAttribute("categories", categories);
        model.addAttribute("seamstressDto", seamstressDto);
        model.addAttribute("tableHeaders", headers);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("earningsList", earningsDtosList);
        model.addAttribute("selectedCategoryId", category);

        model.addAttribute("salarySum", salarySum);
        model.addAttribute("totalAmountSum", totalAmountSum);
        model.addAttribute("currentUserId", currentUserId);

        return "seamstressReport";
    }


    @ModelAttribute("previousCustomers")
    public Long previousCustomers() {
        return 0L;
    }
    @ModelAttribute("previousCategories")
    public AtomicLong previousCategories() {
        return new AtomicLong(0L);
    }

@GetMapping("/cuttingTaskReport")
    public String cuttingReport(Model model, Authentication authentication,
                                @RequestParam(name = "customer", required = false) Long customerId,
                                @RequestParam(name = "category", required = false) Long categoryId,
                                @RequestParam(name = "nameMaterial", required = false) String selectedNameMaterial,
                                @ModelAttribute("previousCustomers") Long previousCustomers,
                                @ModelAttribute("previousCategories") AtomicLong previousCategories) {
        // блок получения имени швеи по идентификации *************************************************************
        Object principal = authentication.getPrincipal();
        String userName = null;
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }
        Long userId = customUserDetailsService.getUserIdByUsername(userName);
        String currentUserName = customUserDetailsService.findNameById(customUserDetailsService.getUserIdByUsername(userName));

        // инициализируем поля при первой загрузки формы
        if (customerId == null) {
            customerId = 0L;
        }
        if (categoryId == null) {
            categoryId = 0L;
        }
        if (selectedNameMaterial == null) {
            selectedNameMaterial = "";
        }
        //********************************************************************************
         if(previousCustomers != null && !previousCustomers.equals(customerId)) {
             selectedNameMaterial = "";

        }
            previousCustomers = customerId;
        //*****************************************************************************
        // собираем список id заказчиков с активными заданиями на крой и потом список "заказчик"
        List<Long> warehouseId = warehouseRepository.findIdsByArchiveFalse();
        List<Long> customer = new ArrayList<>();
        for (Long Id : warehouseId) {
            Long customersId = warehouseRepository.findCustomerIdByWarehouseId(Id);
            customer.add(customersId);
        }
        // Преобразование списка в множество, чтобы удалить дубликаты
         Set<Long> uniqueCustomerIds = new HashSet<>(customer);
        // Преобразование обратно в список
         List<Long> uniqueCustomers = new ArrayList<>(uniqueCustomerIds);
         //  получаем лист заказчиков по их id
        List<Customer> customers = uniqueCustomers.stream()
                .map(id -> customerRepository.findCustomerById(id))
                .collect(Collectors.toList());
        customers.sort(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER));
        //*****************************************************************************************
        //получаем категории по заказчику
        List<Category> categories = warehouseRepository.findCategoriesByCustomerId(customerId);
        //*****************************************************************************
        //получаем материал по категории
        List<String> nameMaterial = warehouseRepository.findNameMaterialByCustomerIdAndCategoryId(customerId, categoryId);
        if (previousCategories.get() != categoryId){
            selectedNameMaterial = "";
        }
        previousCategories.set(categoryId);
        //**********************************************************************************
        // выполнится только после нажатия кнопки Ок
            if( selectedNameMaterial != "" && customerId != 0 && categoryId != 0) {
            Long   warehouse = warehouseRepository.findIdByCustomerCategoryMaterial(customerId, categoryId, selectedNameMaterial);
            model.addAttribute("warehouse", warehouse);
            }
        //***************************************************************************
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute( "userId", userId);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("previousCustomers", customerId);
        model.addAttribute("nameMaterial", nameMaterial);
        model.addAttribute("selectedNameMaterial", selectedNameMaterial);

        return "cuttingTaskReport";
    }
    @GetMapping("/packagingTaskReport")
    public String packagingTaskReport(Model model, Authentication authentication,
                                      //  @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                      @RequestParam(name = "customer", required = false) Long customerId,
                                      @RequestParam(name = "category", required = false) Long categoryId,
                                      @RequestParam(name = "nameMaterial", required = false) String selectedNameMaterial,
                                      @ModelAttribute("previousCustomers") Long previousCustomers,
                                      @ModelAttribute("previousCategories") AtomicLong previousCategories) {
        // блок получения имени швеи по идентификации *************************************************************
        Object principal = authentication.getPrincipal();
        String userName = null;
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }
        Long userId = customUserDetailsService.getUserIdByUsername(userName);
        String currentUserName = customUserDetailsService.findNameById(customUserDetailsService.getUserIdByUsername(userName));
        //*********************************************************************************
        // инициализируем поля при первой загрузки формы
        if (customerId == null) {
            customerId = 0L;
        }
        if (categoryId == null) {
            categoryId = 0L;
        }
        if (selectedNameMaterial == null) {
            selectedNameMaterial = "";
        }
        //********************************************************************************
        if(previousCustomers != null && !previousCustomers.equals(customerId)) {
            selectedNameMaterial = "";

        }
        previousCustomers = customerId;
        //*****************************************************************************
        // собираем список id заказчиков с активными заданиями на крой и потом список "заказчик"
        List<Long> warehouseId = warehouseRepository.findIdsByArchiveFalse();// все неархивные задачи
        List<Long> customer = new ArrayList<>();
        for (Long Id : warehouseId) {
            Long customersId = warehouseRepository.findCustomerIdByWarehouseId(Id);
            customer.add(customersId);
        }
        // Преобразование списка в множество, чтобы удалить дубликаты
        Set<Long> uniqueCustomerIds = new HashSet<>(customer);
        // Преобразование обратно в список
        List<Long> uniqueCustomers = new ArrayList<>(uniqueCustomerIds);
        //  получаем лист заказчиков по их id
        List<Customer> customers = uniqueCustomers.stream()
                .map(id -> customerRepository.findCustomerById(id))
                .collect(Collectors.toList());
        customers.sort(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER));
        //*****************************************************************************************
        //получаем категории по заказчику
        List<Category> categories = warehouseRepository.findCategoriesByCustomerId(customerId);
        //*****************************************************************************
        //получаем материал по категории
        List<String> nameMaterial = warehouseRepository.findNameMaterialByCustomerIdAndCategoryId(customerId, categoryId);
        if (previousCategories.get() != categoryId){
            selectedNameMaterial = "";
        }
        previousCategories.set(categoryId);
        //**********************************************************************************
        // выполнится только после нажатия кнопки Ок
        if( selectedNameMaterial != "" && customerId != 0 && categoryId != 0) {
            Long   warehouse = warehouseRepository.findIdByCustomerCategoryMaterial(customerId, categoryId, selectedNameMaterial);
        /* WarehouseDto warehouseDto = operationsListSpecialService.showCuttingWarehouse(warehouse);
           List<SizeByDateDto> sizeByDateDto = operationsListSpecialService.showTaskPackaging(warehouse);
            Integer totalPackaging = operationsListSpecialService.totalPackaging(warehouse);

            model.addAttribute("totalPackaging", totalPackaging);
            model.addAttribute("sizeByDateDto", sizeByDateDto);
            model.addAttribute("warehouseDto", warehouseDto); */
            model.addAttribute("warehouse", warehouse);
        }
        //***************************************************************************
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute( "userId", userId);
        model.addAttribute("customers", customers);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("previousCustomers", customerId);
        model.addAttribute("nameMaterial", nameMaterial);
        model.addAttribute("selectedNameMaterial", selectedNameMaterial);

        return "packagingTaskReport";
    }

    @GetMapping("/cuttingTaskReportData")
    public String cuttingTaskReportData(Model model, Authentication authentication,
                                        @RequestParam("warehouse") Long warehouse){
        // блок получения имени швеи по идентификации *************************************************************
        Object principal = authentication.getPrincipal();
        String userName = null;
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }
        Long userId = customUserDetailsService.getUserIdByUsername(userName);
        String currentUserName = customUserDetailsService.findNameById(customUserDetailsService.getUserIdByUsername(userName));
        //**************************************************************************************************
        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(warehouse);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName =warehouseRepository.findCategoryById(warehouse).getName();
        String materialName = warehouseRepository.findNameMaterialById(warehouse);
        //**** Получение дат начала и конца периода *********************************************************
        LocalDate startWork = warehouseRepository.findStartWorkById(warehouse);
        LocalDate endWork = warehouseRepository.findEndWorkById(warehouse);
        //*************************************************************************************
        WarehouseDto warehouseDto = operationsListSpecialService.showCuttingWarehouse(warehouse);
        List<SizeByDateDto> sizeByDateDto = operationsListSpecialService.showTaskCutting(warehouse);
        Integer totalCutOut = operationsListSpecialService.totalCutOut(warehouse);

        model.addAttribute("startWork", startWork);
        model.addAttribute("endWork", endWork);
        model.addAttribute( "userId", userId);
        model.addAttribute("totalCutOut", totalCutOut);
        model.addAttribute("sizeByDateDto", sizeByDateDto);
        model.addAttribute("warehouseDto", warehouseDto);
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("customersName", customersName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("materialName", materialName);
        model.addAttribute("warehouse", warehouse);
        return "cuttingTaskReportData";
    }
   @GetMapping("/packagingTaskReportData")
    public String packagingTaskReportData(Model model, Authentication authentication,
                                        @RequestParam("warehouse") Long warehouseId){
        // блок получения имени швеи по идентификации *************************************************************
        Object principal = authentication.getPrincipal();
        String userName = null;
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }
        Long userId = customUserDetailsService.getUserIdByUsername(userName);
        String currentUserName = customUserDetailsService.findNameById(customUserDetailsService.getUserIdByUsername(userName));
        //**************************************************************************************************
        Long customersId = warehouseRepository.findCustomerIdByWarehouseId(warehouseId);
        String customersName = customerRepository.findNameById(customersId);
        String categoryName =warehouseRepository.findCategoryById(warehouseId).getName();
        String materialName = warehouseRepository.findNameMaterialById(warehouseId);
        //**** Получение дат начала и конца периода *********************************************************
        LocalDate startWork = warehouseRepository.findStartWorkById(warehouseId);
        LocalDate endWork = warehouseRepository.findEndWorkById(warehouseId);
        //*************************************************************************************
        WarehouseDto warehouseDto = operationsListSpecialService.showCuttingWarehouse(warehouseId);
        List<SizeByDateDto> sizeByDateDto = operationsListSpecialService.showTaskPackaging(warehouseId);
        Integer totalPackaging = operationsListSpecialService.totalPackaging(warehouseId);

        model.addAttribute("startWork", startWork);
        model.addAttribute("endWork", endWork);
        model.addAttribute( "userId", userId);
        model.addAttribute("totalPackaging", totalPackaging);
        model.addAttribute("sizeByDateDto", sizeByDateDto);
        model.addAttribute("warehouseDto", warehouseDto);
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("customersName", customersName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("materialName", materialName);
        model.addAttribute("warehouse", warehouseId);
        return "packagingTaskReportData";
    }

    @PostMapping("/cuttingTaskReportData")
    public String saveCuttingReport(Model model, @ModelAttribute("warehouse") Long warehouse,
                                    @RequestParam("sizeByDateIds") List<Long> sizeByDateIds,
                                    @RequestParam(required = false) Long userId,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam("userInput") List<Integer> userInputs){

        CuttingDto cuttingDto = new CuttingDto();
        cuttingDto.setQuantityFull(userInputs);
        cuttingDto.setDateWork(date);
        cuttingDto.setSeamstressId(userId);
        cuttingDto.setWarehouseId(warehouse);
        cuttingDto.setSizeByDatesId(sizeByDateIds);
        boolean saveSuccessful = adminCuttingService.saveCuttingFromDto(cuttingDto);

        if (saveSuccessful) {
            // Если сохранение прошло успешно, добавляем сообщение в модель
            model.addAttribute("message", "Данные успешно сохранены");
        } else {
            // Если сохранение не удалось, можно добавить соответствующее сообщение об ошибке
            model.addAttribute("message", "Ошибка при сохранении данных");
        }

        return "redirect:/categories/cuttingTaskReport";
    }
    @PostMapping("/packagingTaskReportData")
    public String savePackagingTaskReport(Model model,
                                          @ModelAttribute("warehouse") Long warehouse,
                                          @RequestParam("sizeByDateIds") List<Long> sizeByDateIds,
                                          @RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          @RequestParam("userInput") List<Integer> userInputs){

        CuttingDto cuttingDto = new CuttingDto();
        cuttingDto.setQuantityFull(userInputs);
        cuttingDto.setDateWork(date);
        cuttingDto.setSeamstressId(userId);
        cuttingDto.setWarehouseId(warehouse);
        cuttingDto.setSizeByDatesId(sizeByDateIds);

        boolean saveSuccessful = adminCuttingService.savePackagingFromDto(cuttingDto);
        if (saveSuccessful) {
            // Если сохранение прошло успешно, добавляем сообщение в модель
            model.addAttribute("message", "Данные успешно сохранены");
        } else {
            // Если сохранение не удалось, можно добавить соответствующее сообщение об ошибке
            model.addAttribute("message", "Ошибка при сохранении данных");
        }
        return "redirect:/categories/packagingTaskReport";
    }
}
