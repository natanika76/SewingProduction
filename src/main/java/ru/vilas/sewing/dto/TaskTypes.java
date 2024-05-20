package ru.vilas.sewing.dto;

import lombok.Getter;

@Getter
public enum TaskTypes {
    QUANTITATIVE("Количественная", "Кол-я"),
    HOURLY("Часовая", "Час-я"),
    PACKAGING("Упаковка", "Уп-ка");

    private final String russianName;
    private final String abbreviatedName;

    TaskTypes(String russianName, String abbreviatedName) {
        this.russianName = russianName;
        this.abbreviatedName = abbreviatedName;
    }

}
