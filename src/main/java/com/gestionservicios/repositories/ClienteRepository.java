package com.gestionservicios.repositories;

import com.gestionservicios.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCuit(String cuit);
    List<Cliente> findByRazonSocialContainingIgnoreCase(String razonSocial);
}

