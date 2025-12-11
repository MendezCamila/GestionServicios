package com.gestionservicios.services;

import com.gestionservicios.models.Servicio;
import com.gestionservicios.repositories.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ServicioService {

	private final ServicioRepository servicioRepository;

	public ServicioService(ServicioRepository servicioRepository) {
		this.servicioRepository = servicioRepository;
	}

	public List<Servicio> listarServicios() {
		return servicioRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}

    public Page<Servicio> listarServicios(Pageable pageable) {
		if (pageable == null) pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			return servicioRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id")));
		}
		return servicioRepository.findAll(pageable);
    }

	public List<Servicio> buscarPorNombre(String q) {
		if (q == null || q.isBlank()) return listarServicios();
		return servicioRepository.findByNombreContainingIgnoreCase(q);
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
