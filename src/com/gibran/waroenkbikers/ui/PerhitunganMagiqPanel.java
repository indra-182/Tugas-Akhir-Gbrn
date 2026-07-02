package com.gibran.waroenkbikers.ui;

import com.gibran.waroenkbikers.dao.BaristaDao;
import com.gibran.waroenkbikers.dao.KriteriaDao;
import com.gibran.waroenkbikers.dao.PenilaianDao;
import com.gibran.waroenkbikers.model.HasilRanking;
import com.gibran.waroenkbikers.model.Barista;
import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.service.PerhitunganMagiqService;
import com.gibran.waroenkbikers.service.PerhitunganMagiqService.PerhitunganDetail;
import com.gibran.waroenkbikers.util.DialogUtil;
import com.gibran.waroenkbikers.util.NumberUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PerhitunganMagiqPanel extends JPanel {
    private final BaristaDao baristaDao = new BaristaDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final PerhitunganMagiqService magiqService = new PerhitunganMagiqService();

    private final JPanel tabelPanel = new JPanel();
    private final JLabel baristaTerbaikLabel = new JLabel("Barista terbaik: -");

    public PerhitunganMagiqPanel() {
        setLayout(new BorderLayout(10, 24));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEventPanel();
        muatDataAwal();
    }

    private void buatTampilan() {
        add(TampilanUtil.buatJudul("PROSES PERHITUNGAN MAGIQ"), BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 28));
        isiPanel.setBackground(Color.WHITE);
        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        tombolPanel.setBackground(Color.WHITE);
        JButton prosesButton = TampilanUtil.buatTombolAksi("Proses");
        prosesButton.addActionListener(e -> prosesPerhitungan());
        tombolPanel.add(prosesButton);

        tabelPanel.setLayout(new BoxLayout(tabelPanel, BoxLayout.Y_AXIS));
        tabelPanel.setBackground(Color.WHITE);
        isiPanel.add(tombolPanel, BorderLayout.NORTH);
        isiPanel.add(new JScrollPane(tabelPanel), BorderLayout.CENTER);
        isiPanel.add(baristaTerbaikLabel, BorderLayout.SOUTH);
        add(isiPanel, BorderLayout.CENTER);
    }

    private void pasangEventPanel() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                muatDataAwal();
            }
        });
    }

    private void muatDataAwal() {
        try {
            List<Barista> daftarBarista = baristaDao.ambilAktif();
            List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
            Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks();

            DefaultTableModel modelDataAwal = buatModelDataAwal(daftarBarista, daftarKriteria, matriksPenilaian);
            tampilkanTabelTunggal("Data Penilaian Awal", modelDataAwal);
            baristaTerbaikLabel.setText("Barista terbaik: -");
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private Object[] buatKolomDataAwal(List<Kriteria> daftarKriteria) {
        Object[] kolom = new Object[daftarKriteria.size() + 3];
        kolom[0] = "Kode Barista";
        kolom[1] = "Nama Barista";
        kolom[2] = "Jabatan";
        for (int i = 0; i < daftarKriteria.size(); i++) {
            kolom[i + 3] = daftarKriteria.get(i).getKode();
        }
        return kolom;
    }

    private void prosesPerhitungan() {
        try {
            PerhitunganDetail detail = magiqService.hitungDetailDanSimpan();
            tampilkanDetailPerhitungan(detail);
            List<HasilRanking> daftarHasilRanking = detail.getDaftarHasilRanking();
            if (!daftarHasilRanking.isEmpty()) {
                HasilRanking terbaik = daftarHasilRanking.get(0);
                baristaTerbaikLabel.setText("Barista terbaik: " + terbaik.getNamaBarista()
                        + " dengan nilai MAGIQ " + NumberUtil.format(terbaik.getNilaiMagiq()));
            }
            DialogUtil.showInfo(this, "Perhitungan MAGIQ berhasil dilakukan.");
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void tampilkanDetailPerhitungan(PerhitunganDetail detail) {
        tabelPanel.removeAll();
        tambahBagianTabel("1. Data Penilaian Barista", buatModelMatriksKeputusan(detail));
        tambahBagianTabel("2. Bobot ROC Kriteria", buatModelBobotKriteria(detail));
        tambahBagianTabel("3. Urutan Alternatif Pada Setiap Kriteria", buatModelUrutanAlternatif(detail));
        tambahBagianTabel("4. Skor Lokal Alternatif", buatModelSkorLokal(detail));
        tambahBagianTabel("5. Hasil Ranking MAGIQ", buatModelHasilRanking(detail.getDaftarHasilRanking()));
        tabelPanel.revalidate();
        tabelPanel.repaint();
    }

    private void tampilkanTabelTunggal(String judul, DefaultTableModel model) {
        tabelPanel.removeAll();
        tambahBagianTabel(judul, model);
        tabelPanel.revalidate();
        tabelPanel.repaint();
    }

    private void tambahBagianTabel(String judul, DefaultTableModel model) {
        JPanel bagianPanel = new JPanel(new BorderLayout(0, 8));
        bagianPanel.setBackground(Color.WHITE);
        bagianPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel judulLabel = new JLabel(judul);
        JTable tabelBagian = new JTable(model);
        tabelBagian.setAutoCreateRowSorter(true);
        TampilanUtil.rapikanTabel(tabelBagian);
        JScrollPane scrollPane = new JScrollPane(tabelBagian);
        int tinggi = Math.min(190, 58 + (model.getRowCount() * 24));
        scrollPane.setPreferredSize(new Dimension(900, Math.max(110, tinggi)));

        bagianPanel.add(judulLabel, BorderLayout.NORTH);
        bagianPanel.add(scrollPane, BorderLayout.CENTER);
        tabelPanel.add(bagianPanel);
    }

    private DefaultTableModel buatModelDataAwal(List<Barista> daftarBarista, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        DefaultTableModel model = buatModelTidakBisaEdit(buatKolomDataAwal(daftarKriteria));
        daftarBarista.stream().map((Barista barista) -> {
            Object[] baris = new Object[daftarKriteria.size() + 3];
            baris[0] = barista.getKodeBarista();
            baris[1] = barista.getNama();
            baris[2] = barista.getJabatan();
            Map<Integer, Double> nilaiBarista = matriksPenilaian.get(barista.getId());
            for (int i = 0; i < daftarKriteria.size(); i++) {
                Kriteria kriteria = daftarKriteria.get(i);
                Double nilai = nilaiBarista == null ? null : nilaiBarista.get(kriteria.getId());
                baris[i + 3] = nilai == null ? "-" : NumberUtil.format(nilai);
            }
            return baris;
        }).forEachOrdered((baris) -> {
            model.addRow(baris);
        });
        return model;
    }

    private DefaultTableModel buatModelMatriksKeputusan(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(buatKolomDataAwal(detail.getDaftarKriteria()));
        double[][] matriksKeputusan = detail.getMatriksKeputusan();
        for (int i = 0; i < detail.getDaftarBarista().size(); i++) {
            Barista barista = detail.getDaftarBarista().get(i);
            Object[] baris = new Object[detail.getDaftarKriteria().size() + 3];
            baris[0] = barista.getKodeBarista();
            baris[1] = barista.getNama();
            baris[2] = barista.getJabatan();
            for (int j = 0; j < detail.getDaftarKriteria().size(); j++) {
                baris[j + 3] = NumberUtil.format(matriksKeputusan[i][j]);
            }
            model.addRow(baris);
        }
        return model;
    }

    private DefaultTableModel buatModelBobotKriteria(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{
            "Urutan", "Kode", "Kriteria", "Bobot Awal", "Bobot ROC", "Tipe"
        });
        detail.getDaftarUrutanKriteria().forEach((Object[] data) -> {
            model.addRow(new Object[]{
                data[0],
                data[1],
                data[2],
                NumberUtil.format(((Double) data[3])),
                NumberUtil.format(((Double) data[4])),
                data[5]
            });
        });
        return model;
    }

    private DefaultTableModel buatModelUrutanAlternatif(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{
            "Kode", "Kriteria", "Tipe", "Urutan Alternatif"
        });
        detail.getDaftarUrutanAlternatif().forEach((data) -> {
            model.addRow(data);
        });
        return model;
    }

    private DefaultTableModel buatModelSkorLokal(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(buatKolomDataAwal(detail.getDaftarKriteria()));
        double[][] skorLokal = detail.getSkorLokal();
        for (int i = 0; i < detail.getDaftarBarista().size(); i++) {
            Barista barista = detail.getDaftarBarista().get(i);
            Object[] baris = new Object[detail.getDaftarKriteria().size() + 3];
            baris[0] = barista.getKodeBarista();
            baris[1] = barista.getNama();
            baris[2] = barista.getJabatan();
            for (int j = 0; j < detail.getDaftarKriteria().size(); j++) {
                baris[j + 3] = NumberUtil.format(skorLokal[i][j]);
            }
            model.addRow(baris);
        }
        return model;
    }

    private DefaultTableModel buatModelHasilRanking(List<HasilRanking> daftarHasilRanking) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{
            "Peringkat", "Kode Barista", "Nama Barista", "Nilai MAGIQ"
        });
        daftarHasilRanking.forEach((hasilRanking) -> {
            model.addRow(new Object[]{
                hasilRanking.getPeringkat(),
                hasilRanking.getKodeBarista(),
                hasilRanking.getNamaBarista(),
                NumberUtil.format(hasilRanking.getNilaiMagiq())
            });
        });
        return model;
    }

    private DefaultTableModel buatModelTidakBisaEdit(Object[] kolom) {
        return new DefaultTableModel(new Object[][]{}, kolom) {
            @Override
            public boolean isCellEditable(int baris, int kolomIndex) {
                return false;
            }
        };
    }
}
