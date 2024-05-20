package ru.vilas.sewing.service;

import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OperationDataService {
    void saveOperationData(OperationData operationData);

    OperationData getOperationDataById(Long id);

    List<OperationData> getOperationDataByTask(Task task);

    OperationDto convertToOperationDto(Task task, Long seamstressId, String nameMaterial);

    void saveOrUpdateOperations(List<InOperationDto> inoperationDtos);

    BigDecimal getEarningsForSeamstressInPeriod(Long seamstressId, LocalDate startDate, LocalDate endDate);

    List<SeamstressDto> getSeamstressDtosList(LocalDate startDate, LocalDate endDate);

    List<EarningsDto> getEarningsDtosList(LocalDate startDate, LocalDate endDate, List<Category> categories);

    List<EarningsDto> getCommonEarningsDtosList(LocalDate startDate, LocalDate endDate);

    List<String> getDatesInPeriod(LocalDate startDate, LocalDate endDate);

    List<WorkedDto> getWorkedDtosList(LocalDate startDate, LocalDate endDate);
    SeamstressDto getSeamstressDto(LocalDate startDate, LocalDate endDate);


}
