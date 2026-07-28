package com.gibran.waroenkbikers.dao;

import com.gibran.waroenkbikers.model.Pengguna;
import com.gibran.waroenkbikers.util.DatabaseConnection;
import com.gibran.waroenkbikers.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PenggunaDao {
    public Pengguna login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, nama_lengkap, role FROM pengguna "
                + "WHERE username = ? AND password_hash = ?";
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            System.out.println("[DEBUG] DB connected: " + (koneksi != null && !koneksi.isClosed()));
            perintah = koneksi.prepareStatement(sql);
            perintah.setString(1, username);
            String generatedHash = PasswordUtil.sha256(password);
            System.out.println("[DEBUG] Login attempt: username='" + username + "' password='****' hash='" + generatedHash + "'");
            System.out.println("[DEBUG] SQL: " + sql);
            perintah.setString(2, generatedHash);
            hasil = perintah.executeQuery();
            System.out.println("[DEBUG] Query executed");

            if (!hasil.next()) {
                System.out.println("[DEBUG] No matching row found");
                return null;
            }
            System.out.println("[DEBUG] Row found, user.id=" + hasil.getInt("id"));

            Pengguna pengguna = new Pengguna();
            pengguna.setId(hasil.getInt("id"));
            pengguna.setUsername(hasil.getString("username"));
            pengguna.setNamaLengkap(hasil.getString("nama_lengkap"));
            pengguna.setRole(hasil.getString("role"));
            return pengguna;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }
}
