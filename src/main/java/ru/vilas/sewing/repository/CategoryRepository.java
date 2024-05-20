package ru.vilas.sewing.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vilas.sewing.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Object> findByName(String categoryId);

    @NotNull
    @Query("SELECT c FROM Category c ORDER BY c.name")
    List<Category> findAll();

    List<Category> findByCustomerId(Long customerId);
    @Query("SELECT c.id FROM Category c WHERE c.name = :name")
    Long findIdByName(@Param("name") String name);

    @Query("SELECT c.name FROM Category c WHERE c.id = :id")
    String findNameById(@Param("id") Long id);


}

