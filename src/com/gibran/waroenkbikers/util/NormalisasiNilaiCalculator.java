package com.gibran.waroenkbikers.util;

public final class NormalisasiNilaiCalculator {
    private NormalisasiNilaiCalculator() {
    }

    public static double hitungNilai(double nilai, double[] nilaiKolom, boolean cost) {
        if (nilaiKolom == null || nilaiKolom.length == 0) {
            throw new IllegalArgumentException("Data nilai untuk normalisasi belum tersedia.");
        }

        double pembanding = cost ? nilaiMinimum(nilaiKolom) : nilaiMaksimum(nilaiKolom);
        if (cost) {
            if (pembanding == 0.0) {
                return nilai == 0.0 ? 1.0 : 0.0;
            }
            return nilai == 0.0 ? 1.0 : pembanding / nilai;
        }
        return pembanding == 0.0 ? 1.0 : nilai / pembanding;
    }

    private static double nilaiMaksimum(double[] nilaiKolom) {
        double maksimum = nilaiKolom[0];
        for (int i = 1; i < nilaiKolom.length; i++) {
            maksimum = Math.max(maksimum, nilaiKolom[i]);
        }
        return maksimum;
    }

    private static double nilaiMinimum(double[] nilaiKolom) {
        double minimum = nilaiKolom[0];
        for (int i = 1; i < nilaiKolom.length; i++) {
            minimum = Math.min(minimum, nilaiKolom[i]);
        }
        return minimum;
    }
}
