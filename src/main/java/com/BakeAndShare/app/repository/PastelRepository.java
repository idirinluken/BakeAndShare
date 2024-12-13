package  com.BakeAndShare.app.repository;

import  com.BakeAndShare.app.model.Pastel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PastelRepository extends JpaRepository<Pastel, Long> {
   
}