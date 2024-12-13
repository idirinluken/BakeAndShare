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

    @ManyToMany(mappedBy = "pasteles")
    private List<Usuario> usuarios; // Lista de usuarios que han pedido este pastel

    @ManyToOne
    @JoinColumn(name = "cocinero_id") // Clave foránea para Cocinero
    private Cocinero cocinero;

    @OneToOne(mappedBy = "pastel", cascade = CascadeType.ALL)
    private Receta receta;
}
