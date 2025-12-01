package com.gestionservicios.repositories;

import com.gestionservicios.models.PagoMetodo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoMetodoRepository extends JpaRepository<PagoMetodo, Long> {

	// Recupera en lote los métodos asociados a una lista de pagos (evita N+1)
	List<PagoMetodo> findByPagoIdIn(List<Long> pagoIds);

}
