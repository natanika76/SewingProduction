package ru.vilas.sewing.service;

import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.dto.WorkedByDate;
import ru.vilas.sewing.dto.WorkedDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Warehouse;

import java.time.LocalDate;
import java.util.List;

public interface OperationsListSpecialService {
    List<WorkedDto> getWorkedDtosList(LocalDate startDate, LocalDate endDate);
    List<WorkedByDate> getWorkedByDateList(LocalDate startDate, LocalDate endDate, Long seamstressId,
                                           Category category);

    WarehouseDto showCuttingWarehouse(Long warehouseId);

    List<SizeByDateDto> showTaskCutting(Long id);
    List<SizeByDateDto> showTaskPackaging(Long id);
    Integer totalCutOut (Long warehouseId);
    Integer totalPackaging(Long warehouseId);
}
