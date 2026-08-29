package com.gibran.waroenkbikers.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String CONFIG_FILE = "/config.properties";
    private static final long BATAS_IDLE_VALIDASI_MILIS = 60_000L;

    private static Properties properties;
    private static Connection koneksiBersama;
    private static long terakhirDipakaiMilis;

    private DatabaseConnection() {
    }

    /**
     * Mengembalikan koneksi bersama (dipakai ulang oleh semua DAO).
     * Koneksi hanya dibuka sekali; ke database cloud, handshake TCP+TLS per
     * pemanggilan terlalu mahal. Reconnect otomatis jika koneksi terputus.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (koneksiMasihHidup()) {
            terakhirDipakaiMilis = System.currentTimeMillis();
            return koneksiBersama;
        }

        loadProperties();
        koneksiBersama = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
        terakhirDipakaiMilis = System.currentTimeMillis();
        return koneksiBersama;
    }

    private static boolean koneksiMasihHidup() {
        if (koneksiBersama == null) {
            return false;
        }
        try {
            if (koneksiBersama.isClosed()) {
                return false;
            }
            long idleMilis = System.currentTimeMillis() - terakhirDipakaiMilis;
            if (idleMilis > BATAS_IDLE_VALIDASI_MILIS && !koneksiBersama.isValid(2)) {
                tutupKoneksiBersama();
                return false;
            }
            return true;
        } catch (SQLException ex) {
            tutupKoneksiBersama();
            return false;
        }
    }

    private static void tutupKoneksiBersama() {
        Connection koneksi = koneksiBersama;
        koneksiBersama = null;
        if (koneksi == null) {
            return;
        }
        try {
            koneksi.close();
        } catch (SQLException ignored) {
        }
    }

    private static void loadProperties() throws SQLException {
        if (properties != null) {
            return;
        }

        properties = new Properties();
        try (InputStream inputStream = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new SQLException("File konfigurasi database tidak ditemukan: " + CONFIG_FILE);
            }
            try {
                properties.load(inputStream);
            } catch (IOException ex) {
                throw new SQLException("Gagal membaca konfigurasi database.", ex);
            }
        } catch (IOException ex) {
            throw new SQLException("Gagal menutup file konfigurasi database.", ex);
        }
    }

}
