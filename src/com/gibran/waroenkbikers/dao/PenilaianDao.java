package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PenilaianDao {
    public Map<Integer, Map<Integer, Double>> ambilSemuaSebagaiMatriks() throws SQLException {
        String sql = "SELECT id_barista, id_kriteria, nilai FROM penilaian";
        Map<Integer, Map<Integer, Double>> matriks = new HashMap<>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                int idBarista = hasil.getInt("id_barista");
                int idKriteria = hasil.getInt("id_kriteria");
                double nilai = hasil.getDouble("nilai");

                if (!matriks.containsKey(idBarista)) {
                    matriks.put(idBarista, new HashMap<>());
                }
                matriks.get(idBarista).put(idKriteria, nilai);
            }
            return matriks;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public Map<Integer, Double> ambilBerdasarkanBarista(int idBarista) throws SQLException {
        String sql = "SELECT id_kriteria, nilai FROM penilaian WHERE id_barista = ?";
        Map<Integer, Double> daftarNilai = new HashMap<Integer, Double>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idBarista);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarNilai.put(hasil.getInt("id_kriteria"), hasil.getDouble("nilai"));
            }
            return daftarNilai;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public void simpan(int idBarista, int idKriteria, double nilai) throws SQLException {
        String sql = "INSERT INTO penilaian (id_barista, id_kriteria, nilai) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)";
        Connection koneksi = null;
        PreparedStatement perintah = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idBarista);
            perintah.setInt(2, idKriteria);
            perintah.setDouble(3, nilai);
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public boolean apakahPenilaianLengkap() throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM barista WHERE status = 'AKTIF') AS jumlah_barista, "
                + "(SELECT COUNT(*) FROM kriteria) AS jumlah_kriteria, "
                + "(SELECT COUNT(*) FROM penilaian p "
                + "JOIN barista k ON p.id_barista = k.id WHERE k.status = 'AKTIF') AS jumlah_penilaian";
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            if (!hasil.next()) {
                return false;
            }
            int jumlahBarista = hasil.getInt("jumlah_barista");
            int jumlahKriteria = hasil.getInt("jumlah_kriteria");
            int jumlahPenilaian = hasil.getInt("jumlah_penilaian");
            return jumlahBarista > 0 && jumlahKriteria > 0
                    && jumlahPenilaian == jumlahBarista * jumlahKriteria;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<Object[]> ambilLaporanPenilaian() throws SQLException {
        String sql = "SELECT k.kode_barista, k.nama AS nama_barista, kr.kode AS kode_kriteria, "
                + "kr.nama AS nama_kriteria, p.nilai "
                + "FROM penilaian p "
                + "JOIN barista k ON p.id_barista = k.id "
                + "JOIN kriteria kr ON p.id_kriteria = kr.id "
                + "ORDER BY k.kode_barista, kr.kode";
        List<Object[]> data = new ArrayList<Object[]>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                data.add(new Object[]{
                    hasil.getString("kode_barista"),
                    hasil.getString("nama_barista"),
                    hasil.getString("kode_kriteria"),
                    hasil.getString("nama_kriteria"),
                    hasil.getDouble("nilai")
                });
            }
            return data;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }
}
