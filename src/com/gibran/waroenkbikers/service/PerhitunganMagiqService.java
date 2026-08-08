package com.gibran.waroenkbikers.service;

import com.gibran.waroenkbikers.dao.BaristaDao;
import com.gibran.waroenkbikers.dao.HasilRankingDao;
import com.gibran.waroenkbikers.dao.KriteriaDao;
import com.gibran.waroenkbikers.dao.PenilaianDao;
import com.gibran.waroenkbikers.model.Barista;
import com.gibran.waroenkbikers.model.HasilRanking;
import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.util.MagiqPreferenceCalculator;
import com.gibran.waroenkbikers.util.NormalisasiNilaiCalculator;
import com.gibran.waroenkbikers.util.PrioritasKriteriaValidator;
import com.gibran.waroenkbikers.util.RocWeightCalculator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PerhitunganMagiqService {
    private final BaristaDao baristaDao = new BaristaDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();

    public List<HasilRanking> hitungDanSimpan() throws SQLException {
        List<HasilRanking> daftarHasilRanking = hitungDetailDanSimpan().getDaftarHasilRanking();
        return daftarHasilRanking;
    }

    public PerhitunganDetail hitungDetailDanSimpan() throws SQLException {
        PerhitunganDetail detail = hitungDetail();
        List<HasilRanking> daftarHasilRanking = detail.getDaftarHasilRanking();
        hasilRankingDao.gantiSemua(daftarHasilRanking);
        return detail;
    }

    public List<HasilRanking> hitung() throws SQLException {
        return hitungDetail().getDaftarHasilRanking();
    }

    public PerhitunganDetail hitungDetail() throws SQLException {
        List<Barista> daftarBarista = baristaDao.ambilSemua();
        List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
        Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks();

        validasiInput(daftarBarista, daftarKriteria, matriksPenilaian);

        double[][] matriksKeputusan = buatMatriksKeputusan(daftarBarista, daftarKriteria, matriksPenilaian);
        double[] bobotKriteria = hitungBobotKriteria(daftarKriteria);
        double[][] matriksNormalisasi = hitungMatriksNormalisasi(daftarBarista, daftarKriteria, matriksKeputusan);
        double[] nilaiPreferensi = MagiqPreferenceCalculator.hitungSemua(matriksNormalisasi, bobotKriteria);
        List<HasilRanking> daftarHasilRanking = buatHasilRanking(daftarBarista, nilaiPreferensi);
        List<Object[]> daftarUrutanKriteria = buatDataUrutanKriteria(daftarKriteria, bobotKriteria);

        Collections.sort(daftarHasilRanking, (HasilRanking hasilPertama, HasilRanking hasilKedua) -> {
            int hasilBanding = Double.compare(hasilKedua.getNilaiMagiq(), hasilPertama.getNilaiMagiq());
            if (hasilBanding != 0) {
                return hasilBanding;
            }
            return hasilPertama.getNamaBarista().compareToIgnoreCase(hasilKedua.getNamaBarista());
        });

        for (int i = 0; i < daftarHasilRanking.size(); i++) {
            daftarHasilRanking.get(i).setPeringkat(i + 1);
        }

        return new PerhitunganDetail(daftarBarista, daftarKriteria, matriksKeputusan,
                bobotKriteria, daftarUrutanKriteria, matriksNormalisasi,
                nilaiPreferensi, daftarHasilRanking);
    }

    public List<Object[]> ambilLaporanNormalisasi() throws SQLException {
        List<Barista> daftarBarista = baristaDao.ambilSemua();
        List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
        Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks();

        validasiInput(daftarBarista, daftarKriteria, matriksPenilaian);

        double[][] matriksKeputusan = buatMatriksKeputusan(daftarBarista, daftarKriteria, matriksPenilaian);
        double[][] matriksNormalisasi = hitungMatriksNormalisasi(daftarBarista, daftarKriteria, matriksKeputusan);
        List<Object[]> dataSkor = new ArrayList<>();

        for (int j = 0; j < daftarKriteria.size(); j++) {
            for (int i = 0; i < daftarBarista.size(); i++) {
                Barista barista = daftarBarista.get(i);
                Kriteria kriteria = daftarKriteria.get(j);
                dataSkor.add(new Object[]{
                    barista.getKodeBarista(),
                    barista.getNama(),
                    kriteria.getKode(),
                    kriteria.getNama(),
                    matriksNormalisasi[i][j]
                });
            }
        }
        return dataSkor;
    }

    private double[][] buatMatriksKeputusan(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        double[][] matriks = new double[daftarBarista.size()][daftarKriteria.size()];

        for (int i = 0; i < daftarBarista.size(); i++) {
            Barista barista = daftarBarista.get(i);
            Map<Integer, Double> nilaiBarista = matriksPenilaian.get(barista.getId());
            for (int j = 0; j < daftarKriteria.size(); j++) {
                Kriteria kriteria = daftarKriteria.get(j);
                matriks[i][j] = nilaiBarista.get(kriteria.getId());
            }
        }

        return matriks;
    }

    private double[] hitungBobotKriteria(List<Kriteria> daftarKriteria) {
        List<UrutanKriteria> daftarUrutan = buatDaftarUrutanKriteria(daftarKriteria);
        double[] nilaiRoc = RocWeightCalculator.hitungSemua(daftarKriteria.size());
        double[] bobotKriteria = new double[daftarKriteria.size()];
        for (int urutan = 0; urutan < daftarUrutan.size(); urutan++) {
            bobotKriteria[daftarUrutan.get(urutan).indeks] = nilaiRoc[urutan];
        }
        return bobotKriteria;
    }

    private List<UrutanKriteria> buatDaftarUrutanKriteria(List<Kriteria> daftarKriteria) {
        final List<UrutanKriteria> daftarUrutan = new ArrayList<>();
        for (int i = 0; i < daftarKriteria.size(); i++) {
            daftarUrutan.add(new UrutanKriteria(i, daftarKriteria.get(i)));
        }

        Collections.sort(daftarUrutan, (UrutanKriteria dataPertama, UrutanKriteria dataKedua) -> {
            int hasilBanding = Integer.compare(dataPertama.kriteria.getUrutanPrioritas(),
                    dataKedua.kriteria.getUrutanPrioritas());
            if (hasilBanding != 0) {
                return hasilBanding;
            }
            return dataPertama.kriteria.getKode().compareToIgnoreCase(dataKedua.kriteria.getKode());
        });

        return daftarUrutan;
    }

    private List<Object[]> buatDataUrutanKriteria(List<Kriteria> daftarKriteria, double[] bobotKriteria) {
        List<UrutanKriteria> daftarUrutan = buatDaftarUrutanKriteria(daftarKriteria);
        List<Object[]> dataUrutan = new ArrayList<>();
        for (int i = 0; i < daftarUrutan.size(); i++) {
            Kriteria kriteria = daftarUrutan.get(i).kriteria;
            dataUrutan.add(new Object[]{
                kriteria.getUrutanPrioritas(),
                kriteria.getKode(),
                kriteria.getNama(),
                kriteria.getBobot(),
                bobotKriteria[daftarUrutan.get(i).indeks]
            });
        }
        return dataUrutan;
    }

    private double[][] hitungMatriksNormalisasi(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
            double[][] matriksKeputusan) {
        double[][] matriksNormalisasi = new double[daftarBarista.size()][daftarKriteria.size()];
        for (int j = 0; j < daftarKriteria.size(); j++) {
            double[] nilaiKolom = new double[daftarBarista.size()];
            for (int i = 0; i < daftarBarista.size(); i++) {
                nilaiKolom[i] = matriksKeputusan[i][j];
            }

            for (int i = 0; i < daftarBarista.size(); i++) {
                matriksNormalisasi[i][j] = NormalisasiNilaiCalculator.hitungNilai(
                        matriksKeputusan[i][j], nilaiKolom);
            }
        }
        return matriksNormalisasi;
    }

    private List<HasilRanking> buatHasilRanking(List<Barista> daftarBarista, double[] nilaiPreferensi) {
        List<HasilRanking> daftarHasilRanking = new ArrayList<>();

        for (int i = 0; i < daftarBarista.size(); i++) {
            Barista barista = daftarBarista.get(i);
            HasilRanking hasilRanking = new HasilRanking();
            hasilRanking.setIdBarista(barista.getId());
            hasilRanking.setKodeBarista(barista.getKodeBarista());
            hasilRanking.setNamaBarista(barista.getNama());
            hasilRanking.setNilaiMagiq(nilaiPreferensi[i]);
            daftarHasilRanking.add(hasilRanking);
        }

        return daftarHasilRanking;
    }

    private void validasiInput(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        if (daftarBarista.isEmpty()) {
            throw new IllegalArgumentException("Data barista aktif belum tersedia.");
        }
        if (daftarKriteria.isEmpty()) {
            throw new IllegalArgumentException("Data kriteria belum tersedia.");
        }
        validasiPrioritas(daftarKriteria);

        daftarKriteria.forEach((kriteria) -> {
            if (kriteria.getBobot() <= 0) {
                throw new IllegalArgumentException("Bobot kriteria " + kriteria.getKode() + " harus lebih dari 0.");
            }
        });

        daftarBarista.stream().map((Barista barista) -> {
            if (!matriksPenilaian.containsKey(barista.getId())) {
                throw new IllegalArgumentException("Nilai penilaian untuk " + barista.getNama() + " belum lengkap.");
            }
            return barista;
        }).forEachOrdered((Barista barista) -> {
            Map<Integer, Double> nilaiBarista = matriksPenilaian.get(barista.getId());
            daftarKriteria.stream().filter((kriteria) -> (!nilaiBarista.containsKey(kriteria.getId()))).forEachOrdered((kriteria) -> {
                throw new IllegalArgumentException("Nilai " + kriteria.getKode()
                        + " untuk " + barista.getNama() + " belum diisi.");
            });
        });
    }

    private void validasiPrioritas(List<Kriteria> daftarKriteria) {
        int[] prioritas = new int[daftarKriteria.size()];
        for (int i = 0; i < daftarKriteria.size(); i++) {
            prioritas[i] = daftarKriteria.get(i).getUrutanPrioritas();
            if (prioritas[i] < 1 || prioritas[i] > daftarKriteria.size()) {
                throw new IllegalArgumentException("Urutan prioritas harus antara 1 dan "
                        + daftarKriteria.size() + ".");
            }
        }
        PrioritasKriteriaValidator.validasiUnik(prioritas);
    }

    public static class PerhitunganDetail {
        private final List<Barista> daftarBarista;
        private final List<Kriteria> daftarKriteria;
        private final double[][] matriksKeputusan;
        private final double[] bobotKriteria;
        private final List<Object[]> daftarUrutanKriteria;
        private final double[][] matriksNormalisasi;
        private final double[] nilaiPreferensi;
        private final List<HasilRanking> daftarHasilRanking;

        private PerhitunganDetail(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
                double[][] matriksKeputusan, double[] bobotKriteria, List<Object[]> daftarUrutanKriteria,
                double[][] matriksNormalisasi, double[] nilaiPreferensi,
                List<HasilRanking> daftarHasilRanking) {
            this.daftarBarista = daftarBarista;
            this.daftarKriteria = daftarKriteria;
            this.matriksKeputusan = matriksKeputusan;
            this.bobotKriteria = bobotKriteria;
            this.daftarUrutanKriteria = daftarUrutanKriteria;
            this.matriksNormalisasi = matriksNormalisasi;
            this.nilaiPreferensi = nilaiPreferensi;
            this.daftarHasilRanking = daftarHasilRanking;
        }

        public List<Barista> getDaftarBarista() {
            return daftarBarista;
        }

        public List<Kriteria> getDaftarKriteria() {
            return daftarKriteria;
        }

        public double[][] getMatriksKeputusan() {
            return matriksKeputusan;
        }

        public double[] getBobotKriteria() {
            return bobotKriteria;
        }

        public List<Object[]> getDaftarUrutanKriteria() {
            return daftarUrutanKriteria;
        }

        public double[][] getMatriksNormalisasi() {
            return matriksNormalisasi;
        }

        public double[] getNilaiPreferensi() {
            return nilaiPreferensi;
        }

        public List<HasilRanking> getDaftarHasilRanking() {
            return daftarHasilRanking;
        }
    }

    private static class UrutanKriteria {
        private final int indeks;
        private final Kriteria kriteria;

        private UrutanKriteria(int indeks, Kriteria kriteria) {
            this.indeks = indeks;
            this.kriteria = kriteria;
        }
    }

}
