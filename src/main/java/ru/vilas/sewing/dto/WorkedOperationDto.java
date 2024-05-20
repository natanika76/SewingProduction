package ru.vilas.sewing.dto;

import jakarta.persistence.*;
import lombok.Data;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Data
public class WorkedOperationDto {

    private Long id;

    TaskTypes taskType;

    private Category category;

    private Task task;

    private User seamstress;

    private Integer normPerShift;

    private List<Integer> operations;

    private List<String> durations;

    private Integer sumOfOperations;

    private String sumOfTime;

}
