package ru.vilas.sewing.service.admin;

import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.repository.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.vilas.sewing.dto.TaskTypes.*;

@Service
public class AdminShipmentServiceImpl implements AdminShipmentService {
    private final PackagingRepository packagingRepository;
    private final SizeByDateRepository sizeByDateRepository;
    private final WarehouseRepository warehouseRepository;
    private final ShipmentRepository shipmentRepository;
    private final CuttingRepository cuttingRepository;
    private final UserRepository userRepository;
    private final SeamstressService seamstressService;
    private final TaskRepository taskRepository;
    private final OperationDataRepository operationDataRepository;


    public AdminShipmentServiceImpl(PackagingRepository packagingRepository, SizeByDateRepository sizeByDateRepository, WarehouseRepository warehouseRepository, ShipmentRepository shipmentRepository, CuttingRepository cuttingRepository, UserRepository userRepository, SeamstressService seamstressService, TaskRepository taskRepository, OperationDataRepository operationDataRepository) {
        this.packagingRepository = packagingRepository;
        this.sizeByDateRepository = sizeByDateRepository;
        this.warehouseRepository = warehouseRepository;
        this.shipmentRepository = shipmentRepository;
        this.cuttingRepository = cuttingRepository;
        this.userRepository = userRepository;
        this.seamstressService = seamstressService;

        this.taskRepository = taskRepository;
        this.operationDataRepository = operationDataRepository;
    }

    @Override
    public List<WarehouseDto> showAllActiveTasksCutEndShipment(Long customerId, Long categoryId, String nameMaterial) {
        List<WarehouseDto> warehouseDtos = new ArrayList<>();
        List<Warehouse> warehouses;
        if (nameMaterial != "" && customerId != 0 && categoryId != 0) {
            warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerIdAndCategoryIdAndNameMaterial(customerId, categoryId, nameMaterial);
        } else if (customerId != 0 && categoryId != 0 && nameMaterial == "") {
            warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerIdAndCategoryId(customerId, categoryId);
        } else if (customerId != 0 && categoryId == 0 && nameMaterial == "") {
            warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerId(customerId);
        } else if (categoryId != 0 && customerId == 0 && nameMaterial == "") {
            warehouses = warehouseRepository.findAllByCategoryId(categoryId);
        } else if (nameMaterial != "") {
            warehouses = warehouseRepository.findAllByNameMaterial(nameMaterial);
        } else {
            warehouses = warehouseRepository.findAllByArchiveIsFalse();
        }

        for (Warehouse warehouse : warehouses) {
            WarehouseDto warehouseDto = new WarehouseDto();
            warehouseDto.setId(warehouse.getId());
            warehouseDto.setCustomerName(warehouse.getCustomer().getName());
            warehouseDto.setCategoryName(warehouse.getCategory().getName());
            warehouseDto.setNameMaterial(warehouse.getNameMaterial());
            List<Packaging> packagings = packagingRepository.findByWarehouseId(warehouse.getId());
            Integer sum = 0;  // количество упаковано
            for (Packaging packaging : packagings) {
                sum += packaging.getQuantity();
            }
            List<Shipment> shipments = shipmentRepository.findByWarehouseId(warehouse.getId());
            Integer done = 0;
            for (Shipment shipment : shipments) {
                done += shipment.getQuantity();
            }
            warehouseDto.setDone(done);          //количество отгруженно
            int balance = warehouse.getTarget() - warehouse.getRemains();// количество задание
            warehouseDto.setBalance(balance);
            int numberOfDays = calculateDaysFromStart(warehouse.getStartWork()); // количество дней от начала работ до текущей даты
            Integer plan = (warehouse.getNormPerDay() * numberOfDays) * 100 / balance; //  планируемый % выполнения задания
            warehouseDto.setPlan(plan);
            int shipment = done * 100 / balance;
            if (shipment >= 100) {
                shipment = 100;
            }
            warehouseDto.setShipment(shipment); //количество фактическое % отгружено

            sum = sum * 100 / balance; // количество % упаковано
            warehouseDto.setPackaging(sum);

            warehouseDtos.add(warehouseDto);
        }

        return warehouseDtos;
    }

