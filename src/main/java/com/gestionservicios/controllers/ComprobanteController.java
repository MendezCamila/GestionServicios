package com.gestionservicios.controllers;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.models.Cliente;
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
        model.addAttribute("comprobantes", comprobanteService.listarComprobantes());
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
                var contrato = contratoServicioService.obtenerPorId(contratoId);
                if (contrato == null) continue;
                var servicio = contrato.getServicio();

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
        var comprobante = comprobanteService.obtenerPorId(id);
        if (comprobante == null) return "redirect:/facturacion";
        model.addAttribute("comprobante", comprobante);
        model.addAttribute("titulo", "Comprobante " + id);
        model.addAttribute("contenido", "comprobante_detalle");
        return "layout";
    }
    
}
