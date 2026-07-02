package com.gibran.waroenkbikers.service;

import com.gibran.waroenkbikers.dao.BaristaDao;
import com.gibran.waroenkbikers.dao.HasilRankingDao;
import com.gibran.waroenkbikers.dao.KriteriaDao;
import com.gibran.waroenkbikers.dao.PenilaianDao;
import com.gibran.waroenkbikers.model.Barista;
import com.gibran.waroenkbikers.model.HasilRanking;
import com.gibran.waroenkbikers.model.Kriteria;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class PerhitunganMagiqService {
    private static boolean perhitunganSudahDiproses = false;

    private final BaristaDao baristaDao = new BaristaDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();

    public static boolean apakahPerhitunganSudahDiproses() {
        return perhitunganSudahDiproses;
    }

    public List<HasilRanking> hitungDanSimpan() throws SQLException {
        List<HasilRanking> daftarHasilRanking = hitungDetailDanSimpan().getDaftarHasilRanking();
        return daftarHasilRanking;
    }

    public PerhitunganDetail hitungDetailDanSimpan() throws SQLException {
        PerhitunganDetail detail = hitungDetail();
        List<HasilRanking> daftarHasilRanking = detail.getDaftarHasilRanking();
        hasilRankingDao.gantiSemua(daftarHasilRanking);
        perhitunganSudahDiproses = true;
        return detail;
    }

    public List<HasilRanking> hitung() throws SQLException {
        return hitungDetail().getDaftarHasilRanking();
    }

    public PerhitunganDetail hitungDetail() throws SQLException {
        List<Barista> daftarBarista = baristaDao.ambilAktif();
        List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
        Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks();

        validasiInput(daftarBarista, daftarKriteria, matriksPenilaian);

        double[][] matriksKeputusan = buatMatriksKeputusan(daftarBarista, daftarKriteria, matriksPenilaian);
        double[] bobotKriteria = hitungBobotKriteria(daftarKriteria);
        double[][] skorLokal = hitungSkorLokalBarista(daftarBarista, daftarKriteria, matriksKeputusan);
        List<HasilRanking> daftarHasilRanking = buatHasilRanking(daftarBarista, skorLokal, bobotKriteria);
        List<Object[]> daftarUrutanKriteria = buatDataUrutanKriteria(daftarKriteria, bobotKriteria);
        List<Object[]> daftarUrutanAlternatif = buatDataUrutanAlternatif(daftarBarista, daftarKriteria, matriksKeputusan);

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
                bobotKriteria, daftarUrutanKriteria, daftarUrutanAlternatif,
                skorLokal, daftarHasilRanking);
    }

    public List<Object[]> ambilLaporanNormalisasi() throws SQLException {
        List<Barista> daftarBarista = baristaDao.ambilAktif();
        List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
        Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks();

        validasiInput(daftarBarista, daftarKriteria, matriksPenilaian);

        double[][] matriksKeputusan = buatMatriksKeputusan(daftarBarista, daftarKriteria, matriksPenilaian);
        double[][] skorLokal = hitungSkorLokalBarista(daftarBarista, daftarKriteria, matriksKeputusan);
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
                    skorLokal[i][j]
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
        double[] nilaiRoc = hitungNilaiRoc(daftarKriteria.size());
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
            int hasilBanding = Double.compare(dataKedua.kriteria.getBobot(), dataPertama.kriteria.getBobot());
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
                i + 1,
                kriteria.getKode(),
                kriteria.getNama(),
                kriteria.getBobot(),
                bobotKriteria[daftarUrutan.get(i).indeks],
                kriteria.getTipe()
            });
        }
        return dataUrutan;
    }

    private List<Object[]> buatDataUrutanAlternatif(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
            double[][] matriksKeputusan) {
        List<Object[]> dataUrutan = new ArrayList<>();
        for (int j = 0; j < daftarKriteria.size(); j++) {
            List<NilaiAlternatif> daftarNilai = buatDaftarNilaiAlternatif(daftarBarista, matriksKeputusan, j,
                    Kriteria.COST.equals(daftarKriteria.get(j).getTipe()));
            StringBuilder urutanAlternatif = new StringBuilder();
            for (int i = 0; i < daftarNilai.size(); i++) {
                if (i > 0) {
                    urutanAlternatif.append(", ");
                }
                Barista barista = daftarBarista.get(daftarNilai.get(i).indeksBarista);
                urutanAlternatif.append(barista.getKodeBarista());
                urutanAlternatif.append(" - ");
                urutanAlternatif.append(barista.getNama());
                urutanAlternatif.append(" (");
                urutanAlternatif.append(daftarNilai.get(i).nilai);
                urutanAlternatif.append(")");
            }

            Kriteria kriteria = daftarKriteria.get(j);
            dataUrutan.add(new Object[]{
                kriteria.getKode(),
                kriteria.getNama(),
                kriteria.getTipe(),
                urutanAlternatif.toString()
            });
        }
        return dataUrutan;
    }

    private double[][] hitungSkorLokalBarista(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
            double[][] matriksKeputusan) {
        double[][] skorLokal = new double[daftarBarista.size()][daftarKriteria.size()];
        double[] nilaiRocAlternatif = hitungNilaiRoc(daftarBarista.size());

        for (int j = 0; j < daftarKriteria.size(); j++) {
            List<NilaiAlternatif> daftarNilai = buatDaftarNilaiAlternatif(daftarBarista, matriksKeputusan, j,
                    Kriteria.COST.equals(daftarKriteria.get(j).getTipe()));

            // MAGIQ memberi skor ROC berdasarkan urutan alternatif pada setiap kriteria.
            int posisiAwal = 0;
            while (posisiAwal < daftarNilai.size()) {
                int posisiAkhir = posisiAwal;
                while (posisiAkhir + 1 < daftarNilai.size()
                        && Double.compare(daftarNilai.get(posisiAwal).nilai,
                                daftarNilai.get(posisiAkhir + 1).nilai) == 0) {
                    posisiAkhir++;
                }

                double skorRataRata = hitungRataRataRoc(nilaiRocAlternatif, posisiAwal, posisiAkhir);
                for (int posisi = posisiAwal; posisi <= posisiAkhir; posisi++) {
                    NilaiAlternatif nilaiAlternatif = daftarNilai.get(posisi);
                    skorLokal[nilaiAlternatif.indeksBarista][j] = skorRataRata;
                }
                posisiAwal = posisiAkhir + 1;
            }
        }
        return skorLokal;
    }

    private List<NilaiAlternatif> buatDaftarNilaiAlternatif(List<Barista> daftarBarista,
            double[][] matriksKeputusan, int indeksKriteria, final boolean cost) {
        List<NilaiAlternatif> daftarNilai = new ArrayList<>();
        for (int i = 0; i < daftarBarista.size(); i++) {
            daftarNilai.add(new NilaiAlternatif(i, matriksKeputusan[i][indeksKriteria], daftarBarista.get(i).getNama()));
        }

        Collections.sort(daftarNilai, (NilaiAlternatif nilaiPertama, NilaiAlternatif nilaiKedua) -> {
            int hasilBanding = cost
                    ? Double.compare(nilaiPertama.nilai, nilaiKedua.nilai)
                    : Double.compare(nilaiKedua.nilai, nilaiPertama.nilai);
            if (hasilBanding != 0) {
                return hasilBanding;
            }
            return nilaiPertama.nama.compareToIgnoreCase(nilaiKedua.nama);
        });
        return daftarNilai;
    }

    private double[] hitungNilaiRoc(int jumlahData) {
        double[] nilaiRoc = new double[jumlahData];
        for (int urutan = 1; urutan <= jumlahData; urutan++) {
            double total = 0.0;
            for (int pembagi = urutan; pembagi <= jumlahData; pembagi++) {
                total += 1.0 / pembagi;
            }
            nilaiRoc[urutan - 1] = total / jumlahData;
        }
        return nilaiRoc;
    }

    private double hitungRataRataRoc(double[] nilaiRoc, int posisiAwal, int posisiAkhir) {
        double total = 0.0;
        for (int i = posisiAwal; i <= posisiAkhir; i++) {
            total += nilaiRoc[i];
        }
        return total / ((posisiAkhir - posisiAwal) + 1);
    }

    private List<HasilRanking> buatHasilRanking(List<Barista> daftarBarista, double[][] skorLokal,
            double[] bobotKriteria) {
        List<HasilRanking> daftarHasilRanking = new ArrayList<>();

        for (int i = 0; i < daftarBarista.size(); i++) {
            double nilaiMagiq = 0.0;
            for (int j = 0; j < bobotKriteria.length; j++) {
                nilaiMagiq += skorLokal[i][j] * bobotKriteria[j];
            }

            Barista barista = daftarBarista.get(i);
            HasilRanking hasilRanking = new HasilRanking();
            hasilRanking.setIdBarista(barista.getId());
            hasilRanking.setKodeBarista(barista.getKodeBarista());
            hasilRanking.setNamaBarista(barista.getNama());
            hasilRanking.setNilaiMagiq(nilaiMagiq);
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

        daftarKriteria.stream().map((kriteria) -> {
            if (kriteria.getBobot() <= 0) {
                throw new IllegalArgumentException("Bobot kriteria " + kriteria.getKode() + " harus lebih dari 0.");
            }
            return kriteria;
        }).filter((kriteria) -> (!Kriteria.BENEFIT.equals(kriteria.getTipe()) && !Kriteria.COST.equals(kriteria.getTipe()))).forEachOrdered((kriteria) -> {
            throw new IllegalArgumentException("Tipe kriteria " + kriteria.getKode() + " harus BENEFIT atau COST.");
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

    public static class PerhitunganDetail {
        private final List<Barista> daftarBarista;
        private final List<Kriteria> daftarKriteria;
        private final double[][] matriksKeputusan;
        private final double[] bobotKriteria;
        private final List<Object[]> daftarUrutanKriteria;
        private final List<Object[]> daftarUrutanAlternatif;
        private final double[][] skorLokal;
        private final List<HasilRanking> daftarHasilRanking;

        private PerhitunganDetail(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
                double[][] matriksKeputusan, double[] bobotKriteria, List<Object[]> daftarUrutanKriteria,
                List<Object[]> daftarUrutanAlternatif, double[][] skorLokal,
                List<HasilRanking> daftarHasilRanking) {
            this.daftarBarista = daftarBarista;
            this.daftarKriteria = daftarKriteria;
            this.matriksKeputusan = matriksKeputusan;
            this.bobotKriteria = bobotKriteria;
            this.daftarUrutanKriteria = daftarUrutanKriteria;
            this.daftarUrutanAlternatif = daftarUrutanAlternatif;
            this.skorLokal = skorLokal;
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

        public List<Object[]> getDaftarUrutanAlternatif() {
            return daftarUrutanAlternatif;
        }

        public double[][] getSkorLokal() {
            return skorLokal;
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

    private static class NilaiAlternatif {
        private final int indeksBarista;
        private final double nilai;
        private final String nama;

        private NilaiAlternatif(int indeksBarista, double nilai, String nama) {
            this.indeksBarista = indeksBarista;
            this.nilai = nilai;
            this.nama = nama;
        }
    }
}
