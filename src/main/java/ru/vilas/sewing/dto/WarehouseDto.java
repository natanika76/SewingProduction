package ru.vilas.sewing.dto;

import lombok.Data;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ru.vilas.sewing.model.SizeByDate;

@Data
public class WarehouseDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private Customer customer;
    private Category category;
    private String categoryName;
    private String nameMaterial;
    private Integer quantityOfMaterial;
    private String expenditure;
    private Integer numberOfProducts;
    private Integer balance;
    private Integer normPerDay;
    private Integer inTotal;
    private Integer remains;
    private LocalDate startWork;
    private LocalDate endWork;
    private Integer target;
    private UnitsOfMeasurement unitsOfMeasurement;
    private List<String> size;
    private List<String> height;
    private List<Integer> quantity;
    private Integer done;
    private Integer plan;
    private Integer packaging;
    private Integer shipment;
}
