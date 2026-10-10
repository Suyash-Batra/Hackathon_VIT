package com.wildguard.conservation.util;

import com.wildguard.conservation.util.ErrorHandler.AppException;

// small helper methods for text
public class TextUtil {

    // null if the text is empty, otherwise the text without extra spaces
    public static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    // for a field that must be filled. name is used in the error message
    public static String required(String value, String name, int maxLength) {
        String text = clean(value);
        if (text == null) {
            throw new AppException(400, name + " is required.");
        }
        return shortEnough(text, name, maxLength);
    }

    // for a field that can be empty but must not be too long for the column
    public static String optional(String value, String name, int maxLength) {
        String text = clean(value);
        if (text == null) {
            return null;
        }
        return shortEnough(text, name, maxLength);
    }

    private static String shortEnough(String text, String name, int maxLength) {
        if (text.length() > maxLength) {
            throw new AppException(400, name + " can have at most " + maxLength + " characters.");
        }
        return text;
    }

    // UNDER_TREATMENT -> "Under treatment"
    public static String label(Enum<?> value) {
        String text = value.name().replace('_', ' ').toLowerCase();
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
