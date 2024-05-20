package ru.vilas.sewing.service.admin;

import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.WorkedOperationDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.OperationDataRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class AdminOperationServiceImpl implements AdminOperationService {

    private final  OperationDataRepository operationDataRepository;

    public AdminOperationServiceImpl(OperationDataRepository operationDataRepository) {
        this.operationDataRepository = operationDataRepository;
    }

    @Override
    public List<OperationData> findByCategoryIdAndSeamstressIdAndDateBetween(Long categoryId, Long seamstressId, LocalDate startDate, LocalDate endDate) {
        // Фильтр по категории
        Predicate<OperationData> categoryFilter = (categoryId != null && categoryId != 0) ?
                operationData -> operationData.getCategory().getId().equals(categoryId) :
                operationData -> true;

        // Фильтр по швее
        Predicate<OperationData> seamstressFilter = (seamstressId != null && seamstressId != 0) ?
                operationData -> operationData.getSeamstress().getId().equals(seamstressId) :
                operationData -> true;

        Predicate<OperationData> userRoleFilter = operationData -> operationData.getSeamstress().getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_USER"));

        // Фильтр по датам
        Predicate<OperationData> dateFilter = operationData ->
                (startDate == null || operationData.getDate().isAfter(startDate) || operationData.getDate().isEqual(startDate)) &&
                        (endDate == null || operationData.getDate().isBefore(endDate) || operationData.getDate().isEqual(endDate));

        return operationDataRepository.findAll().stream()
                .filter(categoryFilter
                        .and(seamstressFilter)
                        .and(userRoleFilter)
                        .and(dateFilter))
                .sorted(Comparator.comparing(OperationData::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOperation(Long id) {
        operationDataRepository.deleteById(id);
    }

    @Override
    public void saveOperationData(OperationData operationData) {
        operationDataRepository.save(operationData);
    }

    @Override
    public OperationData getOperationById(Long id) {
        return operationDataRepository.findById(id).orElse(null);
    }

    @Override
    public void editOperation(Long id, OperationData updatedOperation) {
        OperationData existingOperation = operationDataRepository.findById(id).orElse(null);

        if (existingOperation != null) {
            //  обновление данных
            existingOperation.setDate(updatedOperation.getDate());
            existingOperation.setSeamstress(updatedOperation.getSeamstress());
            existingOperation.setTaskType(updatedOperation.getTaskType());
            existingOperation.setCategory(updatedOperation.getCategory());
            existingOperation.setTask(updatedOperation.getTask());
            existingOperation.setCostPerPiece(updatedOperation.getCostPerPiece());
            existingOperation.setHourlyRate(updatedOperation.getHourlyRate());
            existingOperation.setCompletedOperations(updatedOperation.getCompletedOperations());
            existingOperation.setHoursWorked(updatedOperation.getHoursWorked());
            existingOperation.setNameMaterial(updatedOperation.getNameMaterial());
            operationDataRepository.save(existingOperation);
        }
    }

    @Override
    public List<WorkedOperationDto> findByCategoryListAndSeamstressIdAndDateBetween(List<Category> categories, Long seamstressId, LocalDate startDate, LocalDate endDate) {

        List<OperationData> operations =  operationDataRepository.findAllByDateBetween(startDate, endDate);

        if(seamstressId != null && seamstressId != 0){
            operations = operations.stream().filter(o -> o.getSeamstress().getId() == seamstressId).toList();
        }

        operations = operations.stream().filter(operation -> categories.contains(operation.getCategory())).toList();

        Map<TaskAndUser, List<OperationData>> operationGroups = operations.stream()
                    .collect(Collectors.groupingBy(
                            operation -> new TaskAndUser(operation.getTask(), operation.getSeamstress())
                    ));

//        List<WorkedOperationDto> workedOperationDtos = operationGroups.entrySet().stream()
//                    .map(entry -> createDtoFromGroup(entry.getKey(), entry.getValue(), startDate, endDate))
//                    .toList();;

        List<WorkedOperationDto> workedOperationDtos = operationGroups.entrySet().stream()
                .map(entry -> createDtoFromGroup(entry.getKey(), entry.getValue(), startDate, endDate))
                .sorted(Comparator.comparing(dto -> dto.getTask().getName())) // Сортировка по названию задачи
                .collect(Collectors.toList());

        return workedOperationDtos;
    }

    @Override
    public List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (currentDate.isBefore(endDate) || currentDate.equals(endDate)) {
            dates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return dates;
    }

    private WorkedOperationDto createDtoFromGroup(TaskAndUser key, List<OperationData> operations, LocalDate startDate, LocalDate endDate) {

        WorkedOperationDto dto = new WorkedOperationDto();

        // Установка других полей DTO
        dto.setId(operations.get(0).getId());
        dto.setTaskType(operations.get(0).getTaskType());
        dto.setCategory(operations.get(0).getCategory());
        dto.setTask(key.getTask());
        dto.setSeamstress(key.getSeamstress());
        dto.setNormPerShift(dto.getTask().getNormPerShift());

        //Количество выполненных задач по датам
        List<Integer> operationsByDate = new ArrayList<>();
        getDatesBetween(startDate, endDate).forEach(d -> {
            operationsByDate.add(operations.stream().filter(o -> o.getDate().equals(d))
                    .map(OperationData::getCompletedOperations).findFirst().orElse(0));
        });
        dto.setOperations(operationsByDate);

        // Сумма выполненных операций
        dto.setSumOfOperations(operationsByDate.stream()
                .mapToInt(o -> o.intValue())
                .sum());

        // Затраченное время по датам
        List<LocalDate> dates = getDatesBetween(startDate, endDate);
        List<Duration> timeByDate = new ArrayList<>();
        dates.forEach(d -> {
            Duration durationForDate = operations.stream()
                    .filter(o -> o.getDate().equals(d))
                    .map(OperationData::getHoursWorked)
                    .filter(Objects::nonNull) // Фильтруем, чтобы исключить null значения
                    .reduce(Duration::plus) // Суммируем все Duration для данной даты
                    .orElse(Duration.ZERO); // Используем Duration.ZERO если нет соответствующих данных
            timeByDate.add(durationForDate);
        });
        // Конвертируем в строку
        List<String> timeByDateString = timeByDate.stream()
                .map(t -> String.format("%02d:%02d", t.toHours(), t.toMinutesPart()))
                .collect(Collectors.toList());

        dto.setDurations(timeByDateString);


        // Сумма времени
        Duration totalDuration = timeByDate.stream()
                .reduce(Duration.ZERO, Duration::plus);
        dto.setSumOfTime(formatDuration(totalDuration));

        return dto;
    }

    // Duration в строку
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        int minutes = duration.toMinutesPart();
        return String.format("%dh %02dm", hours, minutes);
    }

}
