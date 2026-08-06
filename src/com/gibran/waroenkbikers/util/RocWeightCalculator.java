package com.gibran.waroenkbikers.util;

public final class RocWeightCalculator {
    private RocWeightCalculator() {
    }

    public static double hitungBobot(int urutanPrioritas, int jumlahKriteria) {
        validasiJumlahKriteria(jumlahKriteria);
        if (urutanPrioritas < 1 || urutanPrioritas > jumlahKriteria) {
            throw new IllegalArgumentException("Urutan prioritas harus antara 1 dan jumlah kriteria.");
        }

        double total = 0.0;
        for (int pembagi = urutanPrioritas; pembagi <= jumlahKriteria; pembagi++) {
            total += 1.0 / pembagi;
        }
        return total / jumlahKriteria;
    }

    public static double[] hitungSemua(int jumlahKriteria) {
        validasiJumlahKriteria(jumlahKriteria);
        double[] bobot = new double[jumlahKriteria];
        for (int urutan = 1; urutan <= jumlahKriteria; urutan++) {
            bobot[urutan - 1] = hitungBobot(urutan, jumlahKriteria);
        }
        return bobot;
    }

    private static void validasiJumlahKriteria(int jumlahKriteria) {
        if (jumlahKriteria <= 0) {
            throw new IllegalArgumentException("Jumlah kriteria harus lebih dari 0.");
        }
    }
}
