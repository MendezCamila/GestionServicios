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
import org.springframework.validation.FieldError;
import org.springframework.dao.DataIntegrityViolationException;


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

    @GetMapping("/search")
    @ResponseBody
    public java.util.List<Cliente> buscarClientes(@RequestParam(value = "q", required = false) String q) {
        if (q == null || q.isBlank()) return clienteService.listarClientes();
        return clienteService.buscarPorNombreOCuit(q);
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
        try {
            clienteService.guardar(cliente);
            return "redirect:/clientes";
        } catch (DataIntegrityViolationException ex) {
            // probable duplicado por unique constraint en cuit
            bindingResult.addError(new FieldError("cliente", "cuit", "Ya existe un cliente con ese CUIT"));
            model.addAttribute("cliente", cliente);
            return "cliente_form";
        }
    }

    @GetMapping("/editar/{id:\\d+}")
    public String editarCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obtenerPorId(id);
        // cliente ahora se carga directamente y se pasa al modelo
        model.addAttribute("cliente", cliente);
        return "cliente_form";
    }

    @GetMapping("/{id}/servicios")
    public String verServiciosCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obtenerPorId(id);
        model.addAttribute("cliente", cliente);
        model.addAttribute("contratos", contratoServicioService.listarPorCliente(id));//listar los servicios por cliente
        model.addAttribute("servicios", servicioService.listarServicios());//listar todos los servicios
        model.addAttribute("titulo", "Servicios del cliente");//le mando titulo
        model.addAttribute("contenido", "cliente_servicios");//le mando contenido
        return "layout";
    }

    @GetMapping("/eliminar/{id:\\d+}")
    public String eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return "redirect:/clientes";
    }


}
