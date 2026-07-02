package com.gibran.waroenkbikers.service;

import com.gibran.waroenkbikers.dao.PenggunaDao;
import com.gibran.waroenkbikers.model.Pengguna;
import java.sql.SQLException;

public class AutentikasiService {
    private final PenggunaDao penggunaDao = new PenggunaDao();

    public Pengguna login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username wajib diisi.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password wajib diisi.");
        }
        return penggunaDao.login(username.trim(), password);
    }
}
