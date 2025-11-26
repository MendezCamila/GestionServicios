package com.gestionservicios.controllers;

import com.gestionservicios.models.Comprobante;
import com.gestionservicios.models.Cliente;
import com.gestionservicios.services.ClienteService;
import com.gestionservicios.services.ComprobanteService;
import com.gestionservicios.services.ContratoServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
}
