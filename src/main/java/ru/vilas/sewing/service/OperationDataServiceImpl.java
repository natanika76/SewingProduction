package ru.vilas.sewing.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.repository.OperationDataRepository;
import ru.vilas.sewing.repository.TaskRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ru.vilas.sewing.dto.TaskTypes.HOURLY;

@Service
public class OperationDataServiceImpl implements OperationDataService {
    private final OperationDataRepository operationDataRepository;
    private final TaskRepository taskRepository;
    private final CategoryService categoryService;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public OperationDataServiceImpl(OperationDataRepository operationDataRepository, TaskRepository taskRepository, CategoryService categoryService, CustomUserDetailsService customUserDetailsService) {
        this.operationDataRepository = operationDataRepository;
        this.taskRepository = taskRepository;
        this.categoryService = categoryService;
        this.customUserDetailsService = customUserDetailsService;

    }
    @Override
    public void saveOperationData(OperationData operationData) {
        operationDataRepository.save(operationData);
    }

    @Override
    public OperationData getOperationDataById(Long id) {
        return operationDataRepository.findById(id).orElse(null);
    }

    @Override
    public List<OperationData> getOperationDataByTask(Task task) {
        return operationDataRepository.findByTask(task);
    }

    @Override
    public OperationDto convertToOperationDto(Task task, Long seamstressId, String nameMaterial) {

        OperationDto operationDto = new OperationDto();

        operationDto.setId(task.getId());
        operationDto.setTaskType(task.getTaskType());
        operationDto.setName(task.getName());
        operationDto.setEquipment(task.getEquipment());
        operationDto.setTimeInSeconds(task.getTimeInSeconds());
        operationDto.setCostPerPiece(task.getCostPerPiece());
        operationDto.setNormPerShift(task.getNormPerShift());
        operationDto.setSeamstressId(seamstressId);
        operationDto.setOperations(operationDataRepository.countCompletedOperationsBySeamstressAndDateAndMaterial(seamstressId, LocalDate.now(), task.getId(), nameMaterial) );

        if (task.getTaskType().equals(HOURLY)){
            List<OperationData> operations = operationDataRepository
                    .findFirstBySeamstressIdAndDateAndTaskId(seamstressId, LocalDate.now(), task.getId());
            if (!operations.isEmpty()) {
                Duration duration = operations.get(0).getHoursWorked();
                operationDto.setHours((int)duration.toHours());
                operationDto.setMinutes((int)duration.toMinutes() % 60);
            }
        }

        return operationDto;
    }

    @Override
    @Transactional
    public void saveOrUpdateOperations(List<InOperationDto> inOperationDtos) {

        //Получаем текущую швею
        User user = customUserDetailsService.getCurrentUser();

        for (InOperationDto inOperationDto : inOperationDtos) {

            // Получаем задачи
            Task task = taskRepository.getReferenceById(inOperationDto.getTaskId());

            // Проверяем, не обнулил ли пользователь количество операций по задаче.
            if (inOperationDto.getOperations() == 0 && !task.getTaskType().equals(HOURLY)) {
                if (operationDataRepository.existsByTaskAndSeamstressAndDateAndNameMaterial(task, user, LocalDate.now(), inOperationDto.getNameMaterial())){
                    operationDataRepository.deleteByTaskAndSeamstressAndDate(task, user, LocalDate.now());
                }
                continue;
            }

            // Проверяем, не обнулил ли пользователь количество времени по задаче.
            if (inOperationDto.getHours() == 0 && inOperationDto.getMinutes() == 0 && task.getTaskType().equals(HOURLY)) {
                if (operationDataRepository.existsByTaskAndSeamstressAndDateAndNameMaterial(task, user, LocalDate.now(), inOperationDto.getNameMaterial())){
                    operationDataRepository.deleteByTaskAndSeamstressAndDate(task, user, LocalDate.now());
                }
                continue;
            }

            // Если запись за текущие сутки существует, обновляем ее.
            if (operationDataRepository.existsByTaskAndSeamstressAndDateAndNameMaterial(task, user, LocalDate.now(), inOperationDto.getNameMaterial())){
                operationDataRepository.updateCompletedOperationsAndHoursWorkedByTaskAndSeamstressAndDate(
                        task, user, LocalDate.now(), inOperationDto.getOperations(),
                        Duration.ofHours(inOperationDto.getHours()).plusMinutes(inOperationDto.getMinutes()));
                continue;
            }

            OperationData operationData = new OperationData();

            operationData.setCategory(task.getCategory());
            operationData.setTaskType(task.getTaskType());
            operationData.setTask(task);
            operationData.setNameMaterial(inOperationDto.getNameMaterial());
            operationData.setSeamstress(user);
            if (!task.getTaskType().equals(HOURLY)) {
                operationData.setCostPerPiece(task.getCostPerPiece());
                operationData.setCompletedOperations(inOperationDto.getOperations());
            }
            if (task.getTaskType().equals(HOURLY)) {
                operationData.setHourlyRate(user.getHourlyRate());
                operationData.setHoursWorked(Duration.ofHours(inOperationDto.getHours()).plusMinutes(inOperationDto.getMinutes()));
            }
            operationData.setSalary(user.getSalary() != null ? user.getSalary() : BigDecimal.valueOf(0));
            operationData.setDate(LocalDate.now());


            operationDataRepository.save(operationData);
        }
    }

