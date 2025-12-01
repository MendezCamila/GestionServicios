package com.gestionservicios.repositories;

import com.gestionservicios.models.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    List<Comprobante> findByClienteId(Long clienteId);
    boolean existsByContratoIdAndPeriodo(Long contratoId, String periodo);
}
