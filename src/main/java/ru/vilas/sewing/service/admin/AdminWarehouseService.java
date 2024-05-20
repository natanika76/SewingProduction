package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.SizeByDate;
import ru.vilas.sewing.model.Warehouse;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

public interface AdminWarehouseService {
    List<WarehouseDto> showAllActiveTasksCut(Long customerId, Long categoryId, String nameMaterial);
    Warehouse convertToEntity(WarehouseDto warehouseDto);
    Warehouse getWarehouseById(Long id);

    List<SizeByDateDto> getSizeByDateById(Long id);
    void updatedWarehouse(WarehouseDto warehouse);
    Boolean checkingForExistence(WarehouseDto warehouseDto);

    void saveSizeByDate(WarehouseDto warehouseDto, Long  warehouseId);
    Boolean checkingForEdit(WarehouseDto warehouseDto, Long id);
    List<String> showNameMaterialByCategory(Long categoryId);
    List<String> showNameMaterial();
}
