package com.BakeAndShare.app.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosUsuario {
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
}