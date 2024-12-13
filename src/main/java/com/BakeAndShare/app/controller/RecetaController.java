package com.BakeAndShare.app.controller;

import com.BakeAndShare.app.model.Receta;
import com.BakeAndShare.app.repository.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/recetas")
public class RecetaController {

    @Autowired
    private RecetaRepository recetaRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    // Mostrar lista de recetas
    @GetMapping
    public String listarRecetas(Model model) {
        model.addAttribute("recetas", recetaRepository.findAll());
        return "recetas/lista-recetas"; // Thymeleaf template
    }

    // Mostrar formulario para agregar nueva receta
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevaReceta(Model model) {
        model.addAttribute("receta", new Receta());
        return "recetas/receta-form"; // Thymeleaf template
    }

    // Guardar nueva receta
    @PostMapping("/nuevo")
    public String guardarReceta(@ModelAttribute("receta") Receta receta, @RequestParam("imagenUrl") MultipartFile imagenFile, Model model) {
        if (!imagenFile.isEmpty()) {
            try {
                String imagenPath = guardarImagen(imagenFile);
                receta.setImagenUrl(imagenPath); // Guardar la ruta de la imagen en la base de datos
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Error al cargar la imagen.");
                return "recetas/receta-form"; // Vuelve al formulario con mensaje de error
            }
        }

        recetaRepository.save(receta);
        return "redirect:/recetas/lista-recetas"; // Redirigir a la lista de recetas
    }

    // Ver detalles de una receta
    @GetMapping("/{nombre}")
    public String verReceta(@PathVariable String nombre, Model model) {
        Receta receta = recetaRepository.findById(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
        model.addAttribute("receta", receta);
        return "recetas/receta"; // Thymeleaf template
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{nombre}")
    public String editarReceta(@PathVariable String nombre, Model model) {
        Receta receta = recetaRepository.findById(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
        model.addAttribute("receta", receta);
        return "recetas/receta-edit"; // Thymeleaf template
    }

    // Actualizar receta
    @PostMapping("/editar/{nombre}")
    public String actualizarReceta(@PathVariable String nombre, @ModelAttribute Receta receta, @RequestParam(value = "imagenUrl", required = false) MultipartFile imagenFile) {
        Receta recetaExistente = recetaRepository.findById(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));

        // Si se sube una nueva imagen, actualizar la imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String imagenPath = guardarImagen(imagenFile);
                receta.setImagenUrl(imagenPath); // Actualizar la ruta de la imagen
            } catch (IOException e) {
                e.printStackTrace();
                return "recetas/receta-edit"; // Manejo de error de carga de imagen
            }
        } else {
            receta.setImagenUrl(recetaExistente.getImagenUrl()); // Mantener la imagen anterior si no se sube una nueva
        }

        recetaRepository.save(receta);
        return "redirect:/recetas/lista-recetas"; // Redirigir a la lista de recetas
    }

    // Método para guardar la imagen y retornar la ruta
    private String guardarImagen(MultipartFile imagenFile) throws IOException {
        String imagenNombre = System.currentTimeMillis() + "-" + imagenFile.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + imagenNombre);
        Files.write(path, imagenFile.getBytes());

        return "/images/" + imagenNombre; // Retornar la URL de la imagen
    }

    // Eliminar receta
    @GetMapping("/eliminar/{nombre}")
    public String eliminarReceta(@PathVariable String nombre) {
        recetaRepository.deleteById(nombre);
        return "redirect:/recetas/lista-recetas"; // Redirigir a la lista de recetas después de eliminar
    }
}
