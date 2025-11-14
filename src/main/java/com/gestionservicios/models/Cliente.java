package com.gestionservicios.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class Cliente {

    private Long id;
    private String razonSocial;
    private String cuit;
    private String condicionFiscal;
    private String email;
    private String telefono;
    private String direccion;
    private String estado; // activo, inactivo
}
