package com.gestionservicios.controllers;

import com.gestionservicios.models.Cliente;
import com.gestionservicios.services.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
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

    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return "redirect:/clientes";
    }

}
