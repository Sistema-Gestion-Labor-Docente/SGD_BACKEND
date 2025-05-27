package co.edu.unicauca.sed.api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidades para operaciones matemáticas comunes.
 */
public class MathUtils {

  private MathUtils() {
  }

  /**
   * Redondea un número a un número específico de decimales.
   *
   * @param valor   Número a redondear.
   * @param digitos Número de decimales.
   * @return Número redondeado.
   */
  public static BigDecimal redondearDecimal(double valor, int digitos) {
    return BigDecimal.valueOf(valor).setScale(digitos, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el porcentaje completado dado un total y una cantidad completada.
   *
   * @param total       El total de elementos.
   * @param completados La cantidad de elementos completados.
   * @return El porcentaje completado redondeado a 2 decimales.
   */
  public static float calcularPorcentajeCompletado(int total, int completados) {
    if (total <= 0) {
      return 0.0f;
    }
    float porcentaje = (completados / (float) total) * 100;
    return Math.round(porcentaje * 100.0f) / 100.0f;
  }
}
