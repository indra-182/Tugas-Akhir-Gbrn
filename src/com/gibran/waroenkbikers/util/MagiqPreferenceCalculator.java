package com.gibran.waroenkbikers.util;

public final class MagiqPreferenceCalculator {
    private MagiqPreferenceCalculator() {
    }

    public static double hitungNilai(double[] nilaiNormalisasi, double[] bobotKriteria) {
        if (nilaiNormalisasi == null || bobotKriteria == null
                || nilaiNormalisasi.length != bobotKriteria.length) {
            throw new IllegalArgumentException("Matriks normalisasi dan bobot ROC tidak sesuai.");
        }

        double nilaiPreferensi = 0.0;
        for (int i = 0; i < bobotKriteria.length; i++) {
            nilaiPreferensi += nilaiNormalisasi[i] * bobotKriteria[i];
        }
        return nilaiPreferensi;
    }

    public static double[] hitungSemua(double[][] matriksNormalisasi, double[] bobotKriteria) {
        if (matriksNormalisasi == null) {
            throw new IllegalArgumentException("Matriks normalisasi belum tersedia.");
        }

        double[] nilaiPreferensi = new double[matriksNormalisasi.length];
        for (int i = 0; i < matriksNormalisasi.length; i++) {
            nilaiPreferensi[i] = hitungNilai(matriksNormalisasi[i], bobotKriteria);
        }
        return nilaiPreferensi;
    }
}
