package  com.BakeAndShare.app.repository;

import  com.BakeAndShare.app.model.Cocinero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CocineroRepository extends JpaRepository<Cocinero, Long> {
    List<Cocinero> findByNombreContaining(String nombre); // Buscar cocineros cuyo nombre contenga un texto
}