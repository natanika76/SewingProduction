package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.User;

import java.time.LocalDate;
import java.util.List;

public interface AdminShipmentService {
    List<WarehouseDto> showAllActiveTasksCutEndShipment(Long customerId, Long categoryId, String nameMaterial);
    List<SizeByDateDto> addShipmentProcess(Long warehouseId);
    WarehouseDto showShipmentWarehouse(Long warehouseId);
    boolean saveShipmentFromDto (List<CuttingDto> cuttingDtos);

    List<AnalyticsDto> showAnalytics(Long warehouseId);
    AnalyticsDto showTotalAnalytics(Long warehouseId);
    Integer workingPeriod(Long warehouseId);
    Integer   periodCurrentDate(Long warehouseId);
    Integer shipmentForecastForTheEndDate(Long warehouseId);
    Integer forecastOfNumberDays(Long warehouseId);
    Integer  recommendedShipmentRatePerDay(Long warehouseId);
    List<AnalyticsDto>  chartShowAnalytics(Long warehouseId);
    List<ChartCuttingDto> showChartPackaging(Long warehouseId, LocalDate startDate, LocalDate endDate, Long seamstressId);
    List<User> showSeamstress (Long warehouseId);
    List<ChartTaskDto> showChartTaskQuantitative(Long warehouseId, Long seamstressId);
    List<ChartTaskDto> showChartTaskHourly (Long warehouseId, Long seamstressId);
    List<ChartTaskDto> showChartTaskPackaging(Long warehouseId, Long seamstressId);
    List<User> showSeamstressFull (Long warehouseId);
}
