package com.wildguard.conservation.service;

// small helper methods for text, used by the services
public class TextUtil {

    // returns null if the text is empty, otherwise the text without extra spaces
    public static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    // makes enum names readable: UNDER_TREATMENT -> "Under treatment"
    public static String label(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String text = name.replace('_', ' ').toLowerCase();
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String label(Enum<?> value) {
        if (value == null) {
            return "";
        }
        return label(value.name());
    }
}