    //метод для получения % от объщего на сегодняшнюю дату.
    public static int calculateProgress(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now(); // Получаем текущую дату
        long totalDuration = endDate.toEpochDay() - startDate.toEpochDay();
        long elapsedDuration = currentDate.toEpochDay() - startDate.toEpochDay();
        // Проверяем, что elapsedDuration не меньше нуля
        if (elapsedDuration < 0) {
            return 0; // Минимальный процент выполнения
        }
        // Проверяем, если прошло более 100% времени
        if (elapsedDuration >= totalDuration) {
            return 100; // Максимальный процент выполнения
        }
        double progress = (double) elapsedDuration / totalDuration * 100;
        // Округляем и приводим к int
        return (int) Math.round(progress);
    }


    @Override
    public List<SizeByDateDto> addShipmentProcess(Long warehouseId) {
        List<SizeByDateDto> sizeByDateDtos = new ArrayList<>();
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseId(warehouseId);

        Integer total = 0;
        for (SizeByDate sizeByDate : sizeByDates) {
            SizeByDateDto sizeByDateDto = new SizeByDateDto();
            Integer sum = 0;
            List<Packaging> packagings = packagingRepository.findBySizeByDateId(sizeByDate.getId());
            for (Packaging packaging : packagings) {
                sum += packaging.getQuantity();
            }
            Integer summa = 0;
            List<Shipment> shipments = shipmentRepository.findBySizeByDateId(sizeByDate.getId());
            for (Shipment shipment : shipments) {
                summa += shipment.getQuantity();
            }
            sum -= summa;
            sizeByDateDto.setSize(sizeByDate.getSize());
            sizeByDateDto.setHeight(sizeByDate.getHeight());
            sizeByDateDto.setQuantity(sizeByDate.getQuantity());
            sizeByDateDto.setDone(sum);
            sizeByDateDto.setFullDone(sizeByDate.getQuantity() - summa);
            sizeByDateDto.setSizeByDateId(sizeByDate.getId());
            sizeByDateDtos.add(sizeByDateDto);
        }
        return sizeByDateDtos;
    }

    @Override
    public WarehouseDto showShipmentWarehouse(Long warehouseId) {
        Optional<Warehouse> warehouses = warehouseRepository.findById(warehouseId);
        Warehouse warehouseAllDate = warehouses.get();
        WarehouseDto warehouseDto = new WarehouseDto();
        warehouseDto.setId(warehouseAllDate.getId());
        warehouseDto.setNormPerDay(warehouseAllDate.getNormPerDay());
        warehouseDto.setInTotal(warehouseAllDate.getInTotal());
        List<Packaging> packagings = packagingRepository.findByWarehouseId(warehouseId);
        Integer sum = 0;
        for (Packaging packaging : packagings) {
            sum += packaging.getQuantity();
        }
        warehouseDto.setBalance(sum);
        Integer summa = 0;
        List<Shipment> shipments = shipmentRepository.findByWarehouseId(warehouseId);
        for (Shipment shipment : shipments) {
            summa += shipment.getQuantity();
        }
        sum -= summa;
        warehouseDto.setDone(sum);
        warehouseDto.setTarget(warehouseAllDate.getInTotal() - summa);

        List<Cutting> cuttings = cuttingRepository.findAllByWarehouseId(warehouseId);
        sum = 0;
        for (Cutting cutting : cuttings) {
            sum += cutting.getQuantity();
        }
        warehouseDto.setTarget(sum);

        return warehouseDto;
    }

    @Override
    public boolean saveShipmentFromDto(List<CuttingDto> cuttingDtos) {
        LocalDate date = null;
        Long warehouseId = 0L;
        for (CuttingDto cuttingDto : cuttingDtos) {
            date = cuttingDto.getDateWork();
            // seamstressId = cuttingDto.getSeamstressId();
            warehouseId = cuttingDto.getWarehouseId();
            break;
        }
        Boolean record = shipmentRepository.existsByDateWorkAndWarehouseId(date, warehouseId);
        if (record) {
            try {
                for (CuttingDto cuttingDto : cuttingDtos) {
                    Optional<Shipment> optionalShipment = shipmentRepository.findByDateWorkAndSizeByDateIdAndWarehouseId(date, cuttingDto.getSizeByDateId(), warehouseId);
                    Shipment shipment = optionalShipment.get();
                    int quantity = cuttingDto.getQuantity() + shipment.getQuantity();
                    shipment.setQuantity(quantity);
                    shipmentRepository.save(shipment); // Сохраняем объект Cutting в базу данных
                }
                return true; // успешное сохранения
            } catch (Exception e) {
                // Обработка ошибок при сохранении
                return false; // неудачное сохранения
            }
        } else {
            try {
                for (CuttingDto cuttingDto : cuttingDtos) {
                    Shipment shipment = new Shipment();
                    shipment.setWarehouseId(cuttingDto.getWarehouseId());
                    shipment.setDateWork(cuttingDto.getDateWork());
                    shipment.setQuantity(cuttingDto.getQuantity());
                    shipment.setSizeByDateId(cuttingDto.getSizeByDateId());
                    shipmentRepository.save(shipment); // Сохраняем объект Shipment в базу данных
                }
                return true; // успешное сохранения
            } catch (Exception e) {
                // Обработка ошибок при сохранении
                return false; // неудачное сохранения
            }
        }
    }

