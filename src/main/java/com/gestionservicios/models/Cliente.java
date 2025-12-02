package com.gestionservicios.models;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false)
    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 2, message = "La razón social debe tener al menos 2 caracteres")
    @Size(max = 255, message = "La razón social no puede superar los 255 caracteres")
    private String razonSocial;

    @Column(name = "cuit", nullable = true, unique = true)
    // El campo puede estar vacío para ciertos tipos de cliente (p.ej. CONSUMIDOR_FINAL).
    // Permitimos cadena vacía o exactamente 11 dígitos. La obligatoriedad según condición
    // fiscal se valida en el service y lanzará IllegalArgumentException si corresponde.
    @Pattern(regexp = "(^$|\\d{11})", message = "El CUIT debe tener exactamente 11 dígitos")
    @com.gestionservicios.validation.CUIT
    private String cuit;

    @Column(name = "condicion_fiscal")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "La condición fiscal es obligatoria")
    private CondicionFiscal condicionFiscal;

    @Column(name = "email")
    @Email(message = "El email debe ser válido")
    private String email;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "estado") // activo, inactivo
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
