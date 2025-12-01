package com.gestionservicios.services;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.models.FacturacionMasiva;
import com.gestionservicios.models.FacturacionPreviewDTO;
import com.gestionservicios.models.ContratoServicio;
import com.gestionservicios.repositories.ContratoServicioRepository;
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
    private final ContratoServicioRepository contratoServicioRepository;

    // New constructor for injection including contrato repo
    public FacturacionMasivaService(FacturacionMasivaRepository facturacionMasivaRepository,
                                    ComprobanteRepository comprobanteRepository,
                                    ContratoServicioRepository contratoServicioRepository) {
        this.facturacionMasivaRepository = facturacionMasivaRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.contratoServicioRepository = contratoServicioRepository;
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

        /**
         * Simula la facturación masiva para el periodo dado.
         * Retorna conteos: clientes activos, clientes con factura en el periodo, y cantidad a generar.
         */
        public FacturacionPreviewDTO simularFacturacionMasiva(String periodo) {
        // obtener contratos activos
        List<ContratoServicio> contratos = contratoServicioRepository.findByEstado("ACTIVO");

        // clientes activos = cantidad de clientes distintos en contratos
        long clientesActivos = contratos.stream().map(c -> c.getCliente().getId()).distinct().count();

        // contratos que ya tienen comprobante en este periodo
        List<ContratoServicio> contratosConFactura = contratos.stream()
            .filter(c -> comprobanteRepository.existsByContratoIdAndPeriodo(c.getId(), periodo))
            .toList();

        // contratos a facturar
        List<ContratoServicio> contratosAFacturar = contratos.stream()
            .filter(c -> !comprobanteRepository.existsByContratoIdAndPeriodo(c.getId(), periodo))
            .toList();

        // clientes con factura del periodo = clientes distintos entre contratosConFactura
        long clientesConFactura = contratosConFactura.stream().map(c -> c.getCliente().getId()).distinct().count();

        return new FacturacionPreviewDTO((int) clientesActivos, (int) clientesConFactura, contratosAFacturar.size());
        }

}