    @Override
    public BigDecimal getEarningsForSeamstressInPeriod(Long seamstressId, LocalDate startDate, LocalDate endDate){
        return operationDataRepository.calculateEarningsForSeamstressInPeriod(seamstressId, startDate, endDate);
    }

    @Override
    public List<SeamstressDto> getSeamstressDtosList(LocalDate startDate, LocalDate endDate){
        List<User> users = customUserDetailsService.getAllUsers();
        List<SeamstressDto> seamstressDtos = new ArrayList<>();
        for (User user: users) {
            if (user.getRoles().stream().anyMatch(role -> !role.getName().equals("ROLE_USER"))) {
                continue;
            }
            SeamstressDto seamstressDto = new SeamstressDto();
            seamstressDto.setSeamstressId(user.getId());
            seamstressDto.setSeamstressName(user.getName());
            List<BigDecimal> earnings = getEarningsByDateRange(user.getId(), startDate, endDate);
            seamstressDto.setEarnings(earnings);
            seamstressDto.setAmountOfEarnings(earnings.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
            seamstressDtos.add(seamstressDto);
        }

        return seamstressDtos;
    }

    @Override
    public List<EarningsDto> getEarningsDtosList(LocalDate startDate, LocalDate endDate, List<Category> categories) {

        List<User> users = customUserDetailsService.getAllUsers();
        List<EarningsDto> earningsDtos = new ArrayList<>();
        for (User user: users) {
            if (user.getRoles().stream().anyMatch(role -> !role.getName().equals("ROLE_USER"))) {
                continue;
            }


            EarningsDto earningsDto = new EarningsDto();

            earningsDto.setSeamstressId(user.getId());
            earningsDto.setSeamstressName(user.getName());
            //earningsDto.setCategory(category);
            earningsDto.setPaymentsByDateList(getPaymentsByDateList(startDate, endDate, user.getId(), categories));
            earningsDto.setTotalAmount(
                    earningsDto.getPaymentsByDateList().stream()
                    .map(PaymentsByDate::getQuantitativePayments)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(earningsDto.getPaymentsByDateList().stream()
                            .map(PaymentsByDate::getHourlyPayments)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .add(earningsDto.getPaymentsByDateList().stream()
                            .map(PaymentsByDate::getPackagingPayments)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
            );

            if (user.getSalary() != null && user.getSalary().compareTo(BigDecimal.ZERO) != 0 && !earningsDto.getPaymentsByDateList().isEmpty()) {
                earningsDto.setSalary(user.getSalary().divide(new BigDecimal("21"), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(earningsDto.getPaymentsByDateList().size())).setScale(2, RoundingMode.HALF_UP));
            } else {
                earningsDto.setSalary(new BigDecimal("0.00"));
            }

            earningsDto.setResult(earningsDto.getTotalAmount().subtract(earningsDto.getSalary()).setScale(2, RoundingMode.HALF_UP));

            earningsDtos.add(earningsDto);
        }


        return earningsDtos;
    }

    @Override
    public List<EarningsDto> getCommonEarningsDtosList(LocalDate startDate, LocalDate endDate) {
        List<User> users = customUserDetailsService.getAllUsers();
        List<EarningsDto> earningsDtos = new ArrayList<>();

        for (User user : users) {
            if (user.getRoles().stream().anyMatch(role -> !role.getName().equals("ROLE_USER"))) {
                continue;
            }

            EarningsDto earningsDto = new EarningsDto();
            earningsDto.setSeamstressId(user.getId());
            earningsDto.setSeamstressName(user.getName());

            // Общий список платежей для всех категорий

            List<PaymentsByDate> allPayments = new ArrayList<>(getPaymentsByDateList(startDate, endDate, user.getId(), null));

            earningsDto.setPaymentsByDateList(allPayments);

            // Считаем общую сумму
            earningsDto.setTotalAmount(
                    earningsDto.getPaymentsByDateList().stream()
                            .map(PaymentsByDate::getQuantitativePayments)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .add(earningsDto.getPaymentsByDateList().stream()
                                    .map(PaymentsByDate::getHourlyPayments)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .add(earningsDto.getPaymentsByDateList().stream()
                                    .map(PaymentsByDate::getPackagingPayments)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .setScale(2, RoundingMode.HALF_UP) // Установите два знака после запятой
            );

            if (user.getSalary() != null && user.getSalary().compareTo(BigDecimal.ZERO) != 0 && allPayments.size() != 0) {
                earningsDto.setSalary(user.getSalary().divide(new BigDecimal("21"), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(allPayments.size())).setScale(2, RoundingMode.HALF_UP));
            } else {
                earningsDto.setSalary(new BigDecimal("0.00"));
            }


            earningsDto.setResult(earningsDto.getTotalAmount().subtract(earningsDto.getSalary()).setScale(2, RoundingMode.HALF_UP));

            earningsDtos.add(earningsDto);
            }

        return earningsDtos;
    }


    private List<PaymentsByDate> getPaymentsByDateList(LocalDate startDate, LocalDate endDate, Long seamstressId, List<Category> categories) {
        List<OperationData> operationDataList = operationDataRepository.findBetweenDatesAndBySeamstressAndCategories(startDate, endDate, seamstressId, categories);


        List<PaymentsByDate> paymentsByDateList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            PaymentsByDate paymentsByDate = new PaymentsByDate();

            paymentsByDate.setDate(currentDate);

            LocalDate finalCurrentDate = currentDate;

            paymentsByDate.setQuantitativePayments(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.QUANTITATIVE.equals(operationData.getTaskType()))
                            .map(operationData -> operationData.getCostPerPiece().multiply(BigDecimal.valueOf(operationData.getCompletedOperations())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            paymentsByDate.setHourlyPayments(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.HOURLY.equals(operationData.getTaskType()))
                            .map(operationData -> {
                                Duration hoursWorked = operationData.getHoursWorked();
                                BigDecimal hourlyRate = operationData.getHourlyRate();
                                long hours = hoursWorked.toHours();
                                long minutes = hoursWorked.minusHours(hours).toMinutes();
                                BigDecimal totalHours = BigDecimal.valueOf(hours)
                                        .add(BigDecimal.valueOf(minutes)
                                                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                                return hourlyRate.multiply(totalHours).setScale(2, RoundingMode.HALF_UP);
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );


            paymentsByDate.setPackagingPayments(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.PACKAGING.equals(operationData.getTaskType()))
                            .map(operationData -> operationData.getCostPerPiece().multiply(BigDecimal.valueOf(operationData.getCompletedOperations())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            paymentsByDateList.add(paymentsByDate);
            currentDate = currentDate.plusDays(1);
        }

        return paymentsByDateList;
    }

    @Override
    public List<String> getDatesInPeriod(LocalDate startDate, LocalDate endDate) {
        List<String> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy E"); // Формат даты и дня недели

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String formattedDate = currentDate.format(formatter);
            result.add(formattedDate);

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    private List<BigDecimal> getEarningsByDateRange(Long seamstressId, LocalDate startDate, LocalDate endDate) {
        List<BigDecimal> earnings = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal earning = operationDataRepository.getEarningsByDate(seamstressId, currentDate);
            earnings.add(earning);

            currentDate = currentDate.plusDays(1);
        }

        return earnings;
    }

    @Override
    public List<WorkedDto> getWorkedDtosList(LocalDate startDate, LocalDate endDate) {

        List<User> users = customUserDetailsService.getAllUsers();
        List<WorkedDto> workedDtos = new ArrayList<>();
        List<Category> categories = categoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .toList();
        for (User user: users) {
            if (user.getRoles().stream().anyMatch(role -> !role.getName().equals("ROLE_USER"))) {
                continue;
            }

            for (Category category : categories) {
                WorkedDto workedDto = new WorkedDto();

                workedDto.setSeamstressId(user.getId());
                workedDto.setSeamstressName(user.getName());
                workedDto.setCategory(category);
                workedDto.setWorkedByDateList(getWorkedByDateList(startDate, endDate, user.getId(), category));
                workedDto.setTotalWorked(workedSum(workedDto.getWorkedByDateList()));

                workedDtos.add(workedDto);
            }
        }
        return workedDtos;
    }

    @Override
    public SeamstressDto getSeamstressDto(LocalDate startDate, LocalDate endDate) {
        User user = customUserDetailsService.getCurrentUser(); // Предположим, что у вас есть доступ к пользователю

        SeamstressDto seamstressDto = new SeamstressDto();
        seamstressDto.setSeamstressId(user.getId());
        seamstressDto.setSeamstressName(user.getName());

        List<BigDecimal> earnings = getEarningsByDateRange(user.getId(), startDate, endDate);
        seamstressDto.setEarnings(earnings);
        seamstressDto.setAmountOfEarnings(earnings.stream().reduce(BigDecimal.ZERO, BigDecimal::add));

        return seamstressDto;
    }

    private List<WorkedByDate> getWorkedByDateList(LocalDate startDate, LocalDate endDate, Long seamstressId, Category category) {
        List<OperationData> operationDataList = operationDataRepository.findBetweenDatesAndBySeamstressAndCategory(startDate, endDate, seamstressId, category);

        List<WorkedByDate> workedByDateList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            WorkedByDate workedByDate = new WorkedByDate();

            workedByDate.setDate(currentDate);

            LocalDate finalCurrentDate = currentDate;

            workedByDate.setQuantitativeWorked(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.QUANTITATIVE.equals(operationData.getTaskType()))
                            .mapToInt(OperationData::getCompletedOperations)
                            .sum()
            );

            workedByDate.setHourlyWorked(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.HOURLY.equals(operationData.getTaskType()))
                            .map(OperationData::getHoursWorked)
                            .reduce(Duration.ZERO, Duration::plus)
            );

            workedByDate.setHourlyWorkedToString(String.format("%02d:%02d",
                    workedByDate.getHourlyWorked().toHours(),
                    workedByDate.getHourlyWorked().toMinutesPart()
            ));
//workedByDate.setHourlyWorkedToString(workedByDate.getHourlyWorked().stream().map(d -> String.format("%02d:%02d", d.toHours(), d.toMinutesPart()))
//                    .orElse("00:00"));

            workedByDate.setPackagingWorked(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.PACKAGING.equals(operationData.getTaskType()))
                            .mapToInt(OperationData::getCompletedOperations)
                            .sum()
            );

            workedByDateList.add(workedByDate);
            currentDate = currentDate.plusDays(1);
        }

        return workedByDateList;
    }

    public static WorkedByDate workedSum(List<WorkedByDate> workedByDateList) {
        WorkedByDate sumWorkedByDate = new WorkedByDate();
        sumWorkedByDate.setDate(null);
        sumWorkedByDate.setQuantitativeWorked(
                workedByDateList.stream()
                        .mapToInt(WorkedByDate::getQuantitativeWorked)
                        .sum()
        );
        sumWorkedByDate.setHourlyWorked(
                workedByDateList.stream()
                        .map(WorkedByDate::getHourlyWorked)
                        .reduce(Duration.ZERO, Duration::plus)
        );

        sumWorkedByDate.setHourlyWorkedToString(String.format("%02d:%02d",
                sumWorkedByDate.getHourlyWorked().toHours(),
                sumWorkedByDate.getHourlyWorked().toMinutesPart()
        ));


        sumWorkedByDate.setPackagingWorked(
                workedByDateList.stream()
                        .mapToInt(WorkedByDate::getPackagingWorked)
                        .sum()
        );
        return sumWorkedByDate;
    }
}

