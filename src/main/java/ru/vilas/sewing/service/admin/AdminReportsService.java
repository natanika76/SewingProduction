package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.dto.CuttingDto;
import ru.vilas.sewing.dto.ReportsDto;

import java.util.List;

public interface AdminReportsService {
    List<ReportsDto> getCuttingDtos(List<Long> warehouseIds);

    List<Long> getWarehouseId(Long customerId, Long categoryId);
    ReportsDto editCutting(Long cuttingId);
}
