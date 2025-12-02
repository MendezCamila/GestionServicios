package com.gestionservicios.repositories;

import com.gestionservicios.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
	java.util.List<com.gestionservicios.models.Servicio> findByNombreContainingIgnoreCase(String nombre);
}
