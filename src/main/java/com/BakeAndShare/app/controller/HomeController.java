package com.BakeAndShare.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
public class HomeController {
	
	// Inyectar el repositorio
    @Autowired
    private UsuarioRepository usuarioRepository;

    
    @GetMapping("/")
    public String mostrarLogin(Model model) {
        return "login"; // Thymeleaf template
    }
    
    
    @GetMapping("/home")
    public String mostrarHome(Authentication authentication, Model model) {
        // Obtener el email del usuario autenticado
        String email = authentication.getName();

        // Buscar el usuario por email
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar el rol
        if (usuario.getRol() == 1) { // ADMIN
            return "admin/home"; // Página de inicio del admin
        }

        // USER: Pasar el nombre del usuario al modelo
        model.addAttribute("nombreUsuario", usuario.getDatosUsuario().getNombre());
        return "user/home"; // Página de inicio del usuario
    }
    
    
    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth); // Logout
        }
        return "redirect:/"; // Redirigir al login después de logout
    }
    
    
    @GetMapping("/usuarios/editar")
    public String editarDatosUsuario(Authentication authentication, Model model) {
        // Obtener el email del usuario autenticado
        String email = authentication.getName();

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Pasar el objeto usuario al modelo para editar
        model.addAttribute("usuario", usuario);
        return "user/edit-user";  // Vista para editar los datos
    }

    
    // Guardar los cambios de datos
    @PostMapping("/usuarios/editar")
    public String guardarCambiosUsuario(@RequestParam Long id,
                                        @RequestParam String nombre,
                                        @RequestParam String apellido,
                                        @RequestParam String email,
                                        @RequestParam String telefono) {
        // Buscar al usuario en la base de datos
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar los datos del usuario
        usuario.getDatosUsuario().setNombre(nombre);
        usuario.getDatosUsuario().setApellido(apellido);
        usuario.getDatosUsuario().setEmail(email);
        usuario.getDatosUsuario().setTelefono(telefono);

        // Guardar los cambios en la base de datos
        usuarioRepository.save(usuario);

        // Redirigir al usuario a la página de inicio
        return "redirect:/home";
    }
}
