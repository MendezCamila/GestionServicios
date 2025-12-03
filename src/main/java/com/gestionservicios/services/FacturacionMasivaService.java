package com.gestionservicios.services;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.models.FacturacionMasiva;
import com.gestionservicios.models.FacturacionPreviewDTO;
import com.gestionservicios.models.ContratoServicio;
import com.gestionservicios.models.ClientePreviewItem;
import com.gestionservicios.repositories.ComprobanteDetalleRepository;
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
    private final ComprobanteDetalleRepository comprobanteDetalleRepository;
    private final ComprobanteService comprobanteService;
    private final com.gestionservicios.services.ConfiguracionService configuracionService;

    // New constructor including comprobanteDetalleRepository
    public FacturacionMasivaService(FacturacionMasivaRepository facturacionMasivaRepository,
                                    ComprobanteRepository comprobanteRepository,
                                    ContratoServicioRepository contratoServicioRepository,
                                    ComprobanteDetalleRepository comprobanteDetalleRepository,
                                    ComprobanteService comprobanteService,
                                    com.gestionservicios.services.ConfiguracionService configuracionService) {
        this.facturacionMasivaRepository = facturacionMasivaRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.contratoServicioRepository = contratoServicioRepository;
        this.comprobanteDetalleRepository = comprobanteDetalleRepository;
        this.comprobanteService = comprobanteService;
        this.configuracionService = configuracionService;
    }

    @org.springframework.transaction.annotation.Transactional
    public java.util.Map<String, Object> ejecutarFacturacionMasiva(String usuario, String periodo) {
        // crear registro de facturación masiva
        FacturacionMasiva fm = crearFacturacion(usuario, periodo);

        // obtener contratos activos
        List<ContratoServicio> contratos = contratoServicioRepository.findByEstado("ACTIVO");
        java.util.Map<Long, java.util.List<ContratoServicio>> contratosPorCliente = contratos.stream()
            .collect(java.util.stream.Collectors.groupingBy(c -> c.getCliente().getId()));

        // clientes a facturar = clientes que tengan al menos un contrato sin comprobante en el periodo
        java.util.List<Long> clientesAFacturarIds = contratosPorCliente.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(c -> !comprobanteDetalleRepository.existsByContratoServicioIdAndComprobantePeriodo(c.getId(), periodo)))
            .map(java.util.Map.Entry::getKey)
            .toList();

        int contador = 0;
        java.math.BigDecimal totalFacturado = java.math.BigDecimal.ZERO;

        for (Long clienteId : clientesAFacturarIds) {
            // generar comprobante por cliente
            java.util.List<ContratoServicio> pendientes = contratosPorCliente.get(clienteId).stream()
                .filter(c -> !comprobanteDetalleRepository.existsByContratoServicioIdAndComprobantePeriodo(c.getId(), periodo))
                .toList();
            if (pendientes.isEmpty()) continue;

            com.gestionservicios.models.Comprobante comprobante = new com.gestionservicios.models.Comprobante();
            com.gestionservicios.models.Cliente cliente = pendientes.get(0).getCliente();
            comprobante.setCliente(cliente);
            comprobante.setTipoComprobante(com.gestionservicios.util.TaxRules.tipoComprobantePorCondicion(cliente != null ? cliente.getCondicionFiscal() : null));
            comprobante.setFechaEmision(java.time.LocalDate.now());
            comprobante.setEstado("Emitida");

            java.math.BigDecimal subtotalTotal = java.math.BigDecimal.ZERO;
            java.util.List<com.gestionservicios.models.ComprobanteDetalle> detalles = new java.util.ArrayList<>();

            for (ContratoServicio contrato : pendientes) {
                var servicio = contrato.getServicio();
                java.math.BigDecimal precioUnitario = servicio.getPrecioBase();
                if (contrato.getImportePersonalizado() != null) precioUnitario = contrato.getImportePersonalizado();
                int cantidad = 1;
                java.math.BigDecimal subtotal = precioUnitario.multiply(java.math.BigDecimal.valueOf(cantidad));
                subtotalTotal = subtotalTotal.add(subtotal);

                com.gestionservicios.models.ComprobanteDetalle detalle = new com.gestionservicios.models.ComprobanteDetalle();
                detalle.setComprobante(comprobante);
                detalle.setServicio(servicio);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setSubtotal(subtotal);
                detalle.setContratoServicioId(contrato.getId());
                detalles.add(detalle);
            }

            // calcular IVA si aplica
            java.math.BigDecimal ivaTotal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalConIva = subtotalTotal;
            if (com.gestionservicios.util.TaxRules.appliesIva(cliente.getCondicionFiscal())) {
                try {
                    java.math.BigDecimal ivaPercent = configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"));
                    java.math.BigDecimal ivaDecimal = ivaPercent.divide(new java.math.BigDecimal("100"));
                    ivaTotal = subtotalTotal.multiply(ivaDecimal).setScale(2, java.math.RoundingMode.HALF_UP);
                    totalConIva = subtotalTotal.add(ivaTotal);
                } catch (Exception e) {
                    ivaTotal = java.math.BigDecimal.ZERO;
                    totalConIva = subtotalTotal;
                }
            }

            comprobante.setTotal(totalConIva);
            comprobante.setSaldoPendiente(totalConIva);
            comprobante.setDetalles(detalles);

            // guardar comprobante usando ComprobanteService
            com.gestionservicios.models.Comprobante saved = comprobanteService.guardar(comprobante);

            // vincular al registro de facturación masiva
            vincularComprobanteAFacturacion(saved.getId(), fm.getId());

            contador++;
            totalFacturado = totalFacturado.add(saved.getTotal() == null ? java.math.BigDecimal.ZERO : saved.getTotal());
        }

        // actualizar resumen final
        fm.setCantidadFacturas(contador);
        fm.setTotalFacturado(totalFacturado);
        fm.setEstado("COMPLETADO");
        facturacionMasivaRepository.save(fm);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("cantidad", contador);
        result.put("total", totalFacturado);
        return result;
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
        List<Comprobante> comps = new java.util.ArrayList<>(comprobanteRepository.findAllById(fm.getComprobantes().stream().map(Comprobante::getId).toList()));
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

        // Map contratos por cliente
        java.util.Map<Long, java.util.List<ContratoServicio>> contratosPorCliente = contratos.stream()
            .collect(java.util.stream.Collectors.groupingBy(c -> c.getCliente().getId()));

        // clientes con al menos una factura en el periodo
        long clientesConFactura = contratosPorCliente.keySet().stream()
            .filter(clienteId -> comprobanteRepository.existsByClienteIdAndPeriodo(clienteId, periodo))
            .count();

        // clientes a facturar = clientes que tengan al menos un contrato sin comprobante en el periodo
        java.util.List<Long> clientesAFacturarIds = contratosPorCliente.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(c -> !comprobanteDetalleRepository.existsByContratoServicioIdAndComprobantePeriodo(c.getId(), periodo)))
            .map(java.util.Map.Entry::getKey)
            .toList();

        int seGeneraran = clientesAFacturarIds.size();

        // primeros 10 clientes a facturar con listado de contratos pendientes
        java.util.List<ClientePreviewItem> primerosClientes = clientesAFacturarIds.stream()
            .limit(10)
            .map(clienteId -> {
                String nombre = contratosPorCliente.get(clienteId).get(0).getCliente().getRazonSocial();
                java.util.List<Long> pendientes = contratosPorCliente.get(clienteId).stream()
                    .filter(c -> !comprobanteDetalleRepository.existsByContratoServicioIdAndComprobantePeriodo(c.getId(), periodo))
                    .map(ContratoServicio::getId)
                    .toList();
                ClientePreviewItem item = new ClientePreviewItem(clienteId, nombre, pendientes);
                try {
                    // obtener condición fiscal desde el primer contrato del cliente
                    var cond = contratosPorCliente.get(clienteId).get(0).getCliente().getCondicionFiscal();
                    item.setCondicionFiscal(cond != null ? cond.name() : null);
                    // indicar si aplica IVA según regla centralizada
                    boolean applies = com.gestionservicios.util.TaxRules.appliesIva(cond);
                    item.setApplyIva(applies);
                    // determinar tipo de comprobante para mostrar en la preview
                    String tipo = com.gestionservicios.util.TaxRules.tipoComprobantePorCondicion(cond);
                    item.setTipoComprobante(tipo);
                } catch (Exception e) {
                    item.setCondicionFiscal(null);
                    item.setApplyIva(false);
                }
                return item;
            })
            .toList();

        // periodo formateado MM/yyyy
        String periodoFormateado;
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(periodo);
            periodoFormateado = ym.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"));
        } catch (Exception e) {
            periodoFormateado = periodo;
        }

        return new FacturacionPreviewDTO(periodoFormateado, (int) clientesActivos, (int) clientesConFactura, seGeneraran, primerosClientes);
        }

}
