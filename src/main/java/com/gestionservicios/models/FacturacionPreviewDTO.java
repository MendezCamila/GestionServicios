package com.gestionservicios.models;

public class FacturacionPreviewDTO {
    private int clientesActivos;
    private int clientesConFacturaDelPeriodo;
    private int seGeneraran;

    public FacturacionPreviewDTO() {}

    public FacturacionPreviewDTO(int clientesActivos, int clientesConFacturaDelPeriodo, int seGeneraran) {
        this.clientesActivos = clientesActivos;
        this.clientesConFacturaDelPeriodo = clientesConFacturaDelPeriodo;
        this.seGeneraran = seGeneraran;
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
}