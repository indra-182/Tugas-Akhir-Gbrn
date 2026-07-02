package com.gibran.waroenkbikers.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

public final class TampilanUtil {
    public static final Color WARNA_GARIS = new Color(55, 65, 81);
    public static final Color WARNA_HEADER = new Color(15, 118, 110);
    public static final Color WARNA_HEADER_TERANG = new Color(229, 231, 235);
    public static final Color WARNA_SIDEBAR = new Color(249, 250, 251);
    public static final Color WARNA_LATAR = new Color(243, 244, 246);
    public static final Color WARNA_BIRU_MUDA = new Color(239, 246, 255);
    public static final Color WARNA_BIRU = new Color(15, 118, 110);
    public static final Color WARNA_BIRU_TUA = new Color(15, 118, 110);
    public static final Color WARNA_HIJAU = new Color(22, 163, 74);
    public static final Color WARNA_KUNING = new Color(245, 158, 11);
    public static final Color WARNA_MERAH = new Color(220, 38, 38);
    public static final Color WARNA_ABU = new Color(107, 114, 128);
    public static final Font FONT_NORMAL = new Font("Tahoma", Font.PLAIN, 12);
    public static final Font FONT_TEBAL = new Font("Tahoma", Font.BOLD, 13);
    public static final Font FONT_JUDUL = new Font("Tahoma", Font.BOLD, 18);

    private TampilanUtil() {
    }

    public static JLabel buatJudul(String teks) {
        JLabel label = new JLabel(teks, SwingConstants.CENTER);
        label.setFont(FONT_JUDUL);
        label.setForeground(WARNA_BIRU_TUA);
        return label;
    }

    public static JButton buatTombol(String teks) {
        return buatTombol(teks, Color.WHITE, WARNA_GARIS);
    }

    public static JButton buatTombol(String teks, Color warnaLatar, Color warnaTeks) {
        JButton tombol = new JButton(teks);
        tombol.setFont(FONT_TEBAL);
        tombol.setFocusPainted(false);
        tombol.setPreferredSize(new Dimension(118, 32));
        tombol.setMargin(new Insets(4, 12, 4, 12));
        tombol.setBorder(BorderFactory.createLineBorder(warnaTeks));
        tombol.setBackground(warnaLatar);
        tombol.setForeground(warnaTeks);
        tombol.setOpaque(true);
        tombol.setContentAreaFilled(true);
        tombol.setRolloverEnabled(true);
        tombol.putClientProperty("Button.foreground", warnaTeks);
        return tombol;
    }

    public static JButton buatTombolAksi(String teks) {
        Color warnaTeks = WARNA_BIRU_TUA;
        Color warnaLatar = Color.WHITE;
        if (null != teks) switch (teks) {
            case "Simpan":
            case "Proses":
                warnaTeks = WARNA_HIJAU;
                break;
            case "Ubah":
                warnaTeks = new Color(146, 64, 14);
                break;
            case "Hapus":
            case "Logout":
                warnaTeks = WARNA_MERAH;
                break;
            case "Reset":
            case "Batal":
            case "Muat":
                warnaTeks = WARNA_GARIS;
                break;
            case "Login":
            case "Tambah":
                warnaLatar = WARNA_BIRU_MUDA;
                break;
            default:
                break;
        }
        return buatTombol(teks, warnaLatar, warnaTeks);
    }

    public static void terapkanWarnaTombolGlobal() {
        UIManager.put("Button.foreground", WARNA_GARIS);
        UIManager.put("Button.select", WARNA_BIRU_MUDA);
        UIManager.put("Button.focus", WARNA_BIRU_TUA);
    }

    public static JPanel buatPanelBergaris() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(WARNA_GARIS));
        return panel;
    }

    public static void rapikanTabel(JTable tabel) {
        tabel.setFont(FONT_NORMAL);
        tabel.setRowHeight(25);
        tabel.setGridColor(new Color(209, 213, 219));
        tabel.setShowGrid(true);
        JTableHeader header = tabel.getTableHeader();
        header.setFont(FONT_TEBAL);
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(new Color(55, 65, 81));
    }
}
