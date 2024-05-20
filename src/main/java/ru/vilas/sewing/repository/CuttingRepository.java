package ru.vilas.sewing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.vilas.sewing.dto.CuttingDto;
import ru.vilas.sewing.model.Cutting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CuttingRepository extends JpaRepository<Cutting, Long> {
  //Вернёт все записи по id заказа
    List<Cutting> findAllByWarehouseId(Long warehouseId);

  @Query("SELECT c FROM Cutting c WHERE c.dateWork = :dateWork AND c.warehouseId = :warehouseId AND c.sizeByDateId = :sizeByDateId")
  List<Cutting> findByDateWorkAndWarehouseIdAndSizeByDateId(@Param("dateWork") LocalDate dateWork,
                                                            @Param("warehouseId") Long warehouseId,
                                                            @Param("sizeByDateId") Long sizeByDateId);

  @Query(value = "SELECT * FROM cutting WHERE size_by_date_id = :sizeByDateId", nativeQuery = true)
  List<Cutting> findBySizeByDateId(@Param("sizeByDateId") Long sizeByDateId);


  @Query(value = "SELECT * FROM cutting WHERE date_work = :dateWork AND size_by_date_id = :sizeByDateId", nativeQuery = true)
  List<Cutting> findByDateWorkAndSizeByDateId(@Param("dateWork") LocalDate dateWork,
                                        @Param("sizeByDateId") Long sizeByDateId);

  @Query("SELECT c FROM Cutting c WHERE c.dateWork = :dateWork AND c.sizeByDateId = :sizeByDateId AND c.seamstressId = :seamstressId")
  Cutting findByDateWorkAndSizeByDateIdAndSeamstressId(@Param("dateWork") LocalDate dateWork,
                                                             @Param("sizeByDateId") Long sizeByDateId,
                                                             @Param("seamstressId") Long seamstressId);

  @Query("SELECT DISTINCT c.seamstressId FROM Cutting c WHERE c.warehouseId = :warehouseId")
  List<Long> findSeamstressIdsByWarehouseId(Long warehouseId);

  boolean existsByDateWorkAndSeamstressIdAndWarehouseId(LocalDate dateWork, Long seamstressId, Long warehouseId);

  Optional<Cutting> findByDateWorkAndSizeByDateIdAndSeamstressIdAndWarehouseId(
          LocalDate dateWork, Long sizeByDateId, Long seamstressId, Long warehouseId);

  Optional<Cutting> findById(Long id);

  @Modifying
  @Transactional
  @Query("UPDATE Cutting c SET c.quantity = :quantity, c.dateWork = :dateWork WHERE c.id = :id")
  void updateQuantityAndDateWorkById(Long id, Integer quantity, LocalDate dateWork);

  @Modifying
  @Transactional
  @Query("DELETE FROM Cutting c WHERE c.id = :id")
  void deleteById(Long id);

  @Modifying
  @Transactional
  @Query("UPDATE Cutting c SET c.quantity = :quantity WHERE c.id = :id")
  void updateQuantityById(@Param("quantity") Integer quantity, @Param("id") Long id);

}

