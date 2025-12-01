package com.gestionservicios.models;

import java.util.List;

public class FacturacionPreviewDTO {
    private String periodo; // formatted MM/yyyy
    private int clientesActivos;
    private int clientesConFacturaDelPeriodo;
    private int seGeneraran;
    private java.util.List<ClientePreviewItem> primerosClientes;

    public FacturacionPreviewDTO() {}

    public FacturacionPreviewDTO(String periodo, int clientesActivos, int clientesConFacturaDelPeriodo, int seGeneraran, java.util.List<ClientePreviewItem> primerosClientes) {
        this.periodo = periodo;
        this.clientesActivos = clientesActivos;
        this.clientesConFacturaDelPeriodo = clientesConFacturaDelPeriodo;
        this.seGeneraran = seGeneraran;
        this.primerosClientes = primerosClientes;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public int getClientesActivos() {
        return clientesActivos;
    }

    public void setClientesActivos(int clientesActivos) {
        this.clientesActivos = clientesActivos;
    }

    public int getClientesConFacturaDelPeriodo() {
        return clientesConFacturaDelPeriodo;
    }

    public void setClientesConFacturaDelPeriodo(int clientesConFacturaDelPeriodo) {
        this.clientesConFacturaDelPeriodo = clientesConFacturaDelPeriodo;
    }

    public int getSeGeneraran() {
        return seGeneraran;
    }

    public void setSeGeneraran(int seGeneraran) {
        this.seGeneraran = seGeneraran;
    }

    public java.util.List<ClientePreviewItem> getPrimerosClientes() {
        return primerosClientes;
    }
    public void setPrimerosClientes(java.util.List<ClientePreviewItem> primerosClientes) {
        this.primerosClientes = primerosClientes;
    }
}