package ru.vilas.sewing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vilas.sewing.model.Cutting;
import ru.vilas.sewing.model.Packaging;
import ru.vilas.sewing.model.Shipment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    @Query("SELECT s FROM Shipment s WHERE s.warehouseId = :warehouseId")
    List<Shipment> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT s FROM Shipment s WHERE s.sizeByDateId = :sizeByDateId")
    List<Shipment> findBySizeByDateId(@Param("sizeByDateId") Long sizeByDateId);
    List<Shipment> findByDateWorkAndWarehouseId(LocalDate dateWork, Long warehouseId);
    Optional<Shipment> findByDateWorkAndSizeByDateIdAndWarehouseId(
            LocalDate dateWork, Long sizeByDateId, Long warehouseId);

    boolean existsByDateWorkAndWarehouseId(LocalDate dateWork, Long warehouseId);
}
