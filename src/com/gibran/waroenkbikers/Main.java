package com.gibran.waroenkbikers;

import com.gibran.waroenkbikers.ui.LoginFrame;
import com.gibran.waroenkbikers.util.DialogUtil;
import com.gibran.waroenkbikers.util.DatabaseConnection;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                com.gibran.waroenkbikers.ui.TampilanUtil.terapkanWarnaTombolGlobal();
                DatabaseConnection.testConnection();
                new LoginFrame().setVisible(true);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException | UnsupportedLookAndFeelException ex) {
                DialogUtil.showError(null,
                        "Aplikasi gagal dijalankan. Pastikan XAMPP/MySQL aktif, database sudah di-import, dan MySQL Connector/J sudah ditambahkan ke Libraries.\n\nDetail: "
                                + ambilPesanError(ex));
            }
        });
    }

    private static String ambilPesanError(Exception ex) {
        Throwable error = ex;
        while ((error.getMessage() == null || error.getMessage().trim().isEmpty())
                && error.getCause() != null) {
            error = error.getCause();
        }

        String pesan = error.getMessage();
        if (pesan == null || pesan.trim().isEmpty()) {
            return error.getClass().getName();
        }
        return pesan;
    }
}
