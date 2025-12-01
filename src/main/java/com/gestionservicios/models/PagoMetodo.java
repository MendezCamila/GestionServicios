package com.gestionservicios.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pago_metodos")
public class PagoMetodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @Column(name = "metodo", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetodoPago metodo;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

}
