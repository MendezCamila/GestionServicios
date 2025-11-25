package com.gestionservicios.controllers;

import com.gestionservicios.models.Cliente;
import com.gestionservicios.services.ClienteService;
import com.gestionservicios.services.ContratoServicioService;
import com.gestionservicios.services.ServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ContratoServicioService contratoServicioService;
    private final ServicioService servicioService;

    public ClienteController(ClienteService clienteService, ContratoServicioService contratoServicioService, ServicioService servicioService) {
        this.clienteService = clienteService;
        this.contratoServicioService = contratoServicioService;
        this.servicioService = servicioService;
    }

    @GetMapping
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("titulo", "Listado de Clientes");
        model.addAttribute("contenido", "clientes");
        return "layout";
    }

    @GetMapping("/nuevo")
    public String nuevoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cliente_form";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@Valid @ModelAttribute Cliente cliente, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cliente", cliente);
            return "cliente_form";
        }
        clienteService.guardar(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obtenerPorId(id);
        model.addAttribute("cliente", cliente);
        return "cliente_form";
    }

    @GetMapping("/{id}/servicios")
    public String verServiciosCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obtenerPorId(id);
        model.addAttribute("cliente", cliente);
        model.addAttribute("contratos", contratoServicioService.listarPorCliente(id));
        model.addAttribute("servicios", servicioService.listarServicios());
        model.addAttribute("titulo", "Servicios del cliente");
        model.addAttribute("contenido", "cliente_servicios");
        return "layout";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return "redirect:/clientes";
    }

}
