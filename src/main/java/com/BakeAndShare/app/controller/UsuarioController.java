package com.BakeAndShare.app.controller;

import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "usuarios/list"; // Thymeleaf template
    }

    // Mostrar formulario de registro
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro"; // Thymeleaf template
    }

    // Guardar nuevo usuario
    @PostMapping("/nuevo")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, Model model) {
        // Verificar si el correo ya está registrado
        if (usuarioRepository.findByDatosUsuarioEmail(usuario.getDatosUsuario().getEmail()).isPresent()) {
            // Si el correo ya está registrado, muestra un error
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            model.addAttribute("usuario", usuario); // Volver a pasar los datos al modelo
            return "registro"; // Vuelve al formulario de registro con el mensaje de error
        }

        // Asignar rol por defecto como USER
        usuario.setRol(0); // 0 para USER

        // Cifrar la contraseña antes de guardarla
        String encryptedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encryptedPassword);

        // Guardar el nuevo usuario en la base de datos
        usuarioRepository.save(usuario);

        return "redirect:/"; // Redirigir al login después de registrarse
    }

    // Ver detalles de un usuario
    @GetMapping("/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "usuarios/view"; // Thymeleaf template
    }

    // Editar un usuario
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "usuarios/form";
    }

    // Actualizar usuario
    @PostMapping("/{id}")
    public String actualizarUsuario(@PathVariable Long id, @ModelAttribute Usuario usuario) {
        usuario.setId(id);
        usuarioRepository.save(usuario);
        return "redirect:/usuarios";
    }

    // Eliminar usuario
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/usuarios";
    }
}
