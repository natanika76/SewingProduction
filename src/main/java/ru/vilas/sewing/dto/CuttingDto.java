package ru.vilas.sewing.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CuttingDto {
    private LocalDate dateWork;
    private Integer quantity;
    private Long warehouseId;
    private Long sizeByDateId;
    private List<Long> sizeByDatesId;
    private Long seamstressId;
    private List<Integer> quantityFull;
}
