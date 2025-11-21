package com.gestionservicios.services;

import com.gestionservicios.models.Servicio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioService {
	private List<Servicio> servicios = new ArrayList<>();
	private Long contador = 1L;

	public List<Servicio> listarServicios() {
		return servicios;
	}

	public Servicio obtenerPorId(long id) {
		return servicios.stream()
				.filter(s -> s.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	public Servicio guardar(Servicio servicio) {
		if (servicio.getId() == null) {
			servicio.setId(contador++);
			servicios.add(servicio);
		} else {
			servicios.removeIf(s -> s.getId().equals(servicio.getId()));
			servicios.add(servicio);
		}
		return servicio;
	}

	public void eliminar(long id) {
		servicios.removeIf(s -> s.getId().equals(id));
	}
}
