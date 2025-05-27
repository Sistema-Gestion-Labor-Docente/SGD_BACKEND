package co.edu.unicauca.sed.api.utils;

import org.springframework.stereotype.Component;

@Component
public class StringUtils {

    private StringUtils() {
    }

    public String safeToUpperCase(String value) {
        return (value != null && !value.isBlank()) ? value.trim().toUpperCase() : value;
    }

    /**
     * Formatea una cadena reemplazando espacios múltiples con "_" y eliminando guiones "-".
     *
     * @param texto Cadena de entrada.
     * @return Cadena formateada.
     */
    public static String formatearCadena(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        return texto.replace("-", "").replaceAll("\\s+", "_");
    }
}
