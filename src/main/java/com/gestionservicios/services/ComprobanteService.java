package com.gestionservicios.services;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.repositories.ComprobanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;

    public ComprobanteService(ComprobanteRepository comprobanteRepository) {
        this.comprobanteRepository = comprobanteRepository;
    }

    public List<Comprobante> listarComprobantes() {
        return comprobanteRepository.findAll();
    }

    public Comprobante guardar(Comprobante comprobante) {
        return comprobanteRepository.save(comprobante);
    }
}
