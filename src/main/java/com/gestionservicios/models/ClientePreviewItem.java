package com.gestionservicios.models;

import java.util.List;

public class ClientePreviewItem {
    private Long clienteId;
    private String clienteNombre;
    private List<Long> contratoIds;
    private String condicionFiscal;
    private boolean applyIva;
    private String tipoComprobante;

    public ClientePreviewItem() {}

    public ClientePreviewItem(Long clienteId, String clienteNombre, List<Long> contratoIds) {
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.contratoIds = contratoIds;
        this.condicionFiscal = null;
        this.applyIva = false;
        this.tipoComprobante = null;
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

    public String getCondicionFiscal() {
        return condicionFiscal;
    }

    public void setCondicionFiscal(String condicionFiscal) {
        this.condicionFiscal = condicionFiscal;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public boolean isApplyIva() {
        return applyIva;
    }

    public void setApplyIva(boolean applyIva) {
        this.applyIva = applyIva;
    }
}