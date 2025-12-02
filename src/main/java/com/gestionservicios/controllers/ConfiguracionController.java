package com.gestionservicios.controllers;

import com.gestionservicios.services.ConfiguracionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/iva")
    public String editarIva(Model model) {
        BigDecimal iva = configuracionService.getIvaGeneralOrDefault(new BigDecimal("21"));
        model.addAttribute("ivaValue", iva);
        return "configuracion/iva_form";
    }

    @PostMapping("/iva")
    public String guardarIva(@RequestParam(name = "iva") String ivaText, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (ivaText == null || ivaText.isBlank()) throw new IllegalArgumentException("El valor del IVA no puede estar vacío");
            // aceptar tanto '21' como '21.0' y '0.21' (decisión: aquí se espera porcentaje, p.ej. 21)
            BigDecimal iva = new BigDecimal(ivaText.trim());
            configuracionService.setIvaGeneral(iva);
            redirectAttributes.addFlashAttribute("successMessage", "IVA actualizado a " + iva.toPlainString());
            return "redirect:/configuracion/iva";
        } catch (NumberFormatException ex) {
            model.addAttribute("errorMessage", "Formato inválido para IVA");
            model.addAttribute("ivaValue", ivaText);
            return "configuracion/iva_form";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("ivaValue", ivaText);
            return "configuracion/iva_form";
        }
    }
}
