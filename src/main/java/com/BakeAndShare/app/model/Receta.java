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
    private List<String> ingredientes;

    @OneToOne
    @JoinColumn(name = "pastel_id")
    private Pastel pastel;
}