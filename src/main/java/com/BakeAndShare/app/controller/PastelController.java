package com.BakeAndShare.app.controller;

import com.BakeAndShare.app.model.Cocinero;
import com.BakeAndShare.app.model.Pastel;
import com.BakeAndShare.app.model.Receta;
import com.BakeAndShare.app.repository.CocineroRepository;
import com.BakeAndShare.app.repository.PastelRepository;
import com.BakeAndShare.app.repository.RecetaRepository;
import com.BakeAndShare.app.model.Usuario;
import com.BakeAndShare.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/pasteles")
public class PastelController {

    @Autowired
    private PastelRepository pastelRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private CocineroRepository cocineroRepository;

    // Mostrar la lista de pasteles
    @GetMapping
    public String listarPasteles(Model model) {
        List<Pastel> pasteles = pastelRepository.findAll();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String rol = authentication.getAuthorities().toString(); // "ROLE_USER" o "ROLE_ADMIN"

        model.addAttribute("pasteles", pasteles);
        model.addAttribute("rol", rol);

        // La vista se adapta dependiendo del rol del usuario
        if (rol.contains("ROLE_ADMIN")) {
            return "admin/pasteles/list-pasteles"; // Vista de administrador
        } else {
            return "user/pasteles/list-pasteles"; // Vista de usuario
        }
    }

    @GetMapping("/eliminar")
    public String eliminarPastel(@RequestParam Long id, Model model) {
        pastelRepository.deleteById(id);
        return "redirect:/pasteles"; // Redirigir a la lista de pasteles después de eliminar el pastel
    }