    @Override
    public List<AnalyticsDto> showAnalytics(Long warehouseId) {
        List<AnalyticsDto> analyticsDtos = new ArrayList<>();

        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseId(warehouseId);
        for (SizeByDate sizeByDate : sizeByDates) {
            AnalyticsDto analyticsDto = new AnalyticsDto();
            analyticsDto.setSize(sizeByDate.getSize());
            analyticsDto.setHeight(sizeByDate.getHeight());
            analyticsDto.setQuantity(sizeByDate.getQuantity());
            int cum = 0;
            List<Packaging> packagings = packagingRepository.findBySizeByDateId(sizeByDate.getId());
            for (Packaging packaging : packagings) {
                cum += packaging.getQuantity();
            }
            analyticsDto.setPackaging(cum);
            cum = 0;
            List<Shipment> shipments = shipmentRepository.findBySizeByDateId(sizeByDate.getId());
            for (Shipment shipment : shipments) {
                cum += shipment.getQuantity();
            }
            analyticsDto.setShipment(cum);
            analyticsDto.setRemainsWarehouse(analyticsDto.getPackaging() - analyticsDto.getShipment());
            analyticsDto.setRemainsShipment(analyticsDto.getQuantity() - analyticsDto.getShipment());

            analyticsDtos.add(analyticsDto);
        }
        return analyticsDtos;
    }

    @Override
    public AnalyticsDto showTotalAnalytics(Long warehouseId) {
        AnalyticsDto analyticsDto = new AnalyticsDto();
        List<AnalyticsDto> analyticsDtos = showAnalytics(warehouseId);
        int totalQuantity = 0;
        int totalPackaging = 0;
        int totalShipment = 0;
        int totalRemainsWarehouse = 0;
        int totalRemainsShipment = 0;

        for (AnalyticsDto total : analyticsDtos) {
            totalQuantity += total.getQuantity();
            totalPackaging += total.getPackaging();
            totalShipment += total.getShipment();
            totalRemainsWarehouse += total.getRemainsWarehouse();
            totalRemainsShipment += total.getRemainsShipment();
        }
        analyticsDto.setTotalQuantity(totalQuantity);
        analyticsDto.setTotalPackaging(totalPackaging);
        analyticsDto.setTotalShipment(totalShipment);
        analyticsDto.setTotalRemainsWarehouse(totalRemainsWarehouse);
        analyticsDto.setTotalRemainsShipment(totalRemainsShipment);

        return analyticsDto;
    }

    @Override
    public Integer workingPeriod(Long warehouseId) {
        Optional<Warehouse> warehouses = warehouseRepository.findById(warehouseId);
        Warehouse warehouseAllDate = warehouses.get();
        LocalDate startWork = warehouseAllDate.getStartWork();
        LocalDate endWork = warehouseAllDate.getEndWork();

        return calculateDaysBetween(startWork, endWork);
    }

