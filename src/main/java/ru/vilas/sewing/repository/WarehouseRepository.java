package ru.vilas.sewing.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.model.Warehouse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    // вернёт все неархивные id
    @Query("SELECT w.id FROM Warehouse w WHERE w.archive = false")
    List<Long> findIdsByArchiveFalse();

    // вернёт все неархивные задачи, если id заказчика равен 0
    @Query("SELECT o FROM Warehouse o WHERE (:customerId is null or :customerId = 0 or o.customer.id = :customerId) AND o.archive = false")
    List<Warehouse> searchByActiveWarehouseArchiveAndCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT w FROM Warehouse w " +
            "WHERE (:customerId is null OR :customerId = 0 OR w.customer.id = :customerId) " +
            "AND (:categoryId is null OR :categoryId = 0 OR w.category.id = :categoryId) " +
            "AND w.archive = false")
    List<Warehouse> searchByActiveWarehouseArchiveAndCustomerIdAndCategoryId(
            @Param("customerId") Long customerId,
            @Param("categoryId") Long categoryId);

    @Query("SELECT w FROM Warehouse w " +
            "WHERE (:customerId is null OR :customerId = 0 OR w.customer.id = :customerId) " +
            "AND (:categoryId is null OR :categoryId = 0 OR w.category.id = :categoryId) " +
            "AND ((:nameMaterial is null OR w.nameMaterial = :nameMaterial) OR (w.nameMaterial = '0')) " +
            "AND w.archive = false")
    List<Warehouse> searchByActiveWarehouseArchiveAndCustomerIdAndCategoryIdAndNameMaterial(
            @Param("customerId") Long customerId,
            @Param("categoryId") Long categoryId,
            @Param("nameMaterial") String nameMaterial);

    @Query("SELECT DISTINCT w.category FROM Warehouse w " +
            "WHERE (:customerId is null OR :customerId = 0 OR w.customer.id = :customerId) " +
            "AND w.archive = false")
    List<Category> findCategoriesByCustomerId(@Param("customerId") Long customerId);

   // вернёт все названия материалов заказчику и категории
   @Query("SELECT DISTINCT w.nameMaterial FROM Warehouse w " +
           "WHERE (:customerId is null OR :customerId = 0 OR w.customer.id = :customerId) " +
           "AND (:categoryId is null OR :categoryId = 0 OR w.category.id = :categoryId)" +
           "AND w.archive = false")
   List<String> findNameMaterialByCustomerIdAndCategoryId(@Param("customerId") Long customerId,
                                                          @Param("categoryId") Long categoryId);


    @Transactional  //добавить задачу в архив
    @Modifying
    @Query("UPDATE Warehouse w SET w.archive = true WHERE w.id = :warehouseId")
    void sendToArchive(@Param("warehouseId") Long warehouseId);

    // вернёт id заказчика по id задачи
    @Query("SELECT w.customer.id FROM Warehouse w WHERE w.id = :warehouseId")
    Long findCustomerIdByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT DISTINCT w.nameMaterial FROM Warehouse w WHERE w.archive = false AND w.category.id = :categoryId")
    List<String> findNameMaterialByArchiveFalseAndCategoryId(@Param("categoryId") Long categoryId);

    //получаем наименование материала в неархивных задачах
    @Query("SELECT DISTINCT w.nameMaterial FROM Warehouse w WHERE w.archive = false")
    List<String> findNameMaterialByArchiveFalse();

    // вернёт id активной задачи по заказчику, категории, наименования материала
    @Query("SELECT w.id FROM Warehouse w " +
            "WHERE w.customer.id = :customerId " +
            "AND w.category.id = :categoryId " +
            "AND w.nameMaterial = :nameMaterial " +
            "AND w.archive = false")
    Long findIdByCustomerCategoryMaterial(
            @Param("customerId") Long customerId,
            @Param("categoryId") Long categoryId,
            @Param("nameMaterial") String nameMaterial);

    @Query("SELECT w.id FROM Warehouse w " +
            "WHERE w.customer.id = :customerId " +
            "AND w.category.id = :categoryId " +
            "AND w.nameMaterial = :nameMaterial " +
            "AND w.archive = false " +
            "AND w.id <> :warehouseId")
    Long findIdByCustomerCategoryMaterialAndWarehouseIdNot(
            @Param("customerId") Long customerId,
            @Param("categoryId") Long categoryId,
            @Param("nameMaterial") String nameMaterial,
            @Param("warehouseId") Long warehouseId);
    @Query("SELECT w.startWork FROM Warehouse w WHERE w.id = :id")
    LocalDate findStartWorkById(@Param("id") Long id);

    @Query("SELECT w.endWork FROM Warehouse w WHERE w.id = :id")
    LocalDate findEndWorkById(@Param("id") Long id);

    @Query("SELECT w.category FROM Warehouse w WHERE w.id = :id")
    Category findCategoryById(@Param("id") Long id);

    @Query("SELECT w.nameMaterial FROM Warehouse w WHERE w.id = :id")
    String findNameMaterialById(@Param("id") Long id);

// вернёт все неархивные заказчики
    @Query("SELECT DISTINCT w.customer FROM Warehouse w WHERE w.archive = false")
    List<Customer> findDistinctCustomersFromNonArchivedWarehouses();

    List<Warehouse> findAllByNameMaterial(String nameMaterial);

    List<Warehouse> findAllByCategoryId(Long categoryId);
    List<Warehouse> findAllByArchiveIsFalse();

    //вернёт все наименования материала
    @Query("SELECT DISTINCT w.nameMaterial FROM Warehouse w")
    List<String> findDistinctNameMaterials();

    Warehouse findById(long id);

   // Вернёт ID заказчика по ID Warehouse
    @Query("SELECT w.customer.id FROM Warehouse w WHERE w.id = :id")
    Long findCustomerIdById(Long id);

    @Query("SELECT w.category.id FROM Warehouse w WHERE w.id = :id")
    Long findCategoryIdById(Long id);

}









