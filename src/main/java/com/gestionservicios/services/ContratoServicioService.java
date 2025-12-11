package com.gestionservicios.services;

import com.gestionservicios.models.ContratoServicio;
import com.gestionservicios.repositories.ContratoServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoServicioService {

    private final ContratoServicioRepository contratoServicioRepository;

    // Constructor Injection de lo que se necesita
    public ContratoServicioService(ContratoServicioRepository contratoServicioRepository) {
        this.contratoServicioRepository = contratoServicioRepository;
    }

    // Listar todos los contratos (cliente-servicio)
    public List<ContratoServicio> listarContratos() {
        return contratoServicioRepository.findAll();
    }

    public ContratoServicio obtenerPorId(long id) {
        return contratoServicioRepository.findById(id).orElse(null);
    }

    public ContratoServicio guardar(ContratoServicio contrato) {
        return contratoServicioRepository.save(contrato);
    }

    public void eliminar(long id) {
        contratoServicioRepository.deleteById(id);
    }

    public void desactivar(long id) {
        ContratoServicio contrato = obtenerPorId(id);
        if (contrato != null) {
            contrato.setEstado("Inactivo");
            contrato.setFechaFin(java.time.LocalDate.now());
            guardar(contrato);
        }
    }

    public List<ContratoServicio> listarPorCliente(Long clienteId) {
        return contratoServicioRepository.findByClienteId(clienteId);
    }

    public List<ContratoServicio> listarPorClienteYEstado(Long clienteId, String estado) {
        return contratoServicioRepository.findByClienteIdAndEstado(clienteId, estado);
    }

    public List<ContratoServicio> listarPorServicio(Long servicioId) {
        return contratoServicioRepository.findByServicioId(servicioId);
    }

    public boolean existeContratoActivo(Long clienteId, Long servicioId) {
        return contratoServicioRepository.existsByClienteIdAndServicioIdAndEstado(clienteId, servicioId, "Activo");
    }
}
