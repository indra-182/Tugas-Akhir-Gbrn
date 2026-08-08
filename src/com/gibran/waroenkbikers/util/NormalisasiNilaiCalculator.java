package com.gibran.waroenkbikers.util;

public final class NormalisasiNilaiCalculator {
    private NormalisasiNilaiCalculator() {
    }

    public static double hitungNilai(double nilai, double[] nilaiKolom) {
        if (nilaiKolom == null || nilaiKolom.length == 0) {
            throw new IllegalArgumentException("Data nilai untuk normalisasi belum tersedia.");
        }

        double maksimum = nilaiMaksimum(nilaiKolom);
        return maksimum == 0.0 ? 1.0 : nilai / maksimum;
    }

    private static double nilaiMaksimum(double[] nilaiKolom) {
        double maksimum = nilaiKolom[0];
        for (int i = 1; i < nilaiKolom.length; i++) {
            maksimum = Math.max(maksimum, nilaiKolom[i]);
        }
        return maksimum;
    }
}
