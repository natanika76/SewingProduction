package ru.vilas.sewing.dto;

import jakarta.persistence.*;
import lombok.Data;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;



@Data
public class OperationDto {
    private Long id;
    private TaskTypes taskType;
    private String name;
    private String equipment;
    private int timeInSeconds;
    private BigDecimal costPerPiece;
    private int normPerShift;
    private Long seamstressId;
    private int operations;
    private int hours;
    private int minutes;

    //private Category category;
    //private LocalDate date;

}
