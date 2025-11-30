package com.gestionservicios.repositories;

import com.gestionservicios.models.Pago;
import com.gestionservicios.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCliente(Cliente cliente);
}
