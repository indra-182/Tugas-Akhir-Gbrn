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
        Connection koneksi = DatabaseConnection.getConnection();

        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, username);
            perintah.setString(2, PasswordUtil.sha256(password));
            try (ResultSet hasil = perintah.executeQuery()) {
                if (!hasil.next()) {
                    return null;
                }

                Pengguna pengguna = new Pengguna();
                pengguna.setId(hasil.getInt("id"));
                pengguna.setUsername(hasil.getString("username"));
                pengguna.setNamaLengkap(hasil.getString("nama_lengkap"));
                pengguna.setRole(hasil.getString("role"));
                return pengguna;
            }
        }
    }
}
