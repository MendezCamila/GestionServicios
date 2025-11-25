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
}
