package com.gestionservicios.services;

import com.gestionservicios.models.Cliente;
import com.gestionservicios.models.CondicionFiscal;
import com.gestionservicios.util.CuitValidator;
import com.gestionservicios.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Listar todos los clientes
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Cliente> listarClientes(Pageable pageable) {
        if (pageable == null) pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
            return clienteRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id")));
        }
        return clienteRepository.findAll(pageable);
    }

    // Obtener por id
    public Cliente obtenerPorId(long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public List<Cliente> buscarPorNombreOCuit(String q) {
        if (q == null || q.isBlank()) return listarClientes();
        // intentar buscar por CUIT exacto primero
        var byCuit = clienteRepository.findByCuit(q);
        if (byCuit.isPresent()) return List.of(byCuit.get());
        // buscar por razón social parcial (ignore case)
        return clienteRepository.findByRazonSocialContainingIgnoreCase(q);
    }

    // Guardar o actualizar cliente
    public Cliente guardar(Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("Cliente no puede ser null");

        CondicionFiscal condicion = cliente.getCondicionFiscal();
        String cuitRaw = cliente.getCuit();
        String cuit = CuitValidator.normalize(cuitRaw);

        if (condicion == CondicionFiscal.RESPONSABLE_INSCRIPTO
                || condicion == CondicionFiscal.MONOTRIBUTO
                || condicion == CondicionFiscal.EXENTO) {
            if (cuit == null || cuit.isBlank()) {
                throw new IllegalArgumentException("CUIT es obligatorio para la condición fiscal: " + condicion);
            }
            if (!CuitValidator.isValid(cuit)) {
                throw new IllegalArgumentException("CUIT inválido para la condición fiscal: " + condicion);
            }
        } else if (condicion == CondicionFiscal.CONSUMIDOR_FINAL) {
            // para CONSUMIDOR_FINAL el CUIT puede ser null o vacío -> no validar
        }

        return clienteRepository.save(cliente);
    }

    // Eliminar cliente
    public void eliminar(long id) {
        clienteRepository.deleteById(id);
    }

}
