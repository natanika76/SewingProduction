package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.dto.WorkedOperationDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.User;

import java.time.LocalDate;
import java.util.List;

public interface AdminOperationService {

    List<OperationData> findByCategoryIdAndSeamstressIdAndDateBetween(Long categoryId, Long seamstressId, LocalDate startDate, LocalDate endDate);

    void deleteOperation(Long id);

    void saveOperationData(OperationData operationData);

    OperationData getOperationById(Long id);

    void editOperation(Long id, OperationData operationData);

    List<WorkedOperationDto> findByCategoryListAndSeamstressIdAndDateBetween(List<Category> categories, Long seamstressId, LocalDate startDate, LocalDate endDate);

    Object getDatesBetween(LocalDate startDate, LocalDate endDate);
}
