package com.gibran.waroenkbikers.util;

import java.text.DecimalFormat;

public final class NumberUtil {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.####");

    private NumberUtil() {
    }

    public static double parseDouble(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " wajib diisi.");
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " harus berupa angka.");
        }
    }

    public static String format(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public static double parsePositiveDouble(String value, String fieldName) {
        double result = parseDouble(value, fieldName);
        if (result <= 0) {
            throw new IllegalArgumentException(fieldName + " harus lebih dari 0.");
        }
        return result;
    }

    public static double parseNonNegativeDouble(String value, String fieldName) {
        double result = parseDouble(value, fieldName);
        if (result < 0) {
            throw new IllegalArgumentException(fieldName + " tidak boleh kurang dari 0.");
        }
        return result;
    }
}
