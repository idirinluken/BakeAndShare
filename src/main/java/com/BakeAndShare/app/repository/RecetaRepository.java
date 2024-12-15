package  com.BakeAndShare.app.repository;

import  com.BakeAndShare.app.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaRepository extends JpaRepository<Receta, String> {
    
    Receta findByNombre(String nombre);  // Método para buscar recetas por nombre
}