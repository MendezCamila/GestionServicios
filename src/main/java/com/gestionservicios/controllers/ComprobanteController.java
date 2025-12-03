package com.gestionservicios.controllers;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.models.Cliente;
import com.gestionservicios.models.ContratoServicio;
import com.gestionservicios.models.Servicio;
import com.gestionservicios.services.ClienteService;
import com.gestionservicios.services.ComprobanteService;
import com.gestionservicios.services.ContratoServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/facturacion")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;
    private final ClienteService clienteService;
    private final ContratoServicioService contratoServicioService;
    private final com.gestionservicios.services.ConfiguracionService configuracionService;

    public ComprobanteController(ComprobanteService comprobanteService, ClienteService clienteService, ContratoServicioService contratoServicioService, com.gestionservicios.services.ConfiguracionService configuracionService) {
        this.comprobanteService = comprobanteService;
        this.clienteService = clienteService;
        this.contratoServicioService = contratoServicioService;
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public String listarComprobantes(Model model) {

        java.util.List<Comprobante> lista = comprobanteService.listarComprobantes();

        model.addAttribute("comprobantes", lista);
        model.addAttribute("titulo", "Facturación");
        model.addAttribute("contenido", "comprobantes");
        return "layout";
    }

    @GetMapping("/individual")
    public String facturacionIndividual(@RequestParam(name = "q", required = false) String q, Model model) {
        List<Cliente> clientes = clienteService.buscarPorNombreOCuit(q);
        model.addAttribute("clientes", clientes);
        model.addAttribute("q", q);
        model.addAttribute("titulo", "Facturación Individual");
        model.addAttribute("contenido", "facturacion_individual");
        return "layout";
    }

    @GetMapping("/cliente/{id}")
    public String verClienteParaFacturar(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obtenerPorId(id);
        model.addAttribute("cliente", cliente);
        model.addAttribute("contratos", contratoServicioService.listarPorCliente(id));
        // exponer condición fiscal explícitamente (útil en la vista)
        model.addAttribute("condicionFiscal", cliente != null ? cliente.getCondicionFiscal() : null);
        model.addAttribute("titulo", "Facturación - Cliente");
        // calcular si corresponde aplicar IVA (solo para RESPONSABLE_INSCRIPTO)
        boolean showIva = cliente != null && cliente.getCondicionFiscal() == com.gestionservicios.models.CondicionFiscal.RESPONSABLE_INSCRIPTO;
        model.addAttribute("showIva", showIva);
        try {
            java.math.BigDecimal ivaPercent = configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"));
            model.addAttribute("ivaPercent", ivaPercent);
        } catch (Exception e) {
            model.addAttribute("ivaPercent", new java.math.BigDecimal("21"));
        }
        model.addAttribute("contenido", "facturacion_cliente");
        return "layout";
    }

    @PostMapping("/cliente/{id}/generar")
    public String generarComprobante(@PathVariable Long id, HttpServletRequest request) {
        Cliente cliente = clienteService.obtenerPorId(id);
        String[] contratoIds = request.getParameterValues("contratoIds");
        if (contratoIds == null || contratoIds.length == 0) {
            return "redirect:/facturacion/cliente/" + id;
        }

        com.gestionservicios.models.Comprobante comprobante = new com.gestionservicios.models.Comprobante();
        comprobante.setCliente(cliente);
        // determinar tipo de comprobante según condición fiscal
        comprobante.setTipoComprobante(com.gestionservicios.util.TaxRules.tipoComprobantePorCondicion(cliente != null ? cliente.getCondicionFiscal() : null));
        comprobante.setFechaEmision(java.time.LocalDate.now());
        comprobante.setEstado("Emitida");

        java.math.BigDecimal subtotalTotal = java.math.BigDecimal.ZERO;
        java.util.List<com.gestionservicios.models.ComprobanteDetalle> detalles = new java.util.ArrayList<>();

        for (String sId : contratoIds) {
                try {
                Long contratoId = Long.parseLong(sId);
                ContratoServicio contrato = contratoServicioService.obtenerPorId(contratoId);
                if (contrato == null) continue;
                Servicio servicio = contrato.getServicio();

                String qtyParam = request.getParameter("cantidad_" + contratoId);
                int cantidad = 1;
                try { cantidad = Integer.parseInt(qtyParam); } catch (Exception ignored) {}

                java.math.BigDecimal precioUnitario = servicio.getPrecioBase();
                if (contrato.getImportePersonalizado() != null) {
                    precioUnitario = contrato.getImportePersonalizado();
                }

                java.math.BigDecimal subtotal = precioUnitario.multiply(java.math.BigDecimal.valueOf(cantidad));
                subtotalTotal = subtotalTotal.add(subtotal);

                com.gestionservicios.models.ComprobanteDetalle detalle = new com.gestionservicios.models.ComprobanteDetalle();
                detalle.setComprobante(comprobante);
                detalle.setServicio(servicio);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setSubtotal(subtotal);
                // Asignar contratoServicioId para trazabilidad (regla: todos los detalles provienen de un contrato)
                detalle.setContratoServicioId(contrato.getId());
                detalles.add(detalle);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // Calcular IVA si corresponde según condición fiscal del cliente
        java.math.BigDecimal ivaTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalConIva = subtotalTotal;
        if (com.gestionservicios.util.TaxRules.appliesIva(cliente.getCondicionFiscal())) {
            try {
                java.math.BigDecimal ivaPercent = configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"));
                java.math.BigDecimal ivaDecimal = ivaPercent.divide(new java.math.BigDecimal("100"));
                ivaTotal = subtotalTotal.multiply(ivaDecimal).setScale(2, java.math.RoundingMode.HALF_UP);
                totalConIva = subtotalTotal.add(ivaTotal);
            } catch (Exception e) {
                // si falla la lectura de config, dejamos ivaTotal en 0 y continuamos
                ivaTotal = java.math.BigDecimal.ZERO;
                totalConIva = subtotalTotal;
            }
        }

        comprobante.setTotal(totalConIva);
        // Inicializar saldo pendiente al total al crear la factura
        comprobante.setSaldoPendiente(totalConIva);
        comprobante.setDetalles(detalles);

        // opcional: almacenar subtotal/iva en atributos transitorios del comprobante antes de guardar (no persistidos)
        // Los mostramos en la vista calculándolos de nuevo al ver el comprobante

        comprobanteService.guardar(comprobante);

        return "redirect:/facturacion";
    }

    @GetMapping("/{id}")
    public String verComprobante(@PathVariable Long id, Model model) {
        Comprobante comprobante = comprobanteService.obtenerPorId(id);
        if (comprobante == null) return "redirect:/facturacion";
        model.addAttribute("comprobante", comprobante);
        // calcular subtotal y IVA para mostrar en la vista
        java.math.BigDecimal subtotalTotal = java.math.BigDecimal.ZERO;
        if (comprobante.getDetalles() != null) {
            for (var d : comprobante.getDetalles()) {
                if (d.getSubtotal() != null) subtotalTotal = subtotalTotal.add(d.getSubtotal());
            }
        }

        java.math.BigDecimal ivaTotal = java.math.BigDecimal.ZERO;
        try {
            if (comprobante.getCliente() != null && com.gestionservicios.util.TaxRules.appliesIva(comprobante.getCliente().getCondicionFiscal())) {
                java.math.BigDecimal ivaPercent = configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"));
                java.math.BigDecimal ivaDecimal = ivaPercent.divide(new java.math.BigDecimal("100"));
                ivaTotal = subtotalTotal.multiply(ivaDecimal).setScale(2, java.math.RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            ivaTotal = java.math.BigDecimal.ZERO;
        }

        model.addAttribute("subtotalTotal", subtotalTotal);
        model.addAttribute("ivaTotal", ivaTotal);
        boolean showIva = ivaTotal != null && ivaTotal.compareTo(java.math.BigDecimal.ZERO) > 0;
        model.addAttribute("showIva", showIva);
        try {
            java.math.BigDecimal ivaPercent = configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"));
            model.addAttribute("ivaPercent", ivaPercent);
        } catch (Exception e) {
            model.addAttribute("ivaPercent", new java.math.BigDecimal("21"));
        }
        model.addAttribute("titulo", "Comprobante " + id);
        model.addAttribute("contenido", "comprobante_detalle");
        return "layout";
    }
    
    @GetMapping("/{id}/fragment")
    @org.springframework.web.bind.annotation.ResponseBody
    public String verComprobanteFragment(@PathVariable Long id) {
        try {
            Comprobante comprobante = comprobanteService.obtenerPorId(id);
            if (comprobante == null) return "<div class='p-4 text-sm text-gray-600'>Comprobante no encontrado</div>";

            StringBuilder sb = new StringBuilder();
            sb.append("<div>");
            sb.append("<div class='flex items-center justify-between mb-4'><h4 class='font-semibold'>Comprobante ").append(comprobante.getId()).append("</h4></div>");
            sb.append("<div class='bg-white p-4 rounded shadow mb-4'>");
            if (comprobante.getCliente() != null) {
                sb.append("<div class='text-sm text-gray-600'>Cliente</div>");
                sb.append("<div class='font-semibold'>").append(comprobante.getCliente().getRazonSocial()).append("</div>");
            }
            if (comprobante.getTipoComprobante() != null) {
                sb.append("<div class='text-sm text-gray-600 mt-2'>Tipo: ").append(comprobante.getTipoComprobante()).append("</div>");
            }
            sb.append("<div class='text-sm text-gray-600 mt-2'>Fecha: ").append(comprobante.getFechaEmision()).append("</div>");
            sb.append("</div>");

            java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es","AR"));
            boolean aplicaIva = comprobante.getCliente() != null && com.gestionservicios.util.TaxRules.appliesIva(comprobante.getCliente().getCondicionFiscal());

            sb.append("<table class='w-full bg-white rounded overflow-hidden'><thead class='bg-gray-50'><tr><th class='p-2 text-left text-sm text-gray-600'>Servicio</th><th class='p-2 text-left text-sm text-gray-600'>Cant</th><th class='p-2 text-left text-sm text-gray-600'>Precio U.</th><th class='p-2 text-left text-sm text-gray-600'>Subtotal</th><th class='p-2 text-left text-sm text-gray-600'>IVA</th></tr></thead><tbody>");
            if (comprobante.getDetalles() != null) {
                for (var d : comprobante.getDetalles()) {
                    sb.append("<tr class='border-b'><td class='p-2'>");
                    sb.append(d.getServicio() != null ? d.getServicio().getNombre() : "-");
                    sb.append("</td><td class='p-2'>").append(d.getCantidad()).append("</td>");
                    String precioStr = d.getPrecioUnitario() != null ? nf.format(d.getPrecioUnitario()) : "-";
                    String subtotalStr = d.getSubtotal() != null ? nf.format(d.getSubtotal()) : "-";
                    sb.append("<td class='p-2'>").append(precioStr).append("</td>");
                    sb.append("<td class='p-2'>").append(subtotalStr).append("</td>");
                    sb.append("<td class='p-2'>");
                    if (aplicaIva) {
                        sb.append("<span class='inline-block px-2 py-0.5 text-xs rounded bg-yellow-100 text-yellow-800'>Aplica IVA</span>");
                    } else {
                        sb.append("-");
                    }
                    sb.append("</td></tr>");
                }
            }
            sb.append("</tbody></table>");

            // calcular subtotal y IVA para mostrar en el fragmento
            java.math.BigDecimal subtotalTotal = java.math.BigDecimal.ZERO;
            if (comprobante.getDetalles() != null) {
                for (var d : comprobante.getDetalles()) {
                    if (d.getSubtotal() != null) subtotalTotal = subtotalTotal.add(d.getSubtotal());
                }
            }

            java.math.BigDecimal ivaTotal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalToShow = comprobante.getTotal() != null ? comprobante.getTotal() : subtotalTotal;
            if (aplicaIva) {
                try {
                    java.math.BigDecimal ivaPercent = configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"));
                    java.math.BigDecimal ivaDecimal = ivaPercent.divide(new java.math.BigDecimal("100"));
                    ivaTotal = subtotalTotal.multiply(ivaDecimal).setScale(2, java.math.RoundingMode.HALF_UP);
                    totalToShow = subtotalTotal.add(ivaTotal);
                } catch (Exception e) {
                    ivaTotal = java.math.BigDecimal.ZERO;
                }
            }

            String subtotalStr = nf.format(subtotalTotal);
            String ivaStr = nf.format(ivaTotal);
            String totalStr = nf.format(totalToShow);

            sb.append("<div class='mt-4 text-right'>");
            sb.append("<div class='text-sm text-gray-600'>Subtotal de servicios: <span>").append(subtotalStr).append("</span></div>");
            if (aplicaIva) {
                sb.append("<div class='text-sm text-gray-600'>IVA (<span>").append(configuracionService.getIvaGeneralOrDefault(new java.math.BigDecimal("21"))).append("</span>%): <span>").append(ivaStr).append("</span></div>");
            }
            sb.append("<div class='mt-2 text-lg font-semibold'>Total: <span class='text-lg'>").append(totalStr).append("</span></div>");
            sb.append("</div>");
            sb.append("</div>");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<div class='text-red-600'>Error interno: " + e.getMessage() + "</div>";
        }
    }
    
}
