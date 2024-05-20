package ru.vilas.sewing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentsByDate {
    private LocalDate date;
    private BigDecimal quantitativePayments;
    private BigDecimal hourlyPayments;
    private BigDecimal packagingPayments;
}
