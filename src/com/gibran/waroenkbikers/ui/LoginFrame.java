package com.gibran.waroenkbikers.ui;

import com.gibran.waroenkbikers.model.Pengguna;
import com.gibran.waroenkbikers.service.AutentikasiService;
import com.gibran.waroenkbikers.util.DialogUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JButton loginButton = TampilanUtil.buatTombolAksi("Login");
    private final JButton batalButton = TampilanUtil.buatTombolAksi("Batal");
    private final AutentikasiService autentikasiService = new AutentikasiService();

    public LoginFrame() {
        setTitle("SPK Kualitas Racikan Kopi Barista - Waroenk Bikers");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 540));
        setLocationRelativeTo(null);
        buatTampilan();
        pasangEvent();
    }

    private void buatTampilan() {
        JPanel panelUtama = TampilanUtil.buatPanelBergaris();
        panelUtama.setLayout(new BorderLayout());
        panelUtama.setBackground(TampilanUtil.WARNA_LATAR);
        panelUtama.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(32, 48, 32, 48),
                BorderFactory.createLineBorder(TampilanUtil.WARNA_GARIS, 2)));

        JLabel headerLabel = new JLabel("SPK Kualitas Racikan Kopi Barista - Waroenk Bikers");
        headerLabel.setFont(TampilanUtil.FONT_TEBAL);
        headerLabel.setForeground(java.awt.Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TampilanUtil.WARNA_HEADER);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TampilanUtil.WARNA_GARIS));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JPanel isiPanel = new JPanel(new GridBagLayout());
        isiPanel.setBackground(TampilanUtil.WARNA_LATAR);
        JPanel loginPanel = buatPanelLogin();
        isiPanel.add(loginPanel);

        panelUtama.add(headerPanel, BorderLayout.NORTH);
        panelUtama.add(isiPanel, BorderLayout.CENTER);
        setContentPane(panelUtama);
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel buatPanelLogin() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(java.awt.Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TampilanUtil.WARNA_GARIS, 2),
                BorderFactory.createEmptyBorder(22, 36, 22, 36)));

        GridBagConstraints batas = new GridBagConstraints();
        batas.insets = new Insets(7, 8, 7, 8);
        batas.fill = GridBagConstraints.HORIZONTAL;

        JLabel judulLabel = new JLabel("LOGIN SISTEM", JLabel.CENTER);
        judulLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        judulLabel.setForeground(TampilanUtil.WARNA_BIRU_TUA);
        batas.gridx = 0;
        batas.gridy = 0;
        batas.gridwidth = 2;
        loginPanel.add(judulLabel, batas);

        batas.gridwidth = 1;
        batas.gridy = 1;
        batas.gridx = 0;
        loginPanel.add(new JLabel("Username"), batas);
        batas.gridx = 1;
        loginPanel.add(usernameField, batas);

        batas.gridy = 2;
        batas.gridx = 0;
        loginPanel.add(new JLabel("Password"), batas);
        batas.gridx = 1;
        loginPanel.add(passwordField, batas);

        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        tombolPanel.setBackground(java.awt.Color.WHITE);
        tombolPanel.add(loginButton);
        tombolPanel.add(batalButton);

        batas.gridy = 3;
        batas.gridx = 0;
        batas.gridwidth = 2;
        batas.insets = new Insets(24, 8, 8, 8);
        loginPanel.add(tombolPanel, batas);

        return loginPanel;
    }

    private void pasangEvent() {
        loginButton.addActionListener(e -> login());
        batalButton.addActionListener(e -> System.exit(0));
    }

    private void login() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            Pengguna pengguna = autentikasiService.login(username, password);
            if (pengguna == null) {
                DialogUtil.showWarning(this, "Username atau password salah.");
                return;
            }

            MainFrame mainFrame = new MainFrame(pengguna);
            mainFrame.setVisible(true);
            dispose();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }
}
