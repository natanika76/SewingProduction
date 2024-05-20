package ru.vilas.sewing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import ru.vilas.sewing.dto.TaskTypes;

import java.math.BigDecimal;
import java.util.Set;


@Entity
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    TaskTypes taskType;

    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String equipment;

    private int timeInSeconds;

    private BigDecimal costPerPiece;

    private int normPerShift;

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OperationData> operations;

    private Boolean active = true;
}
