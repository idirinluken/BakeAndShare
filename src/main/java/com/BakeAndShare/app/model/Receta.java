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
public class Receta {

    @Id
    private String nombre;

    private int tiempoCoccion; // En minutos
    
    private int temperatura;   // En grados Celsius

    @ElementCollection 
    @CollectionTable(name = "receta_ingredientes", joinColumns = @JoinColumn(name = "receta_nombre"))
    @Column(name = "ingrediente")
    private List<String> ingredientes;

    private String imagenUrl;  // URL o ruta de la imagen asociada a la receta

    @OneToOne(cascade = CascadeType.ALL)  // Añadido cascade ALL para las relaciones de la receta
    @JoinColumn(name = "pastel_id")
    private Pastel pastel;
}
