package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KriteriaDao {
    public List<Kriteria> ambilSemua() throws SQLException {
        String sql = "SELECT id, kode, nama, bobot, tipe, keterangan FROM kriteria ORDER BY kode";
        List<Kriteria> daftarKriteria = new ArrayList<>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarKriteria.add(petakanKriteria(hasil));
            }
            return daftarKriteria;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
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
        String sql = "INSERT INTO kriteria (kode, nama, bobot, tipe, keterangan) VALUES (?, ?, ?, ?, ?)";
        simpan(sql, kriteria, false);
    }

    public void ubah(Kriteria kriteria) throws SQLException {
        String sql = "UPDATE kriteria SET kode = ?, nama = ?, bobot = ?, tipe = ?, keterangan = ? WHERE id = ?";
        simpan(sql, kriteria, true);
    }

    public void hapus(int id) throws SQLException {
        String sql = "DELETE FROM kriteria WHERE id = ?";
        Connection koneksi = null;
        PreparedStatement perintah = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, id);
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private void simpan(String sql, Kriteria kriteria, boolean ubah) throws SQLException {
        Connection koneksi = null;
        PreparedStatement perintah = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setString(1, kriteria.getKode());
            perintah.setString(2, kriteria.getNama());
            perintah.setDouble(3, kriteria.getBobot());
            perintah.setString(4, kriteria.getTipe());
            perintah.setString(5, kriteria.getKeterangan());
            if (ubah) {
                perintah.setInt(6, kriteria.getId());
            }
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private Kriteria petakanKriteria(ResultSet hasil) throws SQLException {
        Kriteria kriteria = new Kriteria();
        kriteria.setId(hasil.getInt("id"));
        kriteria.setKode(hasil.getString("kode"));
        kriteria.setNama(hasil.getString("nama"));
        kriteria.setBobot(hasil.getDouble("bobot"));
        kriteria.setTipe(hasil.getString("tipe"));
        kriteria.setKeterangan(hasil.getString("keterangan"));
        return kriteria;
    }
}