    // Метод для расчета количества дней между двумя датами
    public static int calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);

    }

    @Override
    public Integer periodCurrentDate(Long warehouseId) {
        Optional<Warehouse> warehouses = warehouseRepository.findById(warehouseId);
        Warehouse warehouseAllDate = warehouses.get();
        LocalDate startWork = warehouseAllDate.getStartWork();
        Integer periodCurrentDate = calculateDaysFromStart(startWork);

        return periodCurrentDate;
    }

    // Метод для расчета количества дней от заданной даты до текущей даты
    public static int calculateDaysFromStart(LocalDate startDate) {
        LocalDate currentDate = LocalDate.now();
        return (int) ChronoUnit.DAYS.between(startDate, currentDate);
    }

    @Override
    public Integer shipmentForecastForTheEndDate(Long warehouseId) {
        int numberOfDays = periodCurrentDate(warehouseId);
        int workingPeriod = workingPeriod(warehouseId);
        AnalyticsDto analyticsDto = showTotalAnalytics(warehouseId);
        int totalShipment = analyticsDto.getTotalShipment();

        if (numberOfDays == 0) {
            numberOfDays = 1;
        }
        double shipmentForecastDay = (double) totalShipment / numberOfDays;

        if (workingPeriod == numberOfDays) {
            workingPeriod += 1;
        }
        double shipment = (totalShipment + (shipmentForecastDay * (workingPeriod - numberOfDays)));
        int shipmentForecast = (int) Math.ceil(shipment);
        return shipmentForecast;
    }

    @Override
    public Integer forecastOfNumberDays(Long warehouseId) {
        int numberOfDays = periodCurrentDate(warehouseId);
        int workingPeriod = workingPeriod(warehouseId);
        AnalyticsDto analyticsDto = showTotalAnalytics(warehouseId);
        int totalQuantity = analyticsDto.getTotalQuantity();
        int totalShipment = analyticsDto.getTotalShipment();
        if (numberOfDays == 0) {
            numberOfDays = 1;
        }
        int shipmentDay = totalShipment / numberOfDays;

        if (shipmentDay == 0) {
            shipmentDay = 1;
        }
        int forecastDays = (totalQuantity - totalShipment) / shipmentDay;

        return forecastDays;
    }

    @Override
    public Integer recommendedShipmentRatePerDay(Long warehouseId) {
        Optional<Warehouse> warehouses = warehouseRepository.findById(warehouseId);
        Warehouse warehouseAllDate = warehouses.get();
        LocalDate startWork = warehouseAllDate.getStartWork();
        int numberOfDays = calculateDaysFromStart(startWork);
        LocalDate endWork = warehouseAllDate.getEndWork();
        int workingPeriod = calculateDaysBetween(startWork, endWork);

        int restDays = workingPeriod - numberOfDays;
        AnalyticsDto analyticsDto = showTotalAnalytics(warehouseId);
        int totalQuantity = analyticsDto.getTotalQuantity();
        int totalShipment = analyticsDto.getTotalShipment();
        int quantityToBeShipped = totalQuantity - totalShipment;
        double recommendedShipment = 0.0;
        if (restDays <= 0) {
            recommendedShipment = quantityToBeShipped;
        } else {
            recommendedShipment = (double) quantityToBeShipped / restDays;
        }

        int recommendedShipmentRatePerDay = (int) Math.round(recommendedShipment);

        return recommendedShipmentRatePerDay;
    }

    @Override
    public List<AnalyticsDto> chartShowAnalytics(Long warehouseId) {
        List<Shipment> shipments = shipmentRepository.findByWarehouseId(warehouseId);
        List<LocalDate> dates = new ArrayList<>();

        for (Shipment shipment : shipments) {
            // LocalDate date = LocalDate.ofEpochDay(shipment.getSizeByDateId());
            LocalDate date = shipment.getDateWork();
            dates.add(date);
        }
        // Создаем множество из списка, чтобы удалить дубликаты
        Set<LocalDate> uniqueDatesSet = new HashSet<>(dates);
        // Преобразуем обратно в список, если нужно
        List<LocalDate> uniqueDatesList = new ArrayList<>(uniqueDatesSet);
        // Очищаем исходный список и добавляем уникальные даты
        dates.clear();
        dates.addAll(uniqueDatesList);

        List<AnalyticsDto> chartAnalyticsDtos = new ArrayList<>();
        for (LocalDate date : dates) {
            AnalyticsDto charAnalyticsDto = new AnalyticsDto();
            List<Shipment> shipmentLisDate = shipmentRepository.findByDateWorkAndWarehouseId(date, warehouseId);
            int sum = 0;
            for (Shipment shipment : shipmentLisDate) {
                sum += shipment.getQuantity();
            }
            charAnalyticsDto.setDate(date);
            charAnalyticsDto.setQuantity(sum);
            chartAnalyticsDtos.add(charAnalyticsDto);
        }
        return chartAnalyticsDtos;
    }

    @Override
    public List<ChartCuttingDto> showChartPackaging(Long warehouseId, LocalDate startDate, LocalDate endDate, Long seamstressId) {
        // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizeByDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            Long sizeByDateId = 0L;
            sizeByDateId = id.getId();
            sizeByDateIds.add(sizeByDateId);
        }
        List<ChartCuttingDto> chartCuttingDtos = new ArrayList<>();
        int total = 0;
        //если выбрана швея
        if (seamstressId != 0) {
            for (LocalDate date : dateRange) {
                int sum = 0;
                ChartCuttingDto chartCuttingDto = new ChartCuttingDto();
                for (Long id : sizeByDateIds) {
                    Packaging packaging = packagingRepository.findByDateWorkAndSizeByDateIdAndSeamstressId(date, id, seamstressId);
                    if (packaging == null) {
                        sum = 0;
                        break;
                    }
                    sum += packaging.getQuantity();
                }
                total += sum;
                chartCuttingDto.setQuantity(sum);
                chartCuttingDto.setSeamstressName(userRepository.findNameById(seamstressId));
                chartCuttingDto.setTotal(total);
                chartCuttingDtos.add(chartCuttingDto);
            }
            return chartCuttingDtos;
        }
        //для всех щвей
        else {
            for (LocalDate date : dateRange) {
                Integer sum = 0;
                ChartCuttingDto chartCuttingDto = new ChartCuttingDto();

                for (Long id : sizeByDateIds) {
                    List<Packaging> packagings = packagingRepository.findByDateWorkAndSizeByDateId(date, id);
                    for (Packaging packaging : packagings) {
                        if (packaging == null) {
                            sum = 0;
                            continue;
                        }
                        sum += packaging.getQuantity();
                    }
                }
                total += sum;
                chartCuttingDto.setQuantity(sum);
                chartCuttingDto.setTotal(total);
                chartCuttingDtos.add(chartCuttingDto);
            }
            return chartCuttingDtos;
        }
    }

    @Override
    public List<User> showSeamstress(Long warehouseId) {

        List<Long> seamstressId = packagingRepository.findSeamstressIdsByWarehouseId(warehouseId);

        List<User> seamstress = new ArrayList<>();
        for (Long id : seamstressId) {
            User user = seamstressService.getSeamstressById(id);
            seamstress.add(user);
        }
        return seamstress;
    }

    @Override
    public List<ChartTaskDto> showChartTaskQuantitative (Long warehouseId, Long seamstressId) {
        LocalDate startDate = warehouseRepository.findStartWorkById(warehouseId);
        LocalDate endDate = warehouseRepository.findEndWorkById(warehouseId);
        LocalDate todayDate = LocalDate.now(); //получаем значение даты сегодня
        // Проверяем, что endDate не равен null и если конечная дата больше текущей, то конечную дату приравниваем к текущий (нельзя вводить будущую дату)
        if (endDate != null && endDate.compareTo(todayDate) > 0) {
            endDate = todayDate;
        }
        // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizeByDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            Long sizeByDateId = 0L;
            sizeByDateId = id.getId();
            sizeByDateIds.add(sizeByDateId);
        }
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(warehouseId);
        Warehouse warehouse = optionalWarehouse.get();
        Long categoryId = warehouseRepository.findCategoryIdById(warehouseId);
        List<Task> tasks = taskRepository.findByCategoryIdAndActiveIsTrueOrderByName(categoryId);
        List<Cutting> cuttings = cuttingRepository.findAllByWarehouseId(warehouseId);
        int inTotal = 0;   //Всего раскроено
        if (cuttings != null) {
            for (Cutting cutting : cuttings) {
                inTotal += cutting.getQuantity();
            }
        }
        String nameMaterial = warehouseRepository.findNameMaterialById(warehouseId);
        List<ChartTaskDto> chartTaskDtos = new ArrayList<>();
        for (Task task : tasks) {
            ChartTaskDto chartTaskDto = new ChartTaskDto();
           if (task.getTaskType() != QUANTITATIVE) { // выбираем только количественные задачи
               continue;
           }
            List<Integer> operations = new ArrayList<>();
            for (LocalDate date : dateRange) {
                int completedOperations = 0;
             //  List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTask_Id(date, nameMaterial, task.getId());
                List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTaskIdAndSeamstressId(date, nameMaterial, task.getId(), seamstressId); // выбор для фильтрации по швеи
                for (OperationData operationData : operationDatas) {
                    completedOperations += operationData.getCompletedOperations();
                }
                operations.add(completedOperations);
            }
                chartTaskDto.setCompletedOperations(operations);
                chartTaskDto.setTaskName(task.getName());
                chartTaskDto.setNormPerShift(task.getNormPerShift());
                chartTaskDto.setInTotal(inTotal);
                List<OperationData> operationDatas2 = operationDataRepository.findOperationDataByCategoryAndNameMaterialAndTaskId(categoryId, warehouse.getNameMaterial(), task.getId());
                int done = 0;
                for (OperationData operationData : operationDatas2) {
                    done += operationData.getCompletedOperations();
                }
                chartTaskDto.setDone(done);
                chartTaskDtos.add(chartTaskDto);
            }
            return chartTaskDtos;
        }
    @Override
    public List<ChartTaskDto> showChartTaskHourly(Long warehouseId, Long seamstressId) {
        LocalDate startDate = warehouseRepository.findStartWorkById(warehouseId);
        LocalDate endDate = warehouseRepository.findEndWorkById(warehouseId);
        LocalDate todayDate = LocalDate.now(); //получаем значение даты сегодня
        // Проверяем, что endDate не равен null и если конечная дата больше текущей, то конечную дату приравниваем к текущий (нельзя вводить будущую дату)
        if (endDate != null && endDate.compareTo(todayDate) > 0) {
            endDate = todayDate;
        }
        // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizeByDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            Long sizeByDateId = 0L;
            sizeByDateId = id.getId();
            sizeByDateIds.add(sizeByDateId);
        }
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(warehouseId);
        Warehouse warehouse = optionalWarehouse.get();
        Long categoryId = warehouseRepository.findCategoryIdById(warehouseId);
        List<Task> tasks = taskRepository.findByCategoryIdAndActiveIsTrueOrderByName(categoryId);
        List<Cutting> cuttings = cuttingRepository.findAllByWarehouseId(warehouseId);
        int inTotal = 0;   //Всего раскроено
        if (cuttings != null) {
            for (Cutting cutting : cuttings) {
                inTotal += cutting.getQuantity();
            }
        }
        String nameMaterial = warehouseRepository.findNameMaterialById(warehouseId);

        List<ChartTaskDto> chartTaskDtos = new ArrayList<>();
        for (Task task : tasks) {
            ChartTaskDto chartTaskDto = new ChartTaskDto();
            if (task.getTaskType() != HOURLY) { // выбираем только часовые задачи
                continue;
            }

            List<Long> operations = new ArrayList<>();
            for (LocalDate date : dateRange) {
                long minutes = 0L;
                //List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTask_Id(date, nameMaterial, task.getId());
                List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTaskIdAndSeamstressId(date, nameMaterial, task.getId(), seamstressId); // выбор для фильтрации по швеи
                Duration data = Duration.ZERO;
                for (OperationData operationData : operationDatas) {
                    Duration i = Duration.ZERO;
                    i = operationData.getHoursWorked();
                    data = data.plus(i);
                }
                 minutes = data.toMinutes();

                operations.add(minutes);
            }
            chartTaskDto.setMinutes(operations);


            chartTaskDto.setTaskName(task.getName());
            //chartTaskDto.setNormPerShift(task.getNormPerShift());
            chartTaskDto.setInTotal(inTotal);
            List<OperationData> operationDatas2 = operationDataRepository.findOperationDataByCategoryAndNameMaterialAndTaskId(categoryId, warehouse.getNameMaterial(), task.getId());
            Duration done = Duration.ZERO;
            for (OperationData operationData : operationDatas2) {
                Duration i = Duration.ZERO;
                i = operationData.getHoursWorked();
                done = done.plus(i);

            }

            chartTaskDtos.add(chartTaskDto);
        }

        return chartTaskDtos;
    }
    @Override
    public List<ChartTaskDto> showChartTaskPackaging(Long warehouseId, Long seamstressId) {
        LocalDate startDate = warehouseRepository.findStartWorkById(warehouseId);
        LocalDate endDate = warehouseRepository.findEndWorkById(warehouseId);
        LocalDate todayDate = LocalDate.now(); //получаем значение даты сегодня
        // Проверяем, что endDate не равен null и если конечная дата больше текущей, то конечную дату приравниваем к текущий (нельзя вводить будущую дату)
        if (endDate != null && endDate.compareTo(todayDate) > 0) {
            endDate = todayDate;
        }
        // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizeByDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            Long sizeByDateId = 0L;
            sizeByDateId = id.getId();
            sizeByDateIds.add(sizeByDateId);
        }
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(warehouseId);
        Warehouse warehouse = optionalWarehouse.get();
        Long categoryId = warehouseRepository.findCategoryIdById(warehouseId);
        List<Task> tasks = taskRepository.findByCategoryIdAndActiveIsTrueOrderByName(categoryId);
        List<Packaging> packagings = packagingRepository.findAllByWarehouseId(warehouseId);
        int inTotal = 0;   //Всего упаковано
        if (packagings != null) {
            for (Packaging packaging : packagings) {
                inTotal += packaging.getQuantity();
            }
        }

        String nameMaterial = warehouseRepository.findNameMaterialById(warehouseId);
        List<ChartTaskDto> chartTaskDtos = new ArrayList<>();
        for (Task task : tasks) {
            ChartTaskDto chartTaskDto = new ChartTaskDto();
            if (task.getTaskType() != PACKAGING) { // выбираем только задачи по упаковке
                continue;
            }
            List<Integer> operations = new ArrayList<>();
            for (LocalDate date : dateRange) {
                int completedOperations = 0;
                //List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTask_Id(date, nameMaterial, task.getId());
                List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTaskIdAndSeamstressId(date, nameMaterial, task.getId(), seamstressId); // выбор для фильтрации по швеи
                for (OperationData operationData : operationDatas) {
                    completedOperations += operationData.getCompletedOperations();
                }
                operations.add(completedOperations);
            }
            chartTaskDto.setCompletedOperations(operations);
            // записывает каждое значение в массив
            List<List<Integer>> operationsTwo = new ArrayList<>();
            for (LocalDate date : dateRange) {
                List<Integer> dailyOperations = new ArrayList<>();
                List<OperationData> operationDatas = operationDataRepository.findByDateAndNameMaterialAndTask_Id(date, nameMaterial, task.getId());
                if (operationDatas == null || operationDatas.size() == 0) {
                    dailyOperations.add(0);
                }
                for (OperationData operationData : operationDatas) {
                    dailyOperations.add(operationData.getCompletedOperations());
                }
                operationsTwo.add(dailyOperations);
            }
            chartTaskDto.setCompletedOperationsTwo(operationsTwo);

            chartTaskDto.setTaskName(task.getName());
            chartTaskDto.setNormPerShift(task.getNormPerShift());
            chartTaskDto.setInTotal(inTotal);
            List<OperationData> operationDatas2 = operationDataRepository.findOperationDataByCategoryAndNameMaterialAndTaskId(categoryId, warehouse.getNameMaterial(), task.getId());
            int done = 0;
            for (OperationData operationData : operationDatas2) {
                done += operationData.getCompletedOperations();
            }
            chartTaskDto.setDone(done);
            chartTaskDtos.add(chartTaskDto);
        }
        return chartTaskDtos;
    }
   @Override
    public List<User> showSeamstressFull (Long warehouseId){

        List<Long> seamstressIdCutting = cuttingRepository.findSeamstressIdsByWarehouseId(warehouseId);
        List<Long> seamstressIdPackaging  = packagingRepository.findSeamstressIdsByWarehouseId(warehouseId);

       Long categoryId = warehouseRepository.findCategoryIdById(warehouseId);
       List<Long> tasks = taskRepository.findIdsByCategoryIdAndActiveIsTrue(categoryId);
       List<Long> seamstressIds = new ArrayList<>();
       for (Long task : tasks) {
           List<Long> seamstressId = operationDataRepository.findSeamstressIdsByTaskId(task);
           seamstressIds.addAll(seamstressId);
       }


       // Создание множества для хранения уникальных идентификаторов
        Set<Long> uniqueSeamstressIds = new HashSet<>();

     // Добавление всех элементов из обоих списков в множество (убираются дубликаты автоматически)
       uniqueSeamstressIds.addAll(seamstressIdCutting);
       uniqueSeamstressIds.addAll(seamstressIdPackaging);
       uniqueSeamstressIds.addAll(seamstressIds);


     // Преобразование множества обратно в список
       List<Long> uniqueSeamstressIdsList = new ArrayList<>(uniqueSeamstressIds);
       List<User> seamstress = new ArrayList<>();
        for (Long id: uniqueSeamstressIdsList){
            User user = seamstressService.getSeamstressById(id);
            seamstress.add(user);
        }
        return seamstress;
    }


    }

