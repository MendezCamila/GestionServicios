package com.gestionservicios.services;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.models.FacturacionMasiva;
import com.gestionservicios.repositories.ComprobanteRepository;
import com.gestionservicios.repositories.FacturacionMasivaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacturacionMasivaService {

    private final FacturacionMasivaRepository facturacionMasivaRepository;
    private final ComprobanteRepository comprobanteRepository;

    public FacturacionMasivaService(FacturacionMasivaRepository facturacionMasivaRepository,
                                    ComprobanteRepository comprobanteRepository) {
        this.facturacionMasivaRepository = facturacionMasivaRepository;
        this.comprobanteRepository = comprobanteRepository;
    }

    public FacturacionMasiva crearFacturacion(String usuario, String periodo) {
        FacturacionMasiva fm = new FacturacionMasiva();
        fm.setUsuario(usuario);
        fm.setPeriodo(periodo);
        fm.setFecha(LocalDateTime.now());
        fm.setCantidadFacturas(0);
        fm.setTotalFacturado(BigDecimal.ZERO);
        fm.setEstado("PENDIENTE");
        return facturacionMasivaRepository.save(fm);
    }

    @Transactional
    public void vincularComprobanteAFacturacion(Long comprobanteId, Long facturacionId) {
        Comprobante c = comprobanteRepository.findById(comprobanteId).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado: " + comprobanteId));
        FacturacionMasiva fm = facturacionMasivaRepository.findById(facturacionId).orElseThrow(() -> new IllegalArgumentException("FacturacionMasiva no encontrada: " + facturacionId));
        // asociar y actualizar totales
        c.setFacturacionMasiva(fm);
        comprobanteRepository.save(c);

        // recalculate summary
        List<Comprobante> comps = comprobanteRepository.findAllById(fm.getComprobantes().stream().map(Comprobante::getId).toList());
        // include newly linked comprobante as well
        if (comps.stream().noneMatch(x -> x.getId().equals(c.getId()))) comps.add(c);
        int count = comps.size();
        java.math.BigDecimal total = comps.stream().map(comp -> comp.getTotal() == null ? BigDecimal.ZERO : comp.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        fm.setCantidadFacturas(count);
        fm.setTotalFacturado(total);
        fm.setEstado("EN_PROCESO");
        facturacionMasivaRepository.save(fm);
    }

}
