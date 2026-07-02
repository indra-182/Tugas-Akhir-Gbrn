package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.model.Barista;
import com.gibran.waroenkbikers.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaristaDao {
    private static final String KOLOM_BARISTA =
            "id, kode_barista, nama, divisi, jabatan, tanggal_masuk, status";

    public List<Barista> ambilSemua() throws SQLException {
        String sql = "SELECT " + KOLOM_BARISTA + " FROM barista ORDER BY kode_barista";
        return ambilDaftarBarista(sql, null);
    }

    public List<Barista> ambilAktif() throws SQLException {
        String sql = "SELECT " + KOLOM_BARISTA + " FROM barista WHERE status = ? ORDER BY kode_barista";
        return ambilDaftarBarista(sql, "AKTIF");
    }

    public int hitungSemua() throws SQLException {
        String sql = "SELECT COUNT(*) AS jumlah FROM barista";
        return hitungData(sql);
    }

    public void tambah(Barista barista) throws SQLException {
        String sql = "INSERT INTO barista (kode_barista, nama, divisi, jabatan, tanggal_masuk, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        simpan(sql, barista, false);
    }

    public void ubah(Barista barista) throws SQLException {
        String sql = "UPDATE barista SET kode_barista = ?, nama = ?, divisi = ?, jabatan = ?, "
                + "tanggal_masuk = ?, status = ? WHERE id = ?";
        simpan(sql, barista, true);
    }

    public void hapus(int id) throws SQLException {
        String sql = "DELETE FROM barista WHERE id = ?";
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

    private List<Barista> ambilDaftarBarista(String sql, String status) throws SQLException {
        List<Barista> daftarBarista = new ArrayList<Barista>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            if (status != null) {
                perintah.setString(1, status);
            }
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarBarista.add(petakanBarista(hasil));
            }
            return daftarBarista;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private int hitungData(String sql) throws SQLException {
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

    private void simpan(String sql, Barista barista, boolean ubah) throws SQLException {
        Connection koneksi = null;
        PreparedStatement perintah = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setString(1, barista.getKodeBarista());
            perintah.setString(2, barista.getNama());
            perintah.setString(3, barista.getDivisi());
            perintah.setString(4, barista.getJabatan());
            perintah.setString(5, kosongJadiNull(barista.getTanggalMasuk()));
            perintah.setString(6, barista.getStatus());
            if (ubah) {
                perintah.setInt(7, barista.getId());
            }
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private String kosongJadiNull(String nilai) {
        return nilai == null || nilai.trim().isEmpty() ? null : nilai.trim();
    }

    private Barista petakanBarista(ResultSet hasil) throws SQLException {
        Barista barista = new Barista();
        barista.setId(hasil.getInt("id"));
        barista.setKodeBarista(hasil.getString("kode_barista"));
        barista.setNama(hasil.getString("nama"));
        barista.setDivisi(hasil.getString("divisi"));
        barista.setJabatan(hasil.getString("jabatan"));
        barista.setTanggalMasuk(hasil.getString("tanggal_masuk"));
        barista.setStatus(hasil.getString("status"));
        return barista;
    }
}
