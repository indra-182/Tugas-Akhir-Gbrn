package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.model.HasilRanking;
import com.gibran.waroenkbikers.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HasilRankingDao {
    public void hapusSemua() throws SQLException {
        String hapusSql = "DELETE FROM hasil_ranking";
        Connection koneksi = null;
        PreparedStatement perintahHapus = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintahHapus = koneksi.prepareStatement(hapusSql);
            perintahHapus.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintahHapus);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public void gantiSemua(List<HasilRanking> daftarHasilRanking) throws SQLException {
        String hapusSql = "DELETE FROM hasil_ranking";
        String tambahSql = "INSERT INTO hasil_ranking "
                + "(id_barista, nilai_magiq, peringkat) VALUES (?, ?, ?)";

        Connection koneksi = null;
        PreparedStatement perintahHapus = null;
        PreparedStatement perintahTambah = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            koneksi.setAutoCommit(false);

            perintahHapus = koneksi.prepareStatement(hapusSql);
            perintahHapus.executeUpdate();

            perintahTambah = koneksi.prepareStatement(tambahSql);
            for (HasilRanking hasilRanking : daftarHasilRanking) {
                perintahTambah.setInt(1, hasilRanking.getIdBarista());
                perintahTambah.setDouble(2, hasilRanking.getNilaiMagiq());
                perintahTambah.setInt(3, hasilRanking.getPeringkat());
                perintahTambah.addBatch();
            }
            perintahTambah.executeBatch();
            koneksi.commit();
        } catch (SQLException ex) {
            if (koneksi != null) {
                koneksi.rollback();
            }
            throw ex;
        } finally {
            DatabaseConnection.closeQuietly(perintahTambah);
            DatabaseConnection.closeQuietly(perintahHapus);
            if (koneksi != null) {
                koneksi.setAutoCommit(true);
            }
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<HasilRanking> ambilSemua() throws SQLException {
        String sql = "SELECT h.id_barista, b.kode_barista, b.nama, "
                + "h.nilai_magiq, h.peringkat "
                + "FROM hasil_ranking h "
                + "JOIN barista b ON h.id_barista = b.id "
                + "ORDER BY h.peringkat";
        List<HasilRanking> daftarHasilRanking = new ArrayList<HasilRanking>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                HasilRanking hasilRanking = new HasilRanking();
                hasilRanking.setIdBarista(hasil.getInt("id_barista"));
                hasilRanking.setKodeBarista(hasil.getString("kode_barista"));
                hasilRanking.setNamaBarista(hasil.getString("nama"));
                hasilRanking.setNilaiMagiq(hasil.getDouble("nilai_magiq"));
                hasilRanking.setPeringkat(hasil.getInt("peringkat"));
                daftarHasilRanking.add(hasilRanking);
            }
            return daftarHasilRanking;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }
}
