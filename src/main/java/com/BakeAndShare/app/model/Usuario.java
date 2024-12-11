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
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rol; // 0 = user, 1 = admin

    @Embedded
    private DatosUsuario datosUsuario;

    private String password;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Pastel> pasteles;
}
