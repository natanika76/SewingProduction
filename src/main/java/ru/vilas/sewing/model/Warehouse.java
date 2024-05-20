package ru.vilas.sewing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vilas.sewing.dto.UnitsOfMeasurement;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Warehouse implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean archive = false;
    private String nameMaterial;
    @Enumerated(EnumType.STRING)
    UnitsOfMeasurement unitsOfMeasurement;        // единицы измерения
    private Integer quantityOfMaterial;        // количество материала
    private String expenditure;                //расход материала
    private Integer numberOfProducts;          //количество изделий
    private Integer target;                     //задание
    private Integer balance;                    // баланс
    private Integer normPerDay;                  // норма в день
    private Integer inTotal;                    // итого
    private Integer remains;                    // остаток
    private LocalDate startWork;
    private LocalDate endWork;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
   }
