package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.util.DatabaseConnection;
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
        Connection koneksi = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            return ambilSemua(koneksi, false);
        } finally {
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public int hitungSemua() throws SQLException {
        String sql = "SELECT COUNT(*) AS jumlah FROM kriteria";
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            return hasil.next() ? hasil.getInt("jumlah") : 0;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
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
            DatabaseConnection.closeQuietly(koneksi);
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
            DatabaseConnection.closeQuietly(koneksi);
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
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private List<Kriteria> ambilSemua(Connection koneksi, boolean kunciBaris) throws SQLException {
        String sql = "SELECT id, kode, nama, bobot, urutan_prioritas, tipe, keterangan "
                + "FROM kriteria ORDER BY urutan_prioritas";
        if (kunciBaris) {
            sql += " FOR UPDATE";
        }

        List<Kriteria> daftarKriteria = new ArrayList<>();
        PreparedStatement perintah = null;
        ResultSet hasil = null;
        try {
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarKriteria.add(petakanKriteria(hasil));
            }
            return daftarKriteria;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
        }
    }

    private int masukkanSementara(Connection koneksi, Kriteria kriteria, int prioritasSementara)
            throws SQLException {
        String sql = "INSERT INTO kriteria "
                + "(kode, nama, bobot, urutan_prioritas, tipe, keterangan) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement perintah = null;
        ResultSet generatedKeys = null;
        try {
            perintah = koneksi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            perintah.setString(1, kriteria.getKode());
            perintah.setString(2, kriteria.getNama());
            perintah.setDouble(3, kriteria.getBobot());
            perintah.setInt(4, prioritasSementara);
            perintah.setString(5, kriteria.getTipe());
            perintah.setString(6, kriteria.getKeterangan());
            perintah.executeUpdate();
            generatedKeys = perintah.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("ID kriteria baru tidak dapat dibaca.");
            }
            return generatedKeys.getInt(1);
        } finally {
            DatabaseConnection.closeQuietly(generatedKeys);
            DatabaseConnection.closeQuietly(perintah);
        }
    }

    private void ubahDataKriteria(Connection koneksi, Kriteria kriteria) throws SQLException {
        String sql = "UPDATE kriteria SET kode = ?, nama = ?, tipe = ?, keterangan = ? WHERE id = ?";
        PreparedStatement perintah = null;
        try {
            perintah = koneksi.prepareStatement(sql);
            perintah.setString(1, kriteria.getKode());
            perintah.setString(2, kriteria.getNama());
            perintah.setString(3, kriteria.getTipe());
            perintah.setString(4, kriteria.getKeterangan());
            perintah.setInt(5, kriteria.getId());
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
        }
    }

    private void hapusDataKriteria(Connection koneksi, int id) throws SQLException {
        String sql = "DELETE FROM kriteria WHERE id = ?";
        PreparedStatement perintah = null;
        try {
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, id);
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
        }
    }

    private void pindahkanPrioritasSementara(Connection koneksi, int offset) throws SQLException {
        if (offset <= 0) {
            throw new SQLException("Jumlah prioritas kriteria tidak valid.");
        }
        String sql = "UPDATE kriteria SET urutan_prioritas = urutan_prioritas + ?";
        PreparedStatement perintah = null;
        try {
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, offset);
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
        }
    }

    private void kunciTabelKriteria(Connection koneksi) throws SQLException {
        Statement perintah = null;
        try {
            perintah = koneksi.createStatement();
            perintah.execute("LOCK TABLE kriteria IN SHARE ROW EXCLUSIVE MODE");
        } finally {
            DatabaseConnection.closeQuietly(perintah);
        }
    }

    private void simpanUrutanDanBobot(Connection koneksi, List<Kriteria> daftarKriteria) throws SQLException {
        double[] bobot = daftarKriteria.isEmpty()
                ? new double[0] : RocWeightCalculator.hitungSemua(daftarKriteria.size());
        String sql = "UPDATE kriteria SET urutan_prioritas = ?, bobot = ? WHERE id = ?";
        PreparedStatement perintah = null;
        try {
            perintah = koneksi.prepareStatement(sql);
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
        } finally {
            DatabaseConnection.closeQuietly(perintah);
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
        kriteria.setTipe(hasil.getString("tipe"));
        kriteria.setKeterangan(hasil.getString("keterangan"));
        return kriteria;
    }
}
