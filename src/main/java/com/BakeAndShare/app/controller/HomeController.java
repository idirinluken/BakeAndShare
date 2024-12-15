package com.BakeAndShare.app.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.BakeAndShare.app.model.Cocinero;
import com.BakeAndShare.app.model.Pastel;
import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.CocineroRepository;
import com.BakeAndShare.app.repository.PastelRepository;
import com.BakeAndShare.app.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PastelRepository pastelRepository;
    
    @Autowired
    private CocineroRepository cocineroRepository;
    
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

        // Pasar el nombre del usuario y los pasteles asociados al modelo
        model.addAttribute("nombreUsuario", usuario.getDatosUsuario().getNombre());
        model.addAttribute("pasteles", usuario.getPasteles());  // Mostrar los pasteles del usuario
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

    @GetMapping("/cocineros")
    public String verCocineros(Model model) {
        // Obtiene todos los cocineros con sus pasteles
        List<Cocinero> cocineros = cocineroRepository.findAll();
        List<Pastel> pasteles = pastelRepository.findAll();
        
        model.addAttribute("pasteles", pasteles);
        
        model.addAttribute("cocineros", cocineros);
        return "admin/cocineros";
    }

    @GetMapping("/cocineros/formulario")
    public String mostrarFormularioCocinero(@RequestParam(required = false) Long id, Model model) {
        Cocinero cocinero = (id != null) ? cocineroRepository.findById(id).orElse(new Cocinero()) : new Cocinero();
        model.addAttribute("cocinero", cocinero);
        return "admin/cocinero-formulario"; // Vista del formulario
    }

    @PostMapping("/cocineros/guardar")
    public String guardarCocinero(Cocinero cocinero) {
        cocineroRepository.save(cocinero); // Guarda o actualiza el cocinero
        return "redirect:/cocineros"; // Redirige a la lista de cocineros
    }

    @GetMapping("/cocineros/eliminar")
    public String eliminarCocinero(@RequestParam Long id) {
        cocineroRepository.deleteById(id); // Elimina el cocinero
        return "redirect:/cocineros"; // Redirige a la lista de cocineros
    }

    // Método para añadir un pastel a los pedidos del usuario
    @PostMapping("/pedir-pastel")
    public String pedirPastel(Authentication authentication, @RequestParam Long pastelId, Model model) {
        // Obtener el email del usuario autenticado
        String email = authentication.getName();

        // Buscar el usuario por su email
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Buscar el pastel por su ID
        Pastel pastel = pastelRepository.findById(pastelId)
                .orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));

        // Añadir el pastel a la lista de pasteles del usuario
        usuario.getPasteles().add(pastel);

        // Guardar los cambios en la base de datos
        usuarioRepository.save(usuario);

        // Redirigir a la página de pedidos
        model.addAttribute("pasteles", usuario.getPasteles()); // Mostrar los pasteles del usuario
        return "user/lista-pedidos"; // Página de pedidos del usuario
    }

    @GetMapping("/pedidos-admin")
    public String verPedidos(Model model) {
        // Obtiene todos los usuarios que han pedido pasteles
        List<Usuario> usuariosConPedidos = usuarioRepository.findByPastelesIsNotNull();
        
        model.addAttribute("usuarios", usuariosConPedidos);
        return "admin/pedidos";
    }
    
    // Mostrar lista de pasteles (pedidos) de un usuario
    @GetMapping("/pedidos")
    public String verMisPedidos(Authentication authentication, Model model) {
        // Obtener el email del usuario autenticado
        String email = authentication.getName();

        // Buscar el usuario por email
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Mostrar los pasteles (pedidos) asociados a ese usuario
        model.addAttribute("pasteles", usuario.getPasteles()); // Agregamos los pasteles al modelo
        return "user/lista-pedidos"; // Vista que muestra los pasteles del usuario
    }

    //Cancelar pedido
    @GetMapping("/cancelarPedido")
    public String cancelarPedido(@RequestParam("id") Long id, Authentication authentication, Model model) {
        // Obtener el email del usuario autenticado
        String email = authentication.getName();  // Obtiene el nombre de usuario del contexto de seguridad
        
        // Buscar el usuario por su email
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar el pastel por su ID
        Pastel pastel = pastelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pastel no encontrado"));

        // Eliminar el pastel de los pedidos del usuario
        usuario.getPasteles().remove(pastel); // Remover el pastel de la lista de pasteles del usuario

        // Guardar los cambios en la base de datos
        usuarioRepository.save(usuario);
        
        // Redirigir a la página de pedidos del usuario
        model.addAttribute("pasteles", usuario.getPasteles()); // Actualizamos el modelo con los nuevos pedidos
        return "user/lista-pedidos"; // Página de pedidos del usuario actualizada
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

    @PostMapping("/usuarios/editar")
    public String guardarCambiosUsuario(
            Authentication authentication,
            Model model,
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String telefono) {

        // Buscar al usuario en la base de datos
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Comprobar si el correo electrónico ya está registrado por otro usuario
        boolean emailYaRegistrado = usuarioRepository.findByDatosUsuarioEmail(email)
                .filter(u -> !u.getId().equals(id)) // Excluir el usuario actual
                .isPresent();

        if (emailYaRegistrado) {
            // Si el correo ya está registrado, mostrar un error
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            model.addAttribute("usuario", usuario); // Pasar el usuario al modelo
            return "user/edit-user";
        }

        // Actualizar los datos del usuario
        usuario.getDatosUsuario().setNombre(nombre);
        usuario.getDatosUsuario().setApellido(apellido);
        usuario.getDatosUsuario().setEmail(email);
        usuario.getDatosUsuario().setTelefono(telefono);

        // Guardar los cambios en la base de datos
        usuarioRepository.save(usuario);

        // Actualizar el contexto de autenticación
        actualizarContextoAutenticacion(authentication, email);

        // Redirigir al usuario a la página de inicio
        return "redirect:/home";
    }

    private void actualizarContextoAutenticacion(Authentication authentication, String nuevoEmail) {
        // Crear un nuevo UserDetails con el nuevo email
        User principal = new User(
                nuevoEmail,
                "", // Dejar las credenciales vacías si no están disponibles
                authentication.getAuthorities()
        );

        // Crear un nuevo token de autenticación
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                principal,
                null, // Establecer las credenciales como null
                authentication.getAuthorities()
        );

        // Actualizar el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
