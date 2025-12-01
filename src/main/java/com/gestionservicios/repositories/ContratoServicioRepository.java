package com.gestionservicios.repositories;

import com.gestionservicios.models.ContratoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoServicioRepository extends JpaRepository<ContratoServicio, Long> {
    List<ContratoServicio> findByClienteId(Long clienteId);
    List<ContratoServicio> findByServicioId(Long servicioId);
    boolean existsByClienteIdAndServicioIdAndEstado(Long clienteId, Long servicioId, String estado);
    List<ContratoServicio> findByEstado(String estado);
}
