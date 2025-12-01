package com.gestionservicios.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facturacion_masiva")
public class FacturacionMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Column(name = "cantidad_facturas")
    private Integer cantidadFacturas;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "total_facturado")
    private BigDecimal totalFacturado;

    @Column(name = "estado")
    private String estado;

    @OneToMany(mappedBy = "facturacionMasiva")
    private List<Comprobante> comprobantes = new ArrayList<>();
}
