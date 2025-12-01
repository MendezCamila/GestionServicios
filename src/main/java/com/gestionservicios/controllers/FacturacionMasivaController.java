package com.gestionservicios.controllers;

import com.gestionservicios.models.FacturacionMasiva;
import com.gestionservicios.repositories.FacturacionMasivaRepository;
import com.gestionservicios.services.FacturacionMasivaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/facturacion/masiva")
public class FacturacionMasivaController {

    private final FacturacionMasivaService facturacionMasivaService;
    private final FacturacionMasivaRepository facturacionMasivaRepository;

    public FacturacionMasivaController(FacturacionMasivaService facturacionMasivaService, FacturacionMasivaRepository facturacionMasivaRepository) {
        this.facturacionMasivaService = facturacionMasivaService;
        this.facturacionMasivaRepository = facturacionMasivaRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("facturaciones", facturacionMasivaRepository.findAll());
        model.addAttribute("titulo", "Facturación Masiva");
        model.addAttribute("contenido", "facturacion_masiva");
        return "layout";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("titulo", "Nueva facturación masiva");
        model.addAttribute("contenido", "facturacion_masiva_form");
        return "layout";
    }

    @PostMapping
    public String crear(@RequestParam("periodo") String periodo, @RequestParam("usuario") String usuario, Model model) {
        FacturacionMasiva fm = facturacionMasivaService.crearFacturacion(usuario, periodo);
        return "redirect:/facturacion/masiva";
    }
}
