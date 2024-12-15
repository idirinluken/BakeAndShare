package com.BakeAndShare.app.controller;

import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.UsuarioRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inyectamos el PasswordEncoder

    // Mostrar lista de usuarios
    // Mostrar lista de usuarios para el administrador
    @GetMapping("/lista-usuarios")
    public String verListaDeUsuarios(Model model) {
        // Validar que el usuario autenticado tiene rol ADMIN
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta página.");
        }

        // Obtener todos los usuarios de la base de datos
        List<Usuario> usuarios = usuarioRepository.findAll();

        // Pasar la lista de usuarios al modelo
        model.addAttribute("usuarios", usuarios);

        return "admin/lista-usuarios"; // Vista que muestra la lista de usuarios
    }


    // Mostrar formulario de registro para un usuario normal
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        // Crear un nuevo objeto Usuario para el formulario
        model.addAttribute("usuario", new Usuario());
        return "registro"; // Vista para el registro de usuario normal
    }

    // Guardar usuario normal
    @PostMapping("/nuevo")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, Model model) {
        // Verificar si el correo ya está registrado
        if (usuarioRepository.findByDatosUsuarioEmail(usuario.getDatosUsuario().getEmail()).isPresent()) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            return "registro"; // Volver al formulario con el error
        }

        // Asignar rol de USER (0) por defecto
        usuario.setRol(0);

        // Cifrar la contraseña
        String encryptedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encryptedPassword);

        // Guardar el nuevo usuario
        usuarioRepository.save(usuario);

        return "redirect:/"; // Redirigir al login
    }

    // Mostrar formulario de registro
    @GetMapping("/nuevo-admin")
    public String mostrarFormularioNuevoUsuario2(Model model) {
        // Obtener el usuario autenticado desde el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // El nombre de usuario es el email
        
        // Buscar el usuario en la base de datos
        Usuario usuarioAutenticado = usuarioRepository.findByDatosUsuarioEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));
        
        // Obtener el rol del usuario autenticado
        String rol = usuarioAutenticado.getRol() == 0 ? "USER" : "ADMIN"; // Asumiendo que 0 es 'USER' y 1 es 'ADMIN'
        
        // Agregar el rol al modelo
        model.addAttribute("rol", rol);
        
        // Agregar un nuevo objeto de usuario al modelo para el formulario
        model.addAttribute("usuario", new Usuario());
        
        return "registro"; // Nombre del template Thymeleaf
    }

    // Guardar nuevo usuario
    @PostMapping("/nuevo-admin")
    public String guardarUsuario2(@ModelAttribute("usuario") Usuario usuario, Model model) {
        // Verificar si el correo ya está registrado
        if (usuarioRepository.findByDatosUsuarioEmail(usuario.getDatosUsuario().getEmail()).isPresent()) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            // Obtener el usuario autenticado desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); // El nombre de usuario es el email
            
            // Buscar el usuario en la base de datos
            Usuario usuarioAutenticado = usuarioRepository.findByDatosUsuarioEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));
            
            // Obtener el rol del usuario autenticado
            String rol = usuarioAutenticado.getRol() == 0 ? "USER" : "ADMIN"; // Asumiendo que 0 es 'USER' y 1 es 'ADMIN'
            
            // Agregar el rol al modelo
            model.addAttribute("rol", rol);
            model.addAttribute("usuario", usuario); // Volver a pasar los datos al modelo
            return "registro"; // Vuelve al formulario de registro con el mensaje de error
        }

        // Asignar rol por defecto como USER si no es ADMIN
        if (usuario.getRol() == 0) {
            usuario.setRol(0); // 0 para USER si no se selecciona el rol
        }

        // Cifrar la contraseña antes de guardarla
        String encryptedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encryptedPassword);

        // Guardar el nuevo usuario en la base de datos
        usuarioRepository.save(usuario);

        return "redirect:/usuarios/lista-usuarios"; // Redirigir al listado de usuarios

    }


    // Método para eliminar usuarios
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, Model model) {
        // Buscar el usuario por su ID
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            model.addAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios/lista-usuarios";
        }

        // Eliminar el usuario si no es ADMIN
        usuarioRepository.deleteById(id);
        return "redirect:/usuarios/lista-usuarios";
    }

}
