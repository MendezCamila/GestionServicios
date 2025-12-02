package com.gestionservicios.services;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.repositories.ComprobanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;

    public ComprobanteService(ComprobanteRepository comprobanteRepository) {
        this.comprobanteRepository = comprobanteRepository;
    }

    public List<Comprobante> listarComprobantes() {
        return comprobanteRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
    

    public Comprobante obtenerPorId(Long id) {
        return comprobanteRepository.findById(id).orElse(null);
    }

    public Comprobante guardar(Comprobante comprobante) {
        // Asegurar que saldoPendiente se inicialice al total cuando no esté seteado
        if (comprobante.getSaldoPendiente() == null && comprobante.getTotal() != null) {
            comprobante.setSaldoPendiente(comprobante.getTotal());
        }
        // Validar que todos los detalles tengan contratoServicioId (regla del sistema)
        if (comprobante.getDetalles() != null) {
            for (var d : comprobante.getDetalles()) {
                if (d.getContratoServicioId() == null) {
                    throw new IllegalArgumentException("Cada ComprobanteDetalle debe tener contratoServicioId");
                }
            }
        }
        return comprobanteRepository.save(comprobante);
    }

}
