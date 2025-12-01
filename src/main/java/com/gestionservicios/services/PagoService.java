package com.gestionservicios.services;

import com.gestionservicios.models.Pago;
import com.gestionservicios.repositories.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final com.gestionservicios.repositories.ComprobanteRepository comprobanteRepository;
    private final com.gestionservicios.repositories.PagoDetalleRepository pagoDetalleRepository;

    public PagoService(PagoRepository pagoRepository,
                       com.gestionservicios.repositories.ComprobanteRepository comprobanteRepository,
                       com.gestionservicios.repositories.PagoDetalleRepository pagoDetalleRepository) {
        this.pagoRepository = pagoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.pagoDetalleRepository = pagoDetalleRepository;
    }

    public List<Pago> listarPagos() {
        return pagoRepository.findAll();
    }

    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id).orElse(null);
    }

    public List<com.gestionservicios.models.Comprobante> listarFacturasPendientesPorCliente(Long clienteId) {
        var all = comprobanteRepository.findByClienteId(clienteId);
            return all.stream().filter(c -> {
                // Facturas pendientes: estado "Emitida" o saldoPendiente > 0
                boolean estadoEmitida = c.getEstado() != null && c.getEstado().trim().equalsIgnoreCase("Emitida");
                boolean saldoPositivo = c.getSaldoPendiente() != null && c.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0;
                return estadoEmitida || saldoPositivo;
            }).toList();
    }
    public java.util.List<com.gestionservicios.dto.ComprobantePendienteDTO> listarFacturasPendientesDTO(Long clienteId) {
        var list = listarFacturasPendientesPorCliente(clienteId);
        return list.stream().map(c -> new com.gestionservicios.dto.ComprobantePendienteDTO(
                c.getId(), c.getFechaEmision(), c.getTotal(), c.getSaldoPendiente(), c.getEstado()
        )).toList();
    }

    @org.springframework.transaction.annotation.Transactional
    public Pago crearPagoConDetalles(Pago pago, List<com.gestionservicios.models.PagoDetalle> detalles) {
        // guardar pago primero para obtener id
        pago = pagoRepository.save(pago);

        for (com.gestionservicios.models.PagoDetalle det : detalles) {
            var compOpt = comprobanteRepository.findById(det.getComprobante().getId());
            if (compOpt.isEmpty()) throw new IllegalArgumentException("Comprobante no encontrado: " + det.getComprobante().getId());
            var comp = compOpt.get();
            java.math.BigDecimal montoAplicado = det.getMontoAplicado();
            if (montoAplicado == null) montoAplicado = java.math.BigDecimal.ZERO;
            if (montoAplicado.compareTo(java.math.BigDecimal.ZERO) <= 0) continue; // ignorar montos 0
            java.math.BigDecimal saldoAnterior = comp.getSaldoPendiente() == null ? comp.getTotal() : comp.getSaldoPendiente();
            if (montoAplicado.compareTo(saldoAnterior) > 0) {
                throw new IllegalArgumentException("Monto aplicado mayor al saldo pendiente para comprobante " + comp.getId());
            }
            java.math.BigDecimal saldoPosterior = saldoAnterior.subtract(montoAplicado);

            // actualizar comprobante
            comp.setSaldoPendiente(saldoPosterior);
            if (saldoPosterior.compareTo(java.math.BigDecimal.ZERO) == 0) {
                comp.setEstado("Pagado");
            } else {
                comp.setEstado("Parcial");
            }
            comprobanteRepository.save(comp);

            det.setPago(pago);
            det.setComprobante(comp);
            det.setSaldoAnterior(saldoAnterior);
            det.setSaldoPosterior(saldoPosterior);
            pagoDetalleRepository.save(det);
            pago.getDetalles().add(det);
        }

        return pago;
    }

}
