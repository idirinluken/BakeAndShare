package  com.BakeAndShare.app.repository;

import  com.BakeAndShare.app.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaRepository extends JpaRepository<Receta, String> {
    // Se pueden añadir métodos personalizados si es necesario
}