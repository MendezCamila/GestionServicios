package com.gestionservicios.repositories;

import com.gestionservicios.models.ComprobanteDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprobanteDetalleRepository extends JpaRepository<ComprobanteDetalle, Long> {
}