    // Ver los detalles de un pastel específico
    @GetMapping("/{id}")
    public String verPastel(@PathVariable Long id, @RequestParam(value = "origen", required = false, defaultValue = "pasteles") String origen, Model model) {
        Pastel pastel = pastelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));
    
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String rol = authentication.getAuthorities().toString(); // "ROLE_USER" o "ROLE_ADMIN"
    
        model.addAttribute("pastel", pastel);
        model.addAttribute("rol", rol);
        model.addAttribute("origen", origen); // Añadir el origen al modelo

        // La vista se adapta dependiendo del rol del usuario
        if (rol.contains("ROLE_ADMIN")) {
            return "admin/pasteles/view"; // Vista de administrador
        } else {
            return "user/pasteles/view"; // Vista de usuario
        }
    }

    // Agregar un pastel a los pedidos del usuario
    @PostMapping("/pedido/{id}")
    public String hacerPedido(@PathVariable Long id, Authentication authentication, Model model) {
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByDatosUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Pastel pastel = pastelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));

        // Agregar el pastel a la lista de pedidos del usuario
        usuario.getPasteles().add(pastel);
        usuarioRepository.save(usuario);

        return "redirect:/pedidos"; // Redirigir a la vista de pedidos
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("pastel", new Pastel());
        model.addAttribute("cocineros", cocineroRepository.findAll()); // Obtener lista de cocineros
        return "admin/pasteles/formulario-pastel";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Pastel pastel = pastelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));
        model.addAttribute("pastel", pastel);
        model.addAttribute("cocineros", cocineroRepository.findAll());
        return "admin/pasteles/formulario-pastel";
    }

    @PostMapping("/crear")
    public String crearPastel(@ModelAttribute Pastel pastel, 
                              @RequestParam("ingredientes") String ingredientes,
                              @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                              @RequestParam("nombreReceta") String nombreReceta,
                              @RequestParam("tiempoCoccion") Integer tiempoCoccion,
                              @RequestParam("temperatura") Integer temperatura,
                              @RequestParam(value = "cocineroId", required = false) Long cocineroId, // Recibimos el ID del cocinero
                              Model model) {
    
        // Validar que el nombre de la receta no esté repetido en la base de datos
        if (nombreReceta != null && !nombreReceta.trim().isEmpty()) {
            Receta recetaExistente = recetaRepository.findByNombre(nombreReceta);
            if (recetaExistente != null) {
                model.addAttribute("error", "El nombre de la receta ya existe.");
                return "admin/pasteles/formulario-pastel";  // Regresar al formulario con un mensaje de error
            }
        }
    
        // Crear la nueva receta
        Receta nuevaReceta = new Receta();
        nuevaReceta.setNombre(nombreReceta);
        nuevaReceta.setTiempoCoccion(tiempoCoccion);
        nuevaReceta.setTemperatura(temperatura);
    
        // Procesar los ingredientes
        if (ingredientes != null && !ingredientes.trim().isEmpty()) {
            nuevaReceta.setIngredientes(Arrays.asList(ingredientes.split(",")));
        }
    
        // Si se sube una imagen, guardarla y actualizar la URL
        if (imagen != null && !imagen.isEmpty()) {
            String imagenUrl = guardarImagen(imagen);
            nuevaReceta.setImagenUrl(imagenUrl);
        }
    
        // Guardar la receta en la base de datos
        recetaRepository.save(nuevaReceta);
    
        // Asignar la receta al pastel
        pastel.setReceta(nuevaReceta);
    
        // Asignar el cocinero, si se recibe un cocineroId
        if (cocineroId != null) {
            Cocinero cocinero = cocineroRepository.findById(cocineroId)
                    .orElseThrow(() -> new IllegalArgumentException("Cocinero no encontrado"));
            pastel.setCocinero(cocinero);
        }
    
        // Guardar el pastel en la base de datos
        pastelRepository.save(pastel);
    
        // Redirigir a la lista de pasteles
        return "redirect:/pasteles";
    }
    

    @PostMapping("/editar/{id}")
    public String editarPastel(@PathVariable Long id, 
                                @ModelAttribute Pastel pastel, 
                                @RequestParam("ingredientes") String ingredientes,
                                @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                @RequestParam(value = "nombreReceta", required = false) String nombreReceta,
                                @RequestParam(value = "tiempoCoccion", required = false) Integer tiempoCoccion,
                                @RequestParam(value = "temperatura", required = false) Integer temperatura,
                                @RequestParam(value = "cocineroId", required = false) Long cocineroId, // Recibimos el ID del cocinero
                                Model model) {
        // Buscar el pastel existente en la base de datos
        Pastel pastelExistente = pastelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pastel no encontrado"));
        
        // Obtener la receta del pastel
        Receta recetaExistente = pastelExistente.getReceta();
        
        // Validar que el nombre de la receta no esté repetido en la base de datos
        if (nombreReceta != null && !nombreReceta.trim().isEmpty()) {
            Receta recetaConMismoNombre = recetaRepository.findByNombre(nombreReceta);
            if (recetaConMismoNombre != null && !recetaConMismoNombre.getNombre().equals(recetaExistente.getNombre())) {
                model.addAttribute("error", "El nombre de la receta ya existe.");
                return "admin/pasteles/formulario-pastel";  // Regresar al formulario con un mensaje de error
            }
            recetaExistente.setNombre(nombreReceta);
        }

        // Actualizar otros campos de la receta
        if (tiempoCoccion != null) {
            recetaExistente.setTiempoCoccion(tiempoCoccion);
        }
        if (temperatura != null) {
            recetaExistente.setTemperatura(temperatura);
        }
        
        // Si se reciben nuevos ingredientes, procesarlos
        if (ingredientes != null && !ingredientes.trim().isEmpty()) {
            recetaExistente.setIngredientes(Arrays.asList(ingredientes.split(",")));
        }
        
        // Si se sube una nueva imagen, guardarla y actualizar la URL
        if (imagen != null && !imagen.isEmpty()) {
            String imagenUrl = guardarImagen(imagen);
            recetaExistente.setImagenUrl(imagenUrl);
        }

        // Guardar los cambios en la receta
        recetaRepository.save(recetaExistente);
        
        // Actualizar el cocinero, si se recibe un cocineroId
        if (cocineroId != null) {
            Cocinero cocinero = cocineroRepository.findById(cocineroId)
                    .orElseThrow(() -> new IllegalArgumentException("Cocinero no encontrado"));
            pastelExistente.setCocinero(cocinero);
        }

        // Guardar los cambios en el pastel
        pastelExistente.setReceta(recetaExistente);
        pastelRepository.save(pastelExistente);
        
        // Redirigir a la lista de pasteles
        return "redirect:/pasteles";
    }


    private String guardarImagen(MultipartFile imagen) {
        try {
            String directorioImagenes = "/images";
            Path ruta = Paths.get(directorioImagenes + "/" + imagen.getOriginalFilename());
            Files.copy(imagen.getInputStream(), ruta);
            return "/images/" + imagen.getOriginalFilename(); // Retorna la URL de la imagen
        } catch (IOException e) {
            e.printStackTrace();
            // Manejo de errores si falla la carga
            return null;
        }
    }
    

}
