package ru.vilas.sewing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Type;
import ru.vilas.sewing.dto.TaskTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Entity
@Data
@ToString
public class OperationData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Category category;

    @Enumerated(EnumType.STRING)
    TaskTypes taskType;

    @ManyToOne
    private Task task;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "seamstress_id", nullable = false)
    private User seamstress;

    private BigDecimal costPerPiece;

    private BigDecimal hourlyRate;

    private BigDecimal salary;

    private LocalDate date;

    private int completedOperations;

    private Duration hoursWorked;

    private String nameMaterial;
}

