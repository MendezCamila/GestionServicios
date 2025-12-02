package com.gestionservicios.util;

import com.gestionservicios.models.CondicionFiscal;

public final class TaxRules {

    private TaxRules() {}

    /**
     * Devuelve true si, según la condición fiscal, corresponde aplicar IVA.
     * Actualmente sólo RESPONSABLE_INSCRIPTO aplica IVA.
     */
    public static boolean appliesIva(CondicionFiscal condicion) {
        return condicion == CondicionFiscal.RESPONSABLE_INSCRIPTO;
    }
}
