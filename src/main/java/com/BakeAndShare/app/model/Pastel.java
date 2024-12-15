package com.BakeAndShare.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import org.hibernate.annotations.Check;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pastel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
        name = "usuario_pastel",
        joinColumns = @JoinColumn(name = "pastel_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> usuarios;

    @ManyToOne
    @JoinColumn(name = "cocinero_id") // Clave foránea para Cocinero
    private Cocinero cocinero;

    @OneToOne(mappedBy = "pastel", cascade = CascadeType.ALL)
    private Receta receta;

    @Check(constraints = "precio > 0")
    private double precio;  // Precio del pastel
    private String descripcion;  // Descripción del pastel

    public void setCocinero(Cocinero cocinero) {
        this.cocinero = cocinero;
    }
}
