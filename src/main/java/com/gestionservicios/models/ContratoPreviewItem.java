package com.gestionservicios.models;

public class ContratoPreviewItem {
    private Long contratoId;
    private String clienteNombre;

    public ContratoPreviewItem() {}

    public ContratoPreviewItem(Long contratoId, String clienteNombre) {
        this.contratoId = contratoId;
        this.clienteNombre = clienteNombre;
    }

    public Long getContratoId() {
        return contratoId;
    }

    public void setContratoId(Long contratoId) {
        this.contratoId = contratoId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
}