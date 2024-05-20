package ru.vilas.sewing.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AnalyticsDto {
    private String size;
    private String height;
    private Integer quantity;
    private Integer packaging;
    private Integer shipment;
    private Integer remainsWarehouse;
    private Integer remainsShipment;
    private Integer totalQuantity;
    private Integer totalPackaging;
    private Integer totalShipment;
    private Integer totalRemainsWarehouse;
    private Integer totalRemainsShipment;
    private LocalDate date;
}
