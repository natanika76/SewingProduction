package ru.vilas.sewing.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Cutting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate dateWork;
    private Integer quantity;
    private Long seamstressId;
    private Long warehouseId;
    private Long sizeByDateId;

}
