package com.gibran.waroenkbikers.ui;

import com.gibran.waroenkbikers.dao.HasilRankingDao;
import com.gibran.waroenkbikers.dao.BaristaDao;
import com.gibran.waroenkbikers.dao.KriteriaDao;
import com.gibran.waroenkbikers.dao.PenilaianDao;
import com.gibran.waroenkbikers.util.DialogUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardPanel extends JPanel {
    private final BaristaDao baristaDao = new BaristaDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();
    private final JPanel kartuPanel = new JPanel(new GridLayout(2, 2, 32, 26));

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(42, 64, 48, 64));
        buatTampilan();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                muatData();
            }
        });
    }

    private void buatTampilan() {
        add(TampilanUtil.buatJudul("MENU UTAMA"), BorderLayout.NORTH);

        kartuPanel.setBackground(Color.WHITE);
        kartuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 48, 0));
        add(kartuPanel, BorderLayout.CENTER);
        muatData();
    }

    private void muatData() {
        kartuPanel.removeAll();

        try {
            int jumlahBarista = baristaDao.hitungSemua();
            int jumlahKriteria = kriteriaDao.hitungSemua();
            String statusPenilaian = penilaianDao.apakahPenilaianLengkap() ? "Lengkap" : "Belum Lengkap";
            String statusRanking = hasilRankingDao.ambilSemua().isEmpty() ? "Siap dihitung" : "Sudah dihitung";

            kartuPanel.add(buatKartu("Jumlah Barista", String.valueOf(jumlahBarista)));
            kartuPanel.add(buatKartu("Jumlah Kriteria", String.valueOf(jumlahKriteria)));
            kartuPanel.add(buatKartu("Data Penilaian", statusPenilaian));
            kartuPanel.add(buatKartu("Ranking Terbaik", statusRanking));
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
            kartuPanel.add(buatKartu("Jumlah Barista", "-"));
            kartuPanel.add(buatKartu("Jumlah Kriteria", "-"));
            kartuPanel.add(buatKartu("Data Penilaian", "-"));
            kartuPanel.add(buatKartu("Ranking Terbaik", "-"));
        }

        kartuPanel.revalidate();
        kartuPanel.repaint();
    }

    private JPanel buatKartu(String judul, String nilai) {
        JPanel kartu = new JPanel(new GridLayout(2, 1));
        kartu.setBackground(TampilanUtil.WARNA_BIRU_MUDA);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TampilanUtil.WARNA_BIRU_TUA, 2),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JLabel judulLabel = new JLabel(judul, SwingConstants.CENTER);
        judulLabel.setFont(TampilanUtil.FONT_TEBAL);
        JLabel nilaiLabel = new JLabel(nilai, SwingConstants.CENTER);
        nilaiLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        nilaiLabel.setForeground(TampilanUtil.WARNA_BIRU_TUA);

        kartu.add(judulLabel);
        kartu.add(nilaiLabel);
        return kartu;
    }
}
