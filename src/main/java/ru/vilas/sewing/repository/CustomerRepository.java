package ru.vilas.sewing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;

import java.time.LocalDate;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findCustomerById(Long id);

    @Query("SELECT c.id FROM Customer c WHERE c.name = :name")
    Long findCustomerIdByName(@Param("name") String name);



    @Query("SELECT c.name FROM Customer c WHERE c.id = :id")
    String findNameById(@Param("id") Long id);
}


