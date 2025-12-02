package com.gestionservicios.controllers;

import com.gestionservicios.models.Servicio;
import com.gestionservicios.services.ServicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    
	private final ServicioService servicioService;

    // Constructor Injection
	public ServicioController(ServicioService servicioService) {
		this.servicioService = servicioService;
	}

    // Listar todos los servicios
	@GetMapping
	public String listarServicios(Model model) {
		model.addAttribute("servicios", servicioService.listarServicios());
		model.addAttribute("titulo", "Listado de Servicios");
		model.addAttribute("contenido", "servicios");
		return "layout";
	}

	@GetMapping("/nuevo")
	public String nuevoServicio(Model model) {
		model.addAttribute("servicio", new Servicio());
		return "servicio_form";
	}

	@GetMapping("/search")
	@ResponseBody
	public java.util.List<Servicio> buscarServicios(@RequestParam(value = "q", required = false) String q) {
		if (q == null || q.isBlank()) return servicioService.listarServicios();
		return servicioService.buscarPorNombre(q);
	}

	@PostMapping("/guardar")
	public String guardarServicio(@Valid @ModelAttribute Servicio servicio, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("servicio", servicio);
			return "servicio_form";
		}
		servicioService.guardar(servicio);
		return "redirect:/servicios";
	}

	@GetMapping("/editar/{id}")
	public String editarServicio(@PathVariable Long id, Model model) {
		Servicio servicio = servicioService.obtenerPorId(id);
		model.addAttribute("servicio", servicio);
		return "servicio_form";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarServicio(@PathVariable Long id) {
		servicioService.eliminar(id);
		return "redirect:/servicios";
	}

}
