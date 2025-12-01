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

    public Comprobante obtenerPorId(Long id) {
        return comprobanteRepository.findById(id).orElse(null);
    }

    public Comprobante guardar(Comprobante comprobante) {
        // Asegurar que saldoPendiente se inicialice al total cuando no esté seteado
        if (comprobante.getSaldoPendiente() == null && comprobante.getTotal() != null) {
            comprobante.setSaldoPendiente(comprobante.getTotal());
        }
        return comprobanteRepository.save(comprobante);
    }

}
