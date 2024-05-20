package ru.vilas.sewing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class WorkedByDate {
    private LocalDate date;
    private Integer quantitativeWorked;
    private Duration hourlyWorked;
    private String hourlyWorkedToString;
    private Integer packagingWorked;
}
