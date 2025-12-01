package com.gestionservicios.models;

import java.util.List;

public class ClientePreviewItem {
    private Long clienteId;
    private String clienteNombre;
    private List<Long> contratoIds;

    public ClientePreviewItem() {}

    public ClientePreviewItem(Long clienteId, String clienteNombre, List<Long> contratoIds) {
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.contratoIds = contratoIds;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public List<Long> getContratoIds() {
        return contratoIds;
    }

    public void setContratoIds(List<Long> contratoIds) {
        this.contratoIds = contratoIds;
    }
}