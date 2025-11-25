package com.gestionservicios.services;

import com.gestionservicios.models.ContratoServicio;
import com.gestionservicios.repositories.ContratoServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoServicioService {

    private final ContratoServicioRepository contratoServicioRepository;

    public ContratoServicioService(ContratoServicioRepository contratoServicioRepository) {
        this.contratoServicioRepository = contratoServicioRepository;
    }

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

    public List<ContratoServicio> listarPorCliente(Long clienteId) {
        return contratoServicioRepository.findByClienteId(clienteId);
    }

    public List<ContratoServicio> listarPorServicio(Long servicioId) {
        return contratoServicioRepository.findByServicioId(servicioId);
    }
}
