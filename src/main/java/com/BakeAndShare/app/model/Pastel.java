package com.BakeAndShare.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pastel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroPedido;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToOne(mappedBy = "pastel", cascade = CascadeType.ALL)
    private Receta receta;

    @ManyToMany
    @JoinTable(
        name = "pastel_cocinero",
        joinColumns = @JoinColumn(name = "pastel_id"),
        inverseJoinColumns = @JoinColumn(name = "cocinero_id")
    )
    private List<Cocinero> cocineros;
}