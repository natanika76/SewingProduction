package ru.vilas.sewing.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class ChartCuttingDto {
    private String seamstressName;
    private LocalDate date;
    private Integer quantity;
    private Integer total;
}
