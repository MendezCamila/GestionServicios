package com.gestionservicios.util;

import com.gestionservicios.validation.CUIT;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CuitValidator implements ConstraintValidator<CUIT, String> {

    public CuitValidator() {}

    // Elimina guiones, espacios y cualquier caracter no numérico.
    public static String normalize(String cuit) {
        if (cuit == null) return null;
        String cleaned = cuit.replaceAll("\\D", "");
        return cleaned.isEmpty() ? null : cleaned;
    }

    // Valida longitud (11) y dígito verificador según algoritmo argentino (módulo 11).
    public static boolean isValid(String cuit) {
        String normalized = normalize(cuit);
        if (normalized == null) return false;
        if (normalized.length() != 11) return false;

        int[] weights = {5,4,3,2,7,6,5,4,3,2};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            int digit = Character.digit(normalized.charAt(i), 10);
            sum += digit * weights[i];
        }

        int mod = sum % 11;
        int check = 11 - mod;
        if (check == 11) check = 0;
        else if (check == 10) check = 9;

        int lastDigit = Character.digit(normalized.charAt(10), 10);
        return check == lastDigit;
    }

    @Override
    public void initialize(CUIT constraintAnnotation) { }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Cuando el valor es null o vacío, no consideramos esto un fallo aquí:
        // la obligatoriedad (según CondicionFiscal) se valida en el service.
        if (value == null) return true;
        String normalized = normalize(value);
        if (normalized == null) return true; // vacío -> no validar checksum
        return isValid(value);
    }
}
