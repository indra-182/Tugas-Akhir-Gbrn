package com.gibran.waroenkbikers.util;

public final class NormalisasiNilaiCalculator {
    private static final double SKALA_MAKSIMUM = 100.0;

    private NormalisasiNilaiCalculator() {
    }

    public static double hitungNilai(double nilai, double[] nilaiKolom) {
        if (nilaiKolom == null || nilaiKolom.length == 0) {
            throw new IllegalArgumentException("Data nilai untuk normalisasi belum tersedia.");
        }

        return nilai == 0.0 ? 0.0 : nilai / SKALA_MAKSIMUM;
    }
}
