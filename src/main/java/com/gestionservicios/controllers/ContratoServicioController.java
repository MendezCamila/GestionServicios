package com.gestionservicios.controllers;

import com.gestionservicios.models.ContratoServicio;
import com.gestionservicios.services.ClienteService;
import com.gestionservicios.services.ContratoServicioService;
import com.gestionservicios.services.ServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/contratos")
public class ContratoServicioController {

    private final ContratoServicioService contratoServicioService;
    private final ClienteService clienteService;
    private final ServicioService servicioService;

    // Constructor Injection de lo que se necesita
    public ContratoServicioController(ContratoServicioService contratoServicioService,
                                      ClienteService clienteService,
                                      ServicioService servicioService) {
        this.contratoServicioService = contratoServicioService;
        this.clienteService = clienteService;
        this.servicioService = servicioService;
    }

    @GetMapping
    public String listarContratos(Model model) {
        model.addAttribute("contratos", contratoServicioService.listarContratos());
        model.addAttribute("titulo", "Contratos de Servicios");
        model.addAttribute("contenido", "contratos");
        return "layout";
    }

    @GetMapping("/nuevo")
    public String nuevoContrato(Model model) {
        model.addAttribute("contrato", new ContratoServicio());
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("servicios", servicioService.listarServicios());
        return "contrato_form";
    }

    @PostMapping("/guardar")
    public String guardarContrato(@Valid @ModelAttribute ContratoServicio contrato, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarClientes());
            model.addAttribute("servicios", servicioService.listarServicios());
            return "contrato_form";
        }
        contratoServicioService.guardar(contrato);
        return "redirect:/contratos";
    }

    @GetMapping("/editar/{id}")
    public String editarContrato(@PathVariable Long id, Model model) {
        ContratoServicio contrato = contratoServicioService.obtenerPorId(id);
        model.addAttribute("contrato", contrato);
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("servicios", servicioService.listarServicios());
        return "contrato_form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarContrato(@PathVariable Long id) {
        contratoServicioService.eliminar(id);
        return "redirect:/contratos";
    }

    @PostMapping("/crear-para-cliente/{clienteId}")
    public String crearContratosParaCliente(@PathVariable Long clienteId,
                                           @RequestParam(name = "servicioIds", required = false) java.util.List<Long> servicioIds) {
        if (servicioIds == null || servicioIds.isEmpty()) {
            return "redirect:/clientes/" + clienteId + "/servicios";
        }
        var cliente = clienteService.obtenerPorId(clienteId);
        for (Long servicioId : servicioIds) {
            // evitar duplicados activos
            boolean exists = contratoServicioService.existeContratoActivo(clienteId, servicioId);
            if (exists) continue;
            var servicio = servicioService.obtenerPorId(servicioId);
            ContratoServicio contrato = new ContratoServicio();
            contrato.setCliente(cliente);
            contrato.setServicio(servicio);
            contrato.setFechaInicio(java.time.LocalDate.now());
            contrato.setFechaFin(null);
            contrato.setImportePersonalizado(null);
            contrato.setEstado("Activo");
            contratoServicioService.guardar(contrato);
        }
        return "redirect:/clientes/" + clienteId + "/servicios";
    }
}
