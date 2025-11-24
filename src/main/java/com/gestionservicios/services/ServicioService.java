package com.gestionservicios.services;

import com.gestionservicios.models.Servicio;
import com.gestionservicios.repositories.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

	private final ServicioRepository servicioRepository;

	public ServicioService(ServicioRepository servicioRepository) {
		this.servicioRepository = servicioRepository;
	}

	public List<Servicio> listarServicios() {
		return servicioRepository.findAll();
	}

	public Servicio obtenerPorId(long id) {
		return servicioRepository.findById(id).orElse(null);
	}

	public Servicio guardar(Servicio servicio) {
		return servicioRepository.save(servicio);
	}

	public void eliminar(long id) {
		servicioRepository.deleteById(id);
	}
}
