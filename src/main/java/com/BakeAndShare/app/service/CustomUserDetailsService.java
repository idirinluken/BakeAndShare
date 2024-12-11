package com.BakeAndShare.app.service;

import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar el usuario por su email (username)
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));

        // Crear y retornar un objeto UserDetails con el email, la contraseña cifrada y el rol del usuario
        return User.builder()
                .username(usuario.getDatosUsuario().getEmail()) // Usamos el email como username
                .password(usuario.getPassword())  // Contraseña cifrada
                .roles(usuario.getRol() == 0 ? "USER" : "ADMIN")  // Asignamos el rol
                .build();
    }
}
