package com.gestionservicios.services;

import com.gestionservicios.models.Pago;
import com.gestionservicios.repositories.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final com.gestionservicios.repositories.ComprobanteRepository comprobanteRepository;
    private final com.gestionservicios.repositories.PagoDetalleRepository pagoDetalleRepository;
    private final com.gestionservicios.repositories.PagoMetodoRepository pagoMetodoRepository;

    public PagoService(PagoRepository pagoRepository,
                       com.gestionservicios.repositories.ComprobanteRepository comprobanteRepository,
                       com.gestionservicios.repositories.PagoDetalleRepository pagoDetalleRepository,
                       com.gestionservicios.repositories.PagoMetodoRepository pagoMetodoRepository) {
        this.pagoRepository = pagoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.pagoDetalleRepository = pagoDetalleRepository;
        this.pagoMetodoRepository = pagoMetodoRepository;
    }

    public List<Pago> listarPagos() {
        return pagoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Pago> listarPagos(Pageable pageable) {
        return pagoRepository.findAll(pageable);
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
    //Convierte cada factura en una version resumida DTO para enviar al navegador
    public java.util.List<com.gestionservicios.dto.ComprobantePendienteDTO> listarFacturasPendientesDTO(Long clienteId) {
        var list = listarFacturasPendientesPorCliente(clienteId);
        return list.stream().map(c -> new com.gestionservicios.dto.ComprobantePendienteDTO(
                c.getId(), c.getFechaEmision(), c.getTotal(), c.getSaldoPendiente(), c.getEstado()
        )).toList();
    }

    @org.springframework.transaction.annotation.Transactional
    public Pago crearPagoConDetalles(Pago pago, List<com.gestionservicios.models.PagoDetalle> detalles) {
        // Delegate to the new implementation that also accepts métodos de pago (lista vacía aquí)
        return crearPagoConDetalles(pago, detalles, java.util.Collections.emptyList());
    }

    @org.springframework.transaction.annotation.Transactional
    public Pago crearPagoConDetalles(Pago pago, List<com.gestionservicios.models.PagoDetalle> detalles, List<com.gestionservicios.models.PagoMetodo> metodos) {
        // guardar pago primero para obtener id
        pago = pagoRepository.save(pago);

        //iteracion sobre cada detalle
        for (com.gestionservicios.models.PagoDetalle det : detalles) {
            var compOpt = comprobanteRepository.findById(det.getComprobante().getId());
            if (compOpt.isEmpty()) throw new IllegalArgumentException("Comprobante no encontrado: " + det.getComprobante().getId());
            var comp = compOpt.get();
            //lectura y normalizacion del monto aplicado
            java.math.BigDecimal montoAplicado = det.getMontoAplicado();
            if (montoAplicado == null) montoAplicado = java.math.BigDecimal.ZERO;
            if (montoAplicado.compareTo(java.math.BigDecimal.ZERO) <= 0) continue; // ignorar montos 0
            java.math.BigDecimal saldoAnterior = comp.getSaldoPendiente() == null ? comp.getTotal() : comp.getSaldoPendiente();
            // validar que el monto aplicado no exceda el saldo pendiente
            if (montoAplicado.compareTo(saldoAnterior) > 0) {
                throw new IllegalArgumentException("Monto aplicado mayor al saldo pendiente para comprobante " + comp.getId());
            }
            // calcular saldo posterior
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

        // guardar métodos de pago asociados al pago
        if (metodos != null && !metodos.isEmpty()) {
            for (com.gestionservicios.models.PagoMetodo pm : metodos) {
                pm.setPago(pago);
                if (pm.getMonto() == null) pm.setMonto(java.math.BigDecimal.ZERO);
                pagoMetodoRepository.save(pm);
            }
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

    /**
     * Agrupa los métodos de pago (de la tabla `pago_metodos`) por pago y formatea cada entrada
     * como "EFECTIVO (1.000,00), TARJETA (2.500,00)". Devuelve un mapa idPago -> cadena formateada.
     */
    public java.util.Map<Long, String> agruparMetodosConMontos(java.util.List<Pago> pagos) {
        java.util.Map<Long, String> resultado = new java.util.HashMap<>();
        if (pagos == null || pagos.isEmpty()) return resultado;

        //obtener los IDs de los pagos
        java.util.List<Long> ids = pagos.stream()
                .map(Pago::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) return resultado;

        //traer todos los metodos de pago asociados
        java.util.List<com.gestionservicios.models.PagoMetodo> metodos = pagoMetodoRepository.findByPagoIdIn(ids);
        if (metodos == null || metodos.isEmpty()) return resultado;

        // formatear los montos argentino
        java.util.Locale locale = new java.util.Locale("es", "AR");
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);

        //agrupar metodos por pago
        java.util.Map<Long, java.util.List<com.gestionservicios.models.PagoMetodo>> grouped = metodos.stream()
                .collect(java.util.stream.Collectors.groupingBy(pm -> pm.getPago().getId()));

        // construir el resultado formateado
        for (var entry : grouped.entrySet()) {
            Long pagoId = entry.getKey();
            java.util.List<com.gestionservicios.models.PagoMetodo> lista = entry.getValue();
            //por cada metodo armar "metodo(monto)"
            String joined = lista.stream().map(pm -> {
                String metodo = pm.getMetodo() == null ? "" : pm.getMetodo().name();
                java.math.BigDecimal monto = pm.getMonto() == null ? java.math.BigDecimal.ZERO : pm.getMonto();
                String montoFmt = nf.format(monto);
                return metodo + " (" + montoFmt + ")";
            }).collect(java.util.stream.Collectors.joining(", "));
            resultado.put(pagoId, joined);
        }

        return resultado;
    }

    /**
     * Variante que devuelve la lista estructurada por pago: idPago -> List<"METODO (monto)">
     */
    public java.util.Map<Long, java.util.List<String>> agruparMetodosConMontosLista(java.util.List<Pago> pagos) {
        java.util.Map<Long, java.util.List<String>> resultado = new java.util.HashMap<>();
        if (pagos == null || pagos.isEmpty()) return resultado;

        // obtener los IDs de los pagos
        java.util.List<Long> ids = pagos.stream()
                .map(Pago::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) return resultado;

        // traer todos los metodos de pago asociados
        java.util.List<com.gestionservicios.models.PagoMetodo> metodos = pagoMetodoRepository.findByPagoIdIn(ids);
        if (metodos == null || metodos.isEmpty()) return resultado;

        // formatear los montos argentino
        java.util.Locale locale = new java.util.Locale("es", "AR");
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);

        //agrupar los metodos por ID de pago
        java.util.Map<Long, java.util.List<com.gestionservicios.models.PagoMetodo>> grouped = metodos.stream()
                .collect(java.util.stream.Collectors.groupingBy(pm -> pm.getPago().getId()));

        //recorrer cada grupo y armar la lista de strings
        for (var entry : grouped.entrySet()) {
            Long pagoId = entry.getKey();
            java.util.List<com.gestionservicios.models.PagoMetodo> lista = entry.getValue();
            //convertir cada metodo en "metodo(monto)"
            java.util.List<String> items = lista.stream().map(pm -> {
                String metodo = pm.getMetodo() == null ? "" : pm.getMetodo().name();
                java.math.BigDecimal monto = pm.getMonto() == null ? java.math.BigDecimal.ZERO : pm.getMonto();
                String montoFmt = nf.format(monto);
                return metodo + " (" + montoFmt + ")";
            }).collect(java.util.stream.Collectors.toList());
            resultado.put(pagoId, items);
        }

        return resultado;
    }

    /**
     * Devuelve los métodos de pago asociados a un pago (lista de PagoMetodo).
     */
    public java.util.List<com.gestionservicios.models.PagoMetodo> listarMetodosPorPago(Long pagoId) {
        if (pagoId == null) return java.util.Collections.emptyList();
        java.util.List<com.gestionservicios.models.PagoMetodo> metodos = pagoMetodoRepository.findByPagoIdIn(java.util.List.of(pagoId));
        return metodos == null ? java.util.Collections.emptyList() : metodos;
    }

}
