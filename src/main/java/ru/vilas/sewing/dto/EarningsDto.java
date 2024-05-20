package ru.vilas.sewing.dto;

import lombok.Data;
import ru.vilas.sewing.model.Category;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EarningsDto {

    private Long seamstressId;
    private String seamstressName;
    private Category category;
    private List<PaymentsByDate> paymentsByDateList;
    private BigDecimal totalAmount;
    private BigDecimal salary;
    private BigDecimal result;

}
