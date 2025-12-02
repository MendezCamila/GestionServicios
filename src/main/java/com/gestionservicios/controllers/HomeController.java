package com.gestionservicios.controllers;

import com.gestionservicios.services.ClienteService;
import com.gestionservicios.services.ComprobanteService;
import com.gestionservicios.services.ServicioService;
import com.gestionservicios.repositories.ComprobanteDetalleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
public class HomeController {

    private final ClienteService clienteService;
    private final ComprobanteService comprobanteService;
    private final ComprobanteDetalleRepository comprobanteDetalleRepository;
    private final ServicioService servicioService;

    public HomeController(ClienteService clienteService,
                          ComprobanteService comprobanteService,
                          ComprobanteDetalleRepository comprobanteDetalleRepository,
                          ServicioService servicioService) {
        this.clienteService = clienteService;
        this.comprobanteService = comprobanteService;
        this.comprobanteDetalleRepository = comprobanteDetalleRepository;
        this.servicioService = servicioService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Clientes activos
        long activos = clienteService.listarClientes().stream()
                .filter(c -> c.getEstado() != null && c.getEstado().equalsIgnoreCase("activo"))
                .count();

        // Servicios más consumidos (por cantidad sumada en comprobante_detalles)
        List<com.gestionservicios.models.ComprobanteDetalle> detalles = comprobanteDetalleRepository.findAll();
        Map<String, Integer> counts = new HashMap<>();
        for (var d : detalles) {
            String nombre = "-";
            try {
                if (d.getServicio() != null && d.getServicio().getNombre() != null) nombre = d.getServicio().getNombre();
            } catch (Exception e) {
                // ignore lazy issues
            }
            Integer qty = d.getCantidad() == null ? 0 : d.getCantidad();
            counts.merge(nombre, qty, Integer::sum);
        }
        List<Map.Entry<String,Integer>> topServices = new ArrayList<>(counts.entrySet());
        topServices.sort((a,b) -> b.getValue().compareTo(a.getValue()));
        if (topServices.size() > 5) topServices = topServices.subList(0,5);

        // Venta último mes (último mes calendario)
        YearMonth last = YearMonth.now().minusMonths(1);
        LocalDate start = last.atDay(1);
        LocalDate end = last.atEndOfMonth();
        BigDecimal total = comprobanteService.listarComprobantes().stream()
                .filter(c -> c.getFechaEmision() != null && ( !c.getFechaEmision().isBefore(start) && !c.getFechaEmision().isAfter(end) ))
                .map(c -> c.getTotal() == null ? BigDecimal.ZERO : c.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Formatear números (es-AR)
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        String totalFmt = nf.format(total);

        model.addAttribute("titulo", "Inicio");
        model.addAttribute("contenido", "home");
        model.addAttribute("clientesActivos", activos);
        model.addAttribute("topServices", topServices);
        model.addAttribute("ventaUltimoMes", totalFmt);

        return "layout";
    }
}
