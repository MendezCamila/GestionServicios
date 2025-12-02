package com.gestionservicios.models;

import jakarta.persistence.Column;
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
    @Size(max = 255, message = "La razón social no puede superar los 255 caracteres")
    private String razonSocial;

    @Column(name = "cuit", nullable = false, unique = true)
    @NotBlank(message = "El CUIT es obligatorio")
    @com.gestionservicios.validation.CUIT
    private String cuit;

    @Column(name = "condicion_fiscal")
    private String condicionFiscal;

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
