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

    /**
     * Devuelve el tipo de comprobante a emitir según la condición fiscal.
     * - RESPONSABLE_INSCRIPTO -> "Factura A"
     * - MONOTRIBUTO, EXENTO, MONOTRIBUTO_SOCIAL -> "Factura B"
     * - CONSUMIDOR_FINAL, RESPONSABLE_NO_INSCRIPTO, NO_CATEGORIZADO -> "Factura C"
     */
    public static String tipoComprobantePorCondicion(CondicionFiscal condicion) {
        if (condicion == null) return "Factura C";
        switch (condicion) {
            case RESPONSABLE_INSCRIPTO:
                return "Factura A";
            case MONOTRIBUTO:
            case EXENTO:
            case MONOTRIBUTO_SOCIAL:
                return "Factura B";
            case CONSUMIDOR_FINAL:
            case RESPONSABLE_NO_INSCRIPTO:
            case NO_CATEGORIZADO:
            default:
                return "Factura C";
        }
    }
}
