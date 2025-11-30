package com.gestionservicios.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pago_detalles")
public class PagoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    @NotNull
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id", nullable = false)
    @NotNull
    private Comprobante comprobante;

    @Column(name = "monto_aplicado", nullable = false)
    private BigDecimal montoAplicado;

    @Column(name = "saldo_anterior", nullable = false)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false)
    private BigDecimal saldoPosterior;

}
