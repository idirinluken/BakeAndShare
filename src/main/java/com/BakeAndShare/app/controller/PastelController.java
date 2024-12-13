package com.BakeAndShare.app.controller;

import com.BakeAndShare.app.model.Pastel;
import com.BakeAndShare.app.repository.PastelRepository;
import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequestMapping("/pasteles")
public class PastelController {

    @Autowired
    private PastelRepository pastelRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mostrar lista de pasteles disponibles
    @GetMapping("/pasteles")
    public String listarPasteles(Model model) {
        List<Pastel> pasteles = pastelRepository.findAll();
        model.addAttribute("pasteles", pasteles);
        return "pasteles/lista-pasteles"; // Vista que muestra todos los pasteles disponibles
    }

    // Ver detalles de un pastel
    @GetMapping("/{id}")
    public String verPastel(@PathVariable Long id, Model model) {
        Pastel pastel = pastelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));
        model.addAttribute("pastel", pastel);
        return "pasteles/view"; // Vista con los detalles del pastel
    }

    // Agregar un pastel a los pedidos del usuario
    @PostMapping("/pedido/{id}")
    public String hacerPedido(@PathVariable Long id, Authentication authentication, Model model) {
        // Obtener el email del usuario autenticado
        String email = authentication.getName();

        // Buscar el usuario por email
        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Buscar el pastel por id
        Pastel pastel = pastelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));

        // Añadir el pastel a la lista de pedidos del usuario
        usuario.getPasteles().add(pastel);
        usuarioRepository.save(usuario);

        // Redirigir al usuario a la lista de pasteles
        return "redirect:/lista-pedidos"; // Redirigir a la vista de pedidos del usuario
    }
}
