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

    public ComprobanteController(ComprobanteService comprobanteService, ClienteService clienteService, ContratoServicioService contratoServicioService) {
        this.comprobanteService = comprobanteService;
        this.clienteService = clienteService;
        this.contratoServicioService = contratoServicioService;
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
        model.addAttribute("titulo", "Facturación - Cliente");
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
        comprobante.setTipoComprobante("Factura");
        comprobante.setFechaEmision(java.time.LocalDate.now());
        comprobante.setEstado("Emitida");

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
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
                total = total.add(subtotal);

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

        comprobante.setTotal(total);
        // Inicializar saldo pendiente al total al crear la factura
        comprobante.setSaldoPendiente(total);
        comprobante.setDetalles(detalles);

        comprobanteService.guardar(comprobante);

        return "redirect:/facturacion";
    }

    @GetMapping("/{id}")
    public String verComprobante(@PathVariable Long id, Model model) {
        Comprobante comprobante = comprobanteService.obtenerPorId(id);
        if (comprobante == null) return "redirect:/facturacion";
        model.addAttribute("comprobante", comprobante);
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
            sb.append("<div class='text-sm text-gray-600 mt-2'>Fecha: ").append(comprobante.getFechaEmision()).append("</div>");
            sb.append("</div>");

            sb.append("<table class='w-full bg-white rounded overflow-hidden'><thead class='bg-gray-50'><tr><th class='p-2 text-left text-sm text-gray-600'>Servicio</th><th class='p-2 text-left text-sm text-gray-600'>Cant</th><th class='p-2 text-left text-sm text-gray-600'>Precio U.</th><th class='p-2 text-left text-sm text-gray-600'>Subtotal</th></tr></thead><tbody>");
            if (comprobante.getDetalles() != null) {
                for (var d : comprobante.getDetalles()) {
                    sb.append("<tr class='border-b'><td class='p-2'>");
                    sb.append(d.getServicio() != null ? d.getServicio().getNombre() : "-");
                    sb.append("</td><td class='p-2'>").append(d.getCantidad()).append("</td><td class='p-2'>").append(d.getPrecioUnitario()).append("</td><td class='p-2'>").append(d.getSubtotal()).append("</td></tr>");
                }
            }
            sb.append("</tbody></table>");

            sb.append("<div class='mt-4 text-right'><strong>Total: </strong>").append(comprobante.getTotal()).append("</div>");
            sb.append("</div>");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<div class='text-red-600'>Error interno: " + e.getMessage() + "</div>";
        }
    }
    
}
