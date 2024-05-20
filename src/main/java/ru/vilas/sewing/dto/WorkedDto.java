package ru.vilas.sewing.dto;

import lombok.Data;
import ru.vilas.sewing.model.Category;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WorkedDto {
    private Long seamstressId;
    private String seamstressName;
    private Category category;
    private List<WorkedByDate> workedByDateList;
    private WorkedByDate totalWorked;
}

