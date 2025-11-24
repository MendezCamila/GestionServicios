package com.gestionservicios.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comprobantes")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación: Muchos comprobantes pertenecen a 1 cliente
    //Fetch LAZY para no cargar el cliente salvo que se necesite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private Cliente cliente;

    // Factura, nota de credito, etc.   
    @Column(name = "tipo_comprobante", nullable = false)
    @NotBlank(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    // Fecha en que se emitió el comprobante
    @Column(name = "fecha_emision", nullable = false)
    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    // Total final del comprobante (puede ser suma de detalles)
    @Column(name = "total", nullable = false)
    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.00", message = "El total debe ser mayor o igual a 0")
    private BigDecimal total;

    // Estado del comprobante (activo, cancelado, etc.)
    @Column(name = "estado", nullable = false)
    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    // Relación 1 comprobante -> muchos detalles
    // cascade = ALL → al guardar comprobante se guardan los detalles automáticamente
     // orphanRemoval = true → si se elimina un detalle desde la lista, se borra de la BD
    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComprobanteDetalle> detalles = new ArrayList<>();
}
