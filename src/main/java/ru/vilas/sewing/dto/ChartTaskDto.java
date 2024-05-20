package ru.vilas.sewing.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ChartTaskDto {
    private String taskName;
    private int normPerShift;
    private int done;
    private int inTotal;
    private List<Integer> completedOperations;
    private List<List<Integer>> completedOperationsTwo;
    private List<LocalDate> dateRange;
    private List <Long> minutes;
}
