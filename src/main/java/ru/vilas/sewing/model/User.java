package ru.vilas.sewing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = true)
    private String email;
    @Column(nullable = false)
    private String password;
    BigDecimal hourlyRate;
    BigDecimal salary;

    @JsonIgnore
    @OneToMany(mappedBy = "seamstress", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<OperationData> operations;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @JsonIgnore
    @ToString.Exclude
    private Set<Role> roles;
}

