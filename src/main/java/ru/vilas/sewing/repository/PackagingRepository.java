package ru.vilas.sewing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.vilas.sewing.model.Cutting;
import ru.vilas.sewing.model.Packaging;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {
    @Query("SELECT p FROM Packaging p WHERE p.warehouseId = :warehouseId")
    List<Packaging> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT p FROM Packaging p WHERE p.sizeByDateId = :sizeByDateId")
    List<Packaging> findBySizeByDateId(@Param("sizeByDateId") Long sizeByDateId);

    boolean existsByDateWorkAndSeamstressIdAndWarehouseId(LocalDate dateWork, Long seamstressId, Long warehouseId);

    Optional<Packaging> findByDateWorkAndSizeByDateIdAndSeamstressIdAndWarehouseId(
            LocalDate dateWork, Long sizeByDateId, Long seamstressId, Long warehouseId);

    @Query("SELECT p FROM Packaging p WHERE p.dateWork = :dateWork AND p.sizeByDateId = :sizeByDateId AND p.seamstressId = :seamstressId")
    Packaging findByDateWorkAndSizeByDateIdAndSeamstressId(@Param("dateWork") LocalDate dateWork,
                                                         @Param("sizeByDateId") Long sizeByDateId,
                                                         @Param("seamstressId") Long seamstressId);

    @Query(value = "SELECT * FROM packaging WHERE date_work = :dateWork AND size_by_date_id = :sizeByDateId", nativeQuery = true)
    List<Packaging> findByDateWorkAndSizeByDateId(@Param("dateWork") LocalDate dateWork,
                                                @Param("sizeByDateId") Long sizeByDateId);
    @Query("SELECT DISTINCT p.seamstressId FROM Packaging p WHERE p.warehouseId = :warehouseId")
    List<Long> findSeamstressIdsByWarehouseId(Long warehouseId);

    List<Packaging> findAllByWarehouseId(Long warehouseId);

    @Modifying
    @Transactional
    @Query("UPDATE Packaging p SET p.quantity = :quantity WHERE p.id = :id")
    void updateQuantityById(@Param("quantity") Integer quantity, @Param("id") Long id);


}
