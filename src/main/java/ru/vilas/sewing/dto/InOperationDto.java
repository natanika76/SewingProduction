package ru.vilas.sewing.dto;

import lombok.Data;


@Data
public class InOperationDto {
    private Long taskId;
    private int operations;
    private int hours;
    private int minutes;
    private String nameMaterial;

}
