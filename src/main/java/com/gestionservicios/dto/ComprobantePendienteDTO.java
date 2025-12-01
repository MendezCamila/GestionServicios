package com.gestionservicios.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ComprobantePendienteDTO {
    private Long id;
    private LocalDate fechaEmision;
    private BigDecimal total;
    private BigDecimal saldoPendiente;
    private String estado;

    public ComprobantePendienteDTO() {}

    public ComprobantePendienteDTO(Long id, LocalDate fechaEmision, BigDecimal total, BigDecimal saldoPendiente, String estado) {
        this.id = id;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
