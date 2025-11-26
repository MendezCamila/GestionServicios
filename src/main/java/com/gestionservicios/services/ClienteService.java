package com.gestionservicios.services;

import com.gestionservicios.models.Cliente;
import com.gestionservicios.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Listar todos los clientes
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
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
        return clienteRepository.save(cliente);
    }

    // Eliminar cliente
    public void eliminar(long id) {
        clienteRepository.deleteById(id);
    }

}
