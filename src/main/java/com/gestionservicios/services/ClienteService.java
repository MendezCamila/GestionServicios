package com.gestionservicios.services;

import com.gestionservicios.models.Cliente;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
public class ClienteService {
    private List<Cliente> clientes = new ArrayList<>();
    private Long contador = 1L;

    //listar todos los clientes
    public List<Cliente> listarClientes() {
        return clientes;
    }

    //obtener por id
    public Cliente obtenerPorId(long id) {
        return clientes.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
                
    }

    //guardar cliente
    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(contador++);
            clientes.add(cliente);
        } else {
            clientes.removeIf(c -> c.getId().equals(cliente.getId()));
            clientes.add(cliente);
        }
        return cliente;
    }

    //eliminar cliente 
    public void eliminar(long id) {
        clientes.removeIf(c -> c.getId().equals(id));
    }


}
