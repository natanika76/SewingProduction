package ru.vilas.sewing.dto;

import lombok.Getter;

@Getter
public enum UnitsOfMeasurement {
    RUNNINGMETERS("метры погонные","м.п."),
    SQUAREMETERS("метры квадратные","м.кв."),
    KILOGRAMS("килограммы","кг.");

    private final String russianName;
    private final String abbreviatedName;

    UnitsOfMeasurement(String russianName, String abbreviatedName) {
        this.russianName = russianName;
        this.abbreviatedName = abbreviatedName;
    }

}
