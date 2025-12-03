package com.gestionservicios.controllers;

import com.gestionservicios.models.FacturacionMasiva;
import com.gestionservicios.repositories.FacturacionMasivaRepository;
import com.gestionservicios.services.FacturacionMasivaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import com.gestionservicios.models.FacturacionPreviewDTO;

@Controller
@RequestMapping("/facturacion/masiva")
public class FacturacionMasivaController {

    private final FacturacionMasivaService facturacionMasivaService;
    private final FacturacionMasivaRepository facturacionMasivaRepository;

    public FacturacionMasivaController(FacturacionMasivaService facturacionMasivaService, FacturacionMasivaRepository facturacionMasivaRepository) {
        this.facturacionMasivaService = facturacionMasivaService;
        this.facturacionMasivaRepository = facturacionMasivaRepository;
    }

    @GetMapping("/preview")
    @ResponseBody
    public FacturacionPreviewDTO preview(@RequestParam(value = "periodo", required = false) String periodo) {
        if (periodo == null || periodo.isBlank()) {
            periodo = java.time.YearMonth.now().toString();
        }
        return facturacionMasivaService.simularFacturacionMasiva(periodo);
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
    public String crear(@RequestParam("periodo") String periodo, @RequestParam("usuario") String usuario, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.util.Map<String, Object> result = facturacionMasivaService.ejecutarFacturacionMasiva(usuario, periodo);
            int cantidad = (int) result.getOrDefault("cantidad", 0);
            java.math.BigDecimal total = (java.math.BigDecimal) result.getOrDefault("total", java.math.BigDecimal.ZERO);
            redirectAttributes.addFlashAttribute("successMessage", "Se generaron " + cantidad + " comprobantes por un total de " + total);
        } catch (Exception e) {
            // log full stacktrace to help debugging
            e.printStackTrace();
            String err = e.getClass().getName() + (e.getMessage() != null ? (": " + e.getMessage()) : "");
            redirectAttributes.addFlashAttribute("errorMessage", "Error al generar facturación masiva: " + err);
        }
        return "redirect:/facturacion/masiva";
    }
}
