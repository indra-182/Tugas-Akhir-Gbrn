package com.gibran.waroenkbikers.ui;

import com.gibran.waroenkbikers.dao.BaristaDao;
import com.gibran.waroenkbikers.dao.HasilRankingDao;
import com.gibran.waroenkbikers.dao.KriteriaDao;
import com.gibran.waroenkbikers.dao.PenilaianDao;
import com.gibran.waroenkbikers.model.Barista;
import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.util.DialogUtil;
import com.gibran.waroenkbikers.util.NumberUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

public class PenilaianPanel extends JPanel {
    private final BaristaDao baristaDao = new BaristaDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();

    private final JComboBox<Barista> baristaComboBox = new JComboBox<>();
    private final JTextField pencarianField = new TeksPlaceholderField(
            "Cari berdasarkan kode kriteria", 28);
    private final PenilaianTableModel tableModel = new PenilaianTableModel();
    private final JTable tabel = new JTable(tableModel);
    private final TableRowSorter<PenilaianTableModel> penyaringTabel = new TableRowSorter<>(tableModel);

    public PenilaianPanel() {
        setLayout(new BorderLayout(10, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEvent();
        muatBarista();
        muatKriteriaDanNilai();
    }

    private void buatTampilan() {
        add(TampilanUtil.buatJudul("DATA PENILAIAN"), BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 34));
        isiPanel.setBackground(Color.WHITE);
        isiPanel.add(buatPanelInput(), BorderLayout.NORTH);

        TampilanUtil.rapikanTabel(tabel);
        tabel.setRowSorter(penyaringTabel);
        TampilanUtil.pasangKolomNomor(tabel);
        isiPanel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(isiPanel, BorderLayout.CENTER);
    }

    private JPanel buatPanelInput() {
        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBackground(Color.WHITE);
        GridBagConstraints batas = new GridBagConstraints();
        batas.insets = new Insets(5, 5, 5, 5);
        batas.fill = GridBagConstraints.HORIZONTAL;

        batas.gridx = 0;
        batas.gridy = 0;
        batas.weightx = 0;
        panelInput.add(new JLabel("Barista"), batas);
        batas.gridx = 1;
        batas.weightx = 1;
        panelInput.add(baristaComboBox, batas);

        batas.gridx = 0;
        batas.gridy = 1;
        batas.weightx = 0;
        panelInput.add(new JLabel("Search Data"), batas);
        batas.gridx = 1;
        batas.weightx = 1;
        panelInput.add(pencarianField, batas);

        JButton ubahButton = TampilanUtil.buatTombolAksi("Ubah");
        JButton hapusButton = TampilanUtil.buatTombolAksi("Hapus");

        ubahButton.addActionListener(e -> simpanPenilaian());
        hapusButton.addActionListener(e -> DialogUtil.showInfo(this, "Isi nilai 0 pada tabel jika nilai penilaian ingin dikosongkan."));

        batas.weightx = 0;
        batas.gridx = 3;
        batas.gridy = 0;
        panelInput.add(ubahButton, batas);
        batas.gridx = 3;
        batas.gridy = 1;
        panelInput.add(hapusButton, batas);
        return panelInput;
    }

    private void pasangEvent() {
        baristaComboBox.addActionListener(e -> muatKriteriaDanNilai());
        pencarianField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saringData();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saringData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saringData();
            }
        });
    }

    private void saringData() {
        String kataKunci = pencarianField.getText().trim();
        if (kataKunci.isEmpty()) {
            penyaringTabel.setRowFilter(null);
        } else {
            final String pola = ".*" + Pattern.quote(kataKunci.toLowerCase()) + ".*";
            penyaringTabel.setRowFilter(new RowFilter<PenilaianTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends PenilaianTableModel, ? extends Integer> entry) {
                    String kode = String.valueOf(entry.getValue(1)).toLowerCase();
                    return kode.matches(pola);
                }
            });
        }
    }

    private void muatBarista() {
        try {
            baristaComboBox.removeAllItems();
            List<Barista> daftarBarista = baristaDao.ambilAktif();
            daftarBarista.forEach((barista) -> {
                baristaComboBox.addItem(barista);
            });
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void muatKriteriaDanNilai() {
        try {
            Barista baristaTerpilih = (Barista) baristaComboBox.getSelectedItem();
            List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
            Map<Integer, Double> daftarNilai;
            daftarNilai = baristaTerpilih == null
                    ? new java.util.HashMap<>()
                    : penilaianDao.ambilBerdasarkanBarista(baristaTerpilih.getId());
            tableModel.setData(daftarKriteria, daftarNilai);
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void simpanPenilaian() {
        Barista baristaTerpilih = (Barista) baristaComboBox.getSelectedItem();
        if (baristaTerpilih == null) {
            DialogUtil.showWarning(this, "Data barista aktif belum tersedia.");
            return;
        }
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Kriteria kriteria = tableModel.ambilKriteria(i);
                double nilai = tableModel.ambilNilai(i);
                penilaianDao.simpan(baristaTerpilih.getId(), kriteria.getId(), nilai);
            }
            hasilRankingDao.hapusSemua();
            DialogUtil.showInfo(this, "Penilaian berhasil disimpan.");
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private class PenilaianTableModel extends AbstractTableModel {
        private final String[] kolom;
        private List<Kriteria> daftarKriteria = new ArrayList<>();
        private List<Double> daftarNilai = new ArrayList<>();

        private PenilaianTableModel() {
            this.kolom = new String[]{"No", "Kode", "Kriteria", "Bobot", "Tipe", "Nilai"};
        }

        public void setData(List<Kriteria> daftarKriteria, Map<Integer, Double> nilaiTersimpan) {
            this.daftarKriteria = daftarKriteria;
            this.daftarNilai = new ArrayList<>();
            daftarKriteria.stream().map((kriteria) -> nilaiTersimpan.get(kriteria.getId())).forEachOrdered((nilai) -> {
                this.daftarNilai.add(nilai == null ? 0.0 : nilai);
            });
            fireTableDataChanged();
        }

        public Kriteria ambilKriteria(int baris) {
            return daftarKriteria.get(baris);
        }

        public double ambilNilai(int baris) {
            return daftarNilai.get(baris);
        }

        @Override
        public int getRowCount() {
            return daftarKriteria.size();
        }

        @Override
        public int getColumnCount() {
            return kolom.length;
        }

        @Override
        public String getColumnName(int kolomIndex) {
            return kolom[kolomIndex];
        }

        @Override
        public boolean isCellEditable(int baris, int kolomIndex) {
            return kolomIndex == 5;
        }

        @Override
        public Object getValueAt(int baris, int kolomIndex) {
            Kriteria kriteria = daftarKriteria.get(baris);
            switch (kolomIndex) {
                case 0: return baris + 1;
                case 1: return kriteria.getKode();
                case 2: return kriteria.getNama();
                case 3: return kriteria.getBobot();
                case 4: return kriteria.getTipe();
                case 5: return NumberUtil.format(daftarNilai.get(baris));
                default: return "";
            }
        }

        @Override
        public void setValueAt(Object nilaiBaru, int baris, int kolomIndex) {
            if (kolomIndex == 5) {
                try {
                    double nilai = NumberUtil.parseNonNegativeDouble(String.valueOf(nilaiBaru), "Nilai");
                    if (nilai > 100) {
                        throw new IllegalArgumentException("Nilai tidak boleh lebih dari 100.");
                    }
                    daftarNilai.set(baris, nilai);
                } catch (IllegalArgumentException ex) {
                    DialogUtil.showError(PenilaianPanel.this, ex.getMessage());
                }
                fireTableCellUpdated(baris, kolomIndex);
            }
        }
    }
}
