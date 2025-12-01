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

    /**
     * Calcula un estado resumido para un pago:
     * - "Pendiente": si montoIngresado == 0 o no hay detalles y montoIngresado == 0
     * - "Parcial": si al menos un PagoDetalle deja saldoPosterior > 0
     * - "Completado": si hay monto ingresado y todos los detalles tienen saldoPosterior == 0
     */
    public String estadoResumido(Pago pago) {
        if (pago == null) return "Pendiente";
        java.math.BigDecimal monto = pago.getMontoIngresado();
        if (monto == null || monto.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "Pendiente";
        }
        var detalles = pago.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            // Si no hay detalles pero hay monto ingresado, lo consideramos completado
            return "Completado";
        }
        boolean anyPendiente = false;
        for (com.gestionservicios.models.PagoDetalle d : detalles) {
            java.math.BigDecimal saldoPosterior = d.getSaldoPosterior();
            if (saldoPosterior != null && saldoPosterior.compareTo(java.math.BigDecimal.ZERO) > 0) {
                anyPendiente = true;
                break;
            }
        }
        return anyPendiente ? "Parcial" : "Completado";
    }

}
