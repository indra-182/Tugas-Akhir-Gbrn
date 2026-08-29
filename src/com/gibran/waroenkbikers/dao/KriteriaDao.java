package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.util.DatabaseConnection;
import com.gibran.waroenkbikers.util.PrioritasKriteriaValidator;
import com.gibran.waroenkbikers.util.RocWeightCalculator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KriteriaDao {
    public List<Kriteria> ambilSemua() throws SQLException {
        Connection koneksi = DatabaseConnection.getConnection();
        return ambilSemua(koneksi, false);
    }

    public int hitungSemua() throws SQLException {
        String sql = "SELECT COUNT(*) AS jumlah FROM kriteria";
        Connection koneksi = DatabaseConnection.getConnection();
        try (PreparedStatement perintah = koneksi.prepareStatement(sql);
                ResultSet hasil = perintah.executeQuery()) {
            return hasil.next() ? hasil.getInt("jumlah") : 0;
        }
    }

    public void tambah(Kriteria kriteria) throws SQLException {
        Connection koneksi = null;
        boolean transaksiAktif = false;
        try {
            koneksi = DatabaseConnection.getConnection();
            koneksi.setAutoCommit(false);
            transaksiAktif = true;
            kunciTabelKriteria(koneksi);

            List<Kriteria> daftarLama = ambilSemua(koneksi, true);
            int jumlahLama = daftarLama.size();
            int prioritasBaru = kriteria.getUrutanPrioritas() <= 0
                    ? jumlahLama + 1 : kriteria.getUrutanPrioritas();
            validasiPrioritas(prioritasBaru, jumlahLama + 1);
            validasiPrioritasUnik(daftarLama, prioritasBaru, 0);

            int prioritasMaksimal = prioritasMaksimal(daftarLama);
            int offsetSementara = prioritasMaksimal + 1;
            pindahkanPrioritasSementara(koneksi, offsetSementara);
            int prioritasSementara = offsetSementara + prioritasMaksimal + 1;
            int idBaru = masukkanSementara(koneksi, kriteria, prioritasSementara);
            kriteria.setId(idBaru);

            List<Kriteria> daftarBaru = new ArrayList<>(daftarLama);
            daftarBaru.add(prioritasBaru - 1, kriteria);
            simpanUrutanDanBobot(koneksi, daftarBaru);
            koneksi.commit();
        } catch (SQLException ex) {
            rollback(koneksi, ex);
            throw ex;
        } catch (RuntimeException ex) {
            rollback(koneksi, ex);
            throw ex;
        } finally {
            resetAutoCommit(koneksi, transaksiAktif);
        }
    }

    public void ubah(Kriteria kriteria) throws SQLException {
        Connection koneksi = null;
        boolean transaksiAktif = false;
        try {
            koneksi = DatabaseConnection.getConnection();
            koneksi.setAutoCommit(false);
            transaksiAktif = true;
            kunciTabelKriteria(koneksi);

            List<Kriteria> daftarLama = ambilSemua(koneksi, true);
            int indeksLama = indeksDenganId(daftarLama, kriteria.getId());
            if (indeksLama < 0) {
                throw new SQLException("Data kriteria tidak ditemukan.");
            }

            int prioritasBaru = kriteria.getUrutanPrioritas() <= 0
                    ? daftarLama.get(indeksLama).getUrutanPrioritas()
                    : kriteria.getUrutanPrioritas();
            validasiPrioritas(prioritasBaru, daftarLama.size());
            validasiPrioritasUnik(daftarLama, prioritasBaru, kriteria.getId());

            int prioritasMaksimal = prioritasMaksimal(daftarLama);
            int offsetSementara = prioritasMaksimal + 1;
            pindahkanPrioritasSementara(koneksi, offsetSementara);

            List<Kriteria> daftarBaru = new ArrayList<>(daftarLama);
            daftarBaru.remove(indeksLama);
            daftarBaru.add(prioritasBaru - 1, kriteria);
            ubahDataKriteria(koneksi, kriteria);
            simpanUrutanDanBobot(koneksi, daftarBaru);
            koneksi.commit();
        } catch (SQLException ex) {
            rollback(koneksi, ex);
            throw ex;
        } catch (RuntimeException ex) {
            rollback(koneksi, ex);
            throw ex;
        } finally {
            resetAutoCommit(koneksi, transaksiAktif);
        }
    }

    public void hapus(int id) throws SQLException {
        Connection koneksi = null;
        boolean transaksiAktif = false;
        try {
            koneksi = DatabaseConnection.getConnection();
            koneksi.setAutoCommit(false);
            transaksiAktif = true;
            kunciTabelKriteria(koneksi);

            List<Kriteria> daftarLama = ambilSemua(koneksi, true);
            int indeksLama = indeksDenganId(daftarLama, id);
            if (indeksLama < 0) {
                throw new SQLException("Data kriteria tidak ditemukan.");
            }

            int prioritasMaksimal = prioritasMaksimal(daftarLama);
            pindahkanPrioritasSementara(koneksi, prioritasMaksimal + 1);
            hapusDataKriteria(koneksi, id);

            daftarLama.remove(indeksLama);
            simpanUrutanDanBobot(koneksi, daftarLama);
            koneksi.commit();
        } catch (SQLException ex) {
            rollback(koneksi, ex);
            throw ex;
        } catch (RuntimeException ex) {
            rollback(koneksi, ex);
            throw ex;
        } finally {
            resetAutoCommit(koneksi, transaksiAktif);
        }
    }

    private List<Kriteria> ambilSemua(Connection koneksi, boolean kunciBaris) throws SQLException {
        String sql = "SELECT id, kode, nama, bobot, urutan_prioritas, keterangan "
                + "FROM kriteria ORDER BY urutan_prioritas";
        if (kunciBaris) {
            sql += " FOR UPDATE";
        }

        List<Kriteria> daftarKriteria = new ArrayList<>();
        try (PreparedStatement perintah = koneksi.prepareStatement(sql);
                ResultSet hasil = perintah.executeQuery()) {
            while (hasil.next()) {
                daftarKriteria.add(petakanKriteria(hasil));
            }
            return daftarKriteria;
        }
    }

    private int masukkanSementara(Connection koneksi, Kriteria kriteria, int prioritasSementara)
            throws SQLException {
        String sql = "INSERT INTO kriteria "
                + "(kode, nama, bobot, urutan_prioritas, keterangan) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement perintah = koneksi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            perintah.setString(1, kriteria.getKode());
            perintah.setString(2, kriteria.getNama());
            perintah.setDouble(3, kriteria.getBobot());
            perintah.setInt(4, prioritasSementara);
            perintah.setString(5, kriteria.getKeterangan());
            perintah.executeUpdate();
            try (ResultSet generatedKeys = perintah.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("ID kriteria baru tidak dapat dibaca.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    private void ubahDataKriteria(Connection koneksi, Kriteria kriteria) throws SQLException {
        String sql = "UPDATE kriteria SET kode = ?, nama = ?, keterangan = ? WHERE id = ?";
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, kriteria.getKode());
            perintah.setString(2, kriteria.getNama());
            perintah.setString(3, kriteria.getKeterangan());
            perintah.setInt(4, kriteria.getId());
            perintah.executeUpdate();
        }
    }

    private void hapusDataKriteria(Connection koneksi, int id) throws SQLException {
        String sql = "DELETE FROM kriteria WHERE id = ?";
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setInt(1, id);
            perintah.executeUpdate();
        }
    }

    private void pindahkanPrioritasSementara(Connection koneksi, int offset) throws SQLException {
        if (offset <= 0) {
            throw new SQLException("Jumlah prioritas kriteria tidak valid.");
        }
        String sql = "UPDATE kriteria SET urutan_prioritas = urutan_prioritas + ?";
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setInt(1, offset);
            perintah.executeUpdate();
        }
    }

    private void kunciTabelKriteria(Connection koneksi) throws SQLException {
        try (Statement perintah = koneksi.createStatement()) {
            perintah.execute("LOCK TABLE kriteria IN SHARE ROW EXCLUSIVE MODE");
        }
    }

    private void simpanUrutanDanBobot(Connection koneksi, List<Kriteria> daftarKriteria) throws SQLException {
        double[] bobot = daftarKriteria.isEmpty()
                ? new double[0] : RocWeightCalculator.hitungSemua(daftarKriteria.size());
        String sql = "UPDATE kriteria SET urutan_prioritas = ?, bobot = ? WHERE id = ?";
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < daftarKriteria.size(); i++) {
                Kriteria kriteria = daftarKriteria.get(i);
                int urutan = i + 1;
                kriteria.setUrutanPrioritas(urutan);
                kriteria.setBobot(bobot[i]);
                perintah.setInt(1, urutan);
                perintah.setDouble(2, bobot[i]);
                perintah.setInt(3, kriteria.getId());
                perintah.addBatch();
            }
            perintah.executeBatch();
        }
    }

    private int prioritasMaksimal(List<Kriteria> daftarKriteria) {
        int maksimal = 0;
        for (Kriteria kriteria : daftarKriteria) {
            maksimal = Math.max(maksimal, kriteria.getUrutanPrioritas());
        }
        return maksimal;
    }

    private int indeksDenganId(List<Kriteria> daftarKriteria, int id) {
        for (int i = 0; i < daftarKriteria.size(); i++) {
            if (daftarKriteria.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void validasiPrioritas(int prioritas, int jumlahKriteria) {
        if (prioritas < 1 || prioritas > jumlahKriteria) {
            throw new IllegalArgumentException("Urutan prioritas harus antara 1 dan " + jumlahKriteria + ".");
        }
    }

    private void validasiPrioritasUnik(List<Kriteria> daftarKriteria, int prioritasBaru, int idDikecualikan) {
        int jumlahPrioritas = daftarKriteria.size() + (idDikecualikan == 0 ? 1 : 0);
        int[] semuaPrioritas = new int[jumlahPrioritas];
        int indeks = 0;
        for (Kriteria kriteria : daftarKriteria) {
            if (kriteria.getId() != idDikecualikan) {
                semuaPrioritas[indeks++] = kriteria.getUrutanPrioritas();
            }
        }
        semuaPrioritas[indeks] = prioritasBaru;
        PrioritasKriteriaValidator.validasiUnik(semuaPrioritas);
    }

    private void rollback(Connection koneksi, Exception penyebab) {
        if (koneksi == null) {
            return;
        }
        try {
            koneksi.rollback();
        } catch (SQLException ex) {
            penyebab.addSuppressed(ex);
        }
    }

    private void resetAutoCommit(Connection koneksi, boolean transaksiAktif) throws SQLException {
        if (koneksi != null && transaksiAktif) {
            koneksi.setAutoCommit(true);
        }
    }

    private Kriteria petakanKriteria(ResultSet hasil) throws SQLException {
        Kriteria kriteria = new Kriteria();
        kriteria.setId(hasil.getInt("id"));
        kriteria.setKode(hasil.getString("kode"));
        kriteria.setNama(hasil.getString("nama"));
        kriteria.setBobot(hasil.getDouble("bobot"));
        kriteria.setUrutanPrioritas(hasil.getInt("urutan_prioritas"));
        kriteria.setKeterangan(hasil.getString("keterangan"));
        return kriteria;
    }
}
