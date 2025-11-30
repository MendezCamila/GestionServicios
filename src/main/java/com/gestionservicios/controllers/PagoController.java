package com.gestionservicios.controllers;

import com.gestionservicios.services.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public String listarPagos(Model model) {
        model.addAttribute("pagos", pagoService.listarPagos());
        model.addAttribute("titulo", "Pagos");
        model.addAttribute("contenido", "pagos");
        return "layout";
    }

}
