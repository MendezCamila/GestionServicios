package com.gestionservicios.controllers;

import com.gestionservicios.services.PagoService;
import com.gestionservicios.services.ClienteService;
import com.gestionservicios.services.ComprobanteService;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final ClienteService clienteService;
    private final ComprobanteService comprobanteService;

    public PagoController(PagoService pagoService, ClienteService clienteService, ComprobanteService comprobanteService) {
        this.pagoService = pagoService;
        this.clienteService = clienteService;
        this.comprobanteService = comprobanteService;
    }

    @GetMapping
    public String listarPagos(Model model) {
        var pagos = pagoService.listarPagos();
        model.addAttribute("pagos", pagos);
        // calcular estados resumidos y pasarlos a la vista como mapa id->estado
        Map<Long, String> estados = pagos.stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> pagoService.estadoResumido(p)));
        model.addAttribute("estadosPago", estados);
        // agrupar métodos de pago (nombres + montos) por pago y pasar a la vista
        java.util.Map<Long, java.util.List<String>> metodosList = pagoService.agruparMetodosConMontosLista(pagos);
        java.util.Map<Long, String> metodosFull = new java.util.HashMap<>();
        java.util.Map<Long, String> metodosShort = new java.util.HashMap<>();
        for (var e : metodosList.entrySet()) {
            Long id = e.getKey();
            java.util.List<String> items = e.getValue();
            String full = String.join(", ", items);
            metodosFull.put(id, full);
            if (items.size() <= 2) {
                metodosShort.put(id, full);
            } else {
                String firstTwo = String.join(", ", items.subList(0, 2));
                metodosShort.put(id, firstTwo + " +" + (items.size() - 2));
            }
        }
        model.addAttribute("metodosPagoFull", metodosFull);
        model.addAttribute("metodosPagoShort", metodosShort);
        model.addAttribute("titulo", "Pagos");
        model.addAttribute("contenido", "pagos");
        return "layout";
    }

    @GetMapping("/{id}")
    public String verPago(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        var pago = pagoService.obtenerPorId(id);
        if (pago == null) {
            return "redirect:/pagos";
        }
        model.addAttribute("pago", pago);
        model.addAttribute("titulo", "Detalle de Pago");
        model.addAttribute("contenido", "pago_detalle");
        return "layout";
    }

    @GetMapping("/nuevo")
    public String nuevoPago(Model model) {
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("titulo", "Nuevo Pago");
        model.addAttribute("contenido", "pagos_nuevo");
        return "layout";
    }

    @PostMapping
    public String crearPago(HttpServletRequest request, Model model) {
        try {
            String clienteIdStr = request.getParameter("clienteId");
            if (clienteIdStr == null) return "redirect:/pagos/nuevo";
            Long clienteId = Long.parseLong(clienteIdStr);
            var cliente = clienteService.obtenerPorId(clienteId);

            com.gestionservicios.models.Pago pago = new com.gestionservicios.models.Pago();
            pago.setCliente(cliente);
            pago.setFechaPago(java.time.LocalDateTime.now());
            pago.setMetodoPago(com.gestionservicios.models.MetodoPago.EFECTIVO);
            pago.setMontoIngresado(java.math.BigDecimal.ZERO);
            pago.setObservaciones(request.getParameter("observaciones"));

            // parse comprobantes
            String[] comprobanteIds = request.getParameterValues("factura_id");
            java.util.List<com.gestionservicios.models.PagoDetalle> detalles = new java.util.ArrayList<>();
            if (comprobanteIds != null) {
                for (int i = 0; i < comprobanteIds.length; i++) {
                    String idStr = comprobanteIds[i];
                    String montoStr = request.getParameter("monto_aplicar_" + idStr);
                    try {
                        Long compId = Long.parseLong(idStr);
                        java.math.BigDecimal monto = new java.math.BigDecimal(montoStr == null || montoStr.isBlank() ? "0" : montoStr);
                        com.gestionservicios.models.PagoDetalle det = new com.gestionservicios.models.PagoDetalle();
                        com.gestionservicios.models.Comprobante c = new com.gestionservicios.models.Comprobante();
                        c.setId(compId);
                        det.setComprobante(c);
                        det.setMontoAplicado(monto);
                        detalles.add(det);
                    } catch (Exception ignored) {}
                }
            }

            // parse payment methods to compute monto ingresado
            String[] metodos = request.getParameterValues("metodo_pago[]");
            String[] metodosMontos = request.getParameterValues("metodo_monto[]");
            java.math.BigDecimal totalPagos = java.math.BigDecimal.ZERO;
            java.util.List<com.gestionservicios.models.PagoMetodo> metodosList = new java.util.ArrayList<>();
            if (metodos != null) {
                for (int i = 0; i < metodos.length; i++) {
                    String m = metodos[i];
                    String montoStr = (metodosMontos != null && metodosMontos.length > i) ? metodosMontos[i] : "0";
                    try {
                        java.math.BigDecimal monto = new java.math.BigDecimal(montoStr == null || montoStr.isBlank() ? "0" : montoStr);
                        totalPagos = totalPagos.add(monto);
                        com.gestionservicios.models.PagoMetodo pm = new com.gestionservicios.models.PagoMetodo();
                        try {
                            pm.setMetodo(com.gestionservicios.models.MetodoPago.valueOf(m));
                        } catch (Exception ex) {
                            pm.setMetodo(null);
                        }
                        pm.setMonto(monto);
                        metodosList.add(pm);
                    } catch (Exception ignored) {}
                }
            }
            pago.setMontoIngresado(totalPagos);

            // crear pago con detalles y métodos
            pagoService.crearPagoConDetalles(pago, detalles, metodosList);

            return "redirect:/pagos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "layout";
        }
    }

    @GetMapping(value = "/facturas", produces = "application/json")
    @ResponseBody
    public java.util.List<com.gestionservicios.dto.ComprobantePendienteDTO> facturasPendientes(@RequestParam("clienteId") Long clienteId) {
        var list = pagoService.listarFacturasPendientesDTO(clienteId);
        // simple logging to help debugging
        System.out.println("[PagoController] facturas pendientes para cliente " + clienteId + ": " + (list == null ? 0 : list.size()));
        return list;
    }

}
