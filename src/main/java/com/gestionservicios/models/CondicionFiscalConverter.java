package com.gestionservicios.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = false)
public class CondicionFiscalConverter implements AttributeConverter<CondicionFiscal, String> {

    private static final Logger logger = LoggerFactory.getLogger(CondicionFiscalConverter.class);

    @Override
    public String convertToDatabaseColumn(CondicionFiscal attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CondicionFiscal convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return CondicionFiscal.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Valor desconocido en la BD: mapear a NO_CATEGORIZADO y loggear para diagnóstico
            logger.warn("Valor desconocido de condicion_fiscal en BD: '{}', mapeando a NO_CATEGORIZADO", dbData);
            return CondicionFiscal.NO_CATEGORIZADO;
        }
    }
}
