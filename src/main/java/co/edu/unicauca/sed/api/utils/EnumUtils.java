package co.edu.unicauca.sed.api.utils;

import java.util.*;
import java.util.stream.Collectors;

public class EnumUtils {

    public interface ValorEnum {
        String getValor();
    }

    public static List<Map<String, String>> getSelectOptions(Class<? extends Enum<? extends ValorEnum>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> {
                    ValorEnum valorEnum = (ValorEnum) e;
                    Map<String, String> map = new HashMap<>();
                    map.put("codigo", ((Enum<?>) e).name());
                    map.put("nombre", valorEnum.getValor());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public static List<Map<String, String>> mapearValor(List<String> listaBD,
            Class<? extends Enum<? extends EnumUtils.ValorEnum>> enumClass) {

        Set<String> valoresUnicos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // Agrega valores desde la base de datos
        if (listaBD != null) {
            valoresUnicos.addAll(listaBD);
        }

        // Agrega valores desde el enum
        Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .forEach(valoresUnicos::add);

        // Convertir a List<Map<String, String>>
        return valoresUnicos.stream()
                .map(valor -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("codigo", valor);
                    map.put("nombre", valor);
                    return map;
                })
                .collect(Collectors.toList());
    }
}
