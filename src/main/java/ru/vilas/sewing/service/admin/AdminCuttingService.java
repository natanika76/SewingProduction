package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.dto.ChartCuttingDto;
import ru.vilas.sewing.dto.CuttingDto;
import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.model.User;

import java.time.LocalDate;
import java.util.List;

public interface AdminCuttingService {
    boolean saveCuttingFromDto(CuttingDto cuttingDto);
    boolean savePackagingFromDto (CuttingDto cuttingDto);
    List<WarehouseDto> showAllActiveTasksCut(Long customerId, Long categoryId, String nameMaterial);
    List<SizeByDateDto> showCuttingProcess (Long warehouseId, LocalDate startDate, LocalDate endDate);

    List<User> showSeamstress (Long warehouseId);
    List<ChartCuttingDto> showChartSeamstress(Long warehouseId,LocalDate startDate, LocalDate endDate, Long seamstressId);
    List<ChartCuttingDto> showChartPackagingSeamstress(Long warehouseId, LocalDate startDate, LocalDate endDate, Long seamstressId);
}
