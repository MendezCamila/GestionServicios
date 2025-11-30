package com.gestionservicios.repositories;

import com.gestionservicios.models.PagoDetalle;
import com.gestionservicios.models.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoDetalleRepository extends JpaRepository<PagoDetalle, Long> {
    List<PagoDetalle> findByComprobante(Comprobante comprobante);
}
