package com.gestionservicios.services;

import com.gestionservicios.models.Configuracion;
import com.gestionservicios.repositories.ConfiguracionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ConfiguracionService {

    public static final String KEY_IVA_GENERAL = "IVA_GENERAL";

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    /**
     * Devuelve el IVA general como BigDecimal buscando la clave "IVA_GENERAL".
     * Si la configuración no existe o no puede convertirse a BigDecimal, lanza IllegalStateException
     * con un mensaje claro.
     */
    public BigDecimal getIvaGeneral() {
        return configuracionRepository.findByClave(KEY_IVA_GENERAL)
                .map(Configuracion::getValor)
                .map(String::trim)
                .map(this::parseBigDecimalOrThrow)
                .orElseThrow(() -> new IllegalStateException("Configuración '" + KEY_IVA_GENERAL + "' no encontrada"));
    }

    /**
     * Devuelve el IVA general como BigDecimal si existe y es válido, o el valor por defecto provisto.
     */
    public BigDecimal getIvaGeneralOrDefault(BigDecimal defaultValue) {
        return configuracionRepository.findByClave(KEY_IVA_GENERAL)
                .map(Configuracion::getValor)
                .map(String::trim)
                .map(this::parseBigDecimalOrNull)
                .filter(v -> v != null)
                .orElse(defaultValue);
    }

    /** Guarda o actualiza el valor de una configuración por clave. */
    public Configuracion saveOrUpdate(String clave, String valor) {
        Configuracion conf = configuracionRepository.findByClave(clave)
                .orElseGet(() -> new Configuracion(null, clave, valor));
        conf.setValor(valor);
        return configuracionRepository.save(conf);
    }

    /** Guarda el IVA_GENERAL con el valor provisto (BigDecimal). */
    public Configuracion setIvaGeneral(BigDecimal iva) {
        if (iva == null) throw new IllegalArgumentException("IVA no puede ser null");
        // guardamos como número entero o con punto decimal según lo proveído
        return saveOrUpdate(KEY_IVA_GENERAL, iva.toPlainString());
    }

    private BigDecimal parseBigDecimalOrThrow(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Valor de configuración '" + KEY_IVA_GENERAL + "' inválido: '" + s + "'");
        }
    }

    private BigDecimal parseBigDecimalOrNull(String s) {
        try {
            return new BigDecimal(s);
        } catch (Exception ex) {
            return null;
        }
    }
}
