package com.gestionservicios.repositories;

import com.gestionservicios.models.FacturacionMasiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturacionMasivaRepository extends JpaRepository<FacturacionMasiva, Long> {

}
