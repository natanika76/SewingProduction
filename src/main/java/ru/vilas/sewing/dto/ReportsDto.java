package ru.vilas.sewing.dto;

import lombok.Data;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;

import java.time.LocalDate;

@Data
public class ReportsDto {
    private LocalDate dateWork;
    private Integer quantity;
    private Integer done;
    private Long cuttingId;
    private Long warehouseId;
    private Long sizeByDateId;
    private Long seamstressId;
    private String seamstressName;
    private String size;
    private String height;
    private String customerName;
    private Customer customer;
    private Category category;
    private String categoryName;
    private String nameMaterial;
}
