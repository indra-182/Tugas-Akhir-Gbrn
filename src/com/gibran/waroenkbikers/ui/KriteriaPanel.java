package com.gibran.waroenkbikers.ui;

import com.gibran.waroenkbikers.dao.KriteriaDao;
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

public class KriteriaPanel extends JPanel {
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final KriteriaTableModel tableModel = new KriteriaTableModel();
    private final JTable tabel = new JTable(tableModel);

    private final JTextField kodeField = new JTextField(18);
    private final JTextField namaField = new JTextField(18);
    private final JTextField bobotField = new JTextField(18);
    private final JTextField pencarianField = new TeksPlaceholderField(
            "Cari berdasarkan kode kriteria", 18);
    private final JComboBox<String> tipeComboBox = new JComboBox<>(new String[]{Kriteria.BENEFIT, Kriteria.COST});
    private final TableRowSorter<KriteriaTableModel> penyaringTabel = new TableRowSorter<>(tableModel);

    private int idTerpilih = 0;

    public KriteriaPanel() {
        setLayout(new BorderLayout(10, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEvent();
        muatData();
    }

    private void buatTampilan() {
        add(TampilanUtil.buatJudul("DATA KRITERIA"), BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 34));
        isiPanel.setBackground(Color.WHITE);
        isiPanel.add(buatPanelInput(), BorderLayout.NORTH);

        TampilanUtil.rapikanTabel(tabel);
        tabel.setRowSorter(penyaringTabel);
        isiPanel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(isiPanel, BorderLayout.CENTER);
    }

    private JPanel buatPanelInput() {
        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBackground(Color.WHITE);
        GridBagConstraints panelBatas = new GridBagConstraints();
        panelBatas.insets = new Insets(0, 18, 0, 18);
        panelBatas.anchor = GridBagConstraints.NORTH;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints batas = new GridBagConstraints();
        batas.insets = new Insets(5, 5, 5, 5);
        batas.fill = GridBagConstraints.HORIZONTAL;

        tambahField(formPanel, batas, 0, "Kode", kodeField);
        tambahField(formPanel, batas, 1, "Nama Kriteria", namaField);
        tambahField(formPanel, batas, 2, "Bobot", bobotField);
        tambahField(formPanel, batas, 3, "Jenis", tipeComboBox);
        tambahField(formPanel, batas, 4, "Search Data", pencarianField);

        JPanel tombolPanel = new JPanel(new GridBagLayout());
        tombolPanel.setBackground(Color.WHITE);
        GridBagConstraints tombolBatas = new GridBagConstraints();
        tombolBatas.insets = new Insets(5, 5, 5, 5);
        JButton tambahButton = TampilanUtil.buatTombolAksi("Tambah");
        JButton simpanButton = TampilanUtil.buatTombolAksi("Simpan");
        JButton ubahButton = TampilanUtil.buatTombolAksi("Ubah");
        JButton hapusButton = TampilanUtil.buatTombolAksi("Hapus");

        tambahButton.addActionListener(e -> bersihkanForm());
        simpanButton.addActionListener(e -> simpan());
        ubahButton.addActionListener(e -> ubah());
        hapusButton.addActionListener(e -> hapus());

        tombolBatas.gridx = 0;
        tombolBatas.gridy = 0;
        tombolPanel.add(tambahButton, tombolBatas);
        tombolBatas.gridx = 1;
        tombolPanel.add(simpanButton, tombolBatas);
        tombolBatas.gridx = 2;
        tombolPanel.add(ubahButton, tombolBatas);
        tombolBatas.gridx = 0;
        tombolBatas.gridy = 1;
        tombolPanel.add(hapusButton, tombolBatas);

        panelBatas.gridx = 0;
        panelBatas.gridy = 0;
        panelInput.add(formPanel, panelBatas);
        panelBatas.gridx = 1;
        panelInput.add(tombolPanel, panelBatas);
        return panelInput;
    }

    private void tambahField(JPanel panel, GridBagConstraints batas, int baris, String label, java.awt.Component field) {
        batas.gridx = 0;
        batas.gridy = baris;
        batas.weightx = 0;
        panel.add(new JLabel(label), batas);
        batas.gridx = 1;
        batas.weightx = 1;
        panel.add(field, batas);
    }

    private void pasangEvent() {
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

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabel.getSelectedRow() >= 0) {
                int baris = tabel.convertRowIndexToModel(tabel.getSelectedRow());
                Kriteria kriteria = tableModel.ambilKriteria(baris);
                idTerpilih = kriteria.getId();
                kodeField.setText(kriteria.getKode());
                namaField.setText(kriteria.getNama());
                bobotField.setText(String.valueOf(kriteria.getBobot()));
                tipeComboBox.setSelectedItem(kriteria.getTipe());
            }
        });
    }

    private void saringData() {
        String kataKunci = pencarianField.getText().trim();
        if (kataKunci.isEmpty()) {
            penyaringTabel.setRowFilter(null);
        } else {
            final String pola = ".*" + Pattern.quote(kataKunci.toLowerCase()) + ".*";
            penyaringTabel.setRowFilter(new RowFilter<KriteriaTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends KriteriaTableModel, ? extends Integer> entry) {
                    String kode = String.valueOf(entry.getValue(1)).toLowerCase();
                    String namaKriteria = String.valueOf(entry.getValue(2)).toLowerCase();
                    return kode.matches(pola) || namaKriteria.matches(pola);
                }
            });
        }
    }

    private void muatData() {
        try {
            tableModel.setData(kriteriaDao.ambilSemua());
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void simpan() {
        try {
            kriteriaDao.tambah(bacaForm());
            DialogUtil.showInfo(this, "Data kriteria berhasil disimpan.");
            bersihkanForm();
            muatData();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void ubah() {
        if (idTerpilih == 0) {
            DialogUtil.showWarning(this, "Pilih data yang ingin diubah.");
            return;
        }
        try {
            Kriteria kriteria = bacaForm();
            kriteria.setId(idTerpilih);
            kriteriaDao.ubah(kriteria);
            DialogUtil.showInfo(this, "Data kriteria berhasil diubah.");
            bersihkanForm();
            muatData();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            DialogUtil.showWarning(this, "Pilih data yang ingin dihapus.");
            return;
        }
        if (!DialogUtil.confirm(this, "Hapus data kriteria terpilih?")) {
            return;
        }
        try {
            kriteriaDao.hapus(idTerpilih);
            DialogUtil.showInfo(this, "Data kriteria berhasil dihapus.");
            bersihkanForm();
            muatData();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private Kriteria bacaForm() {
        if (kodeField.getText().trim().isEmpty()
                || namaField.getText().trim().isEmpty()
                || bobotField.getText().trim().isEmpty()
                || tipeComboBox.getSelectedItem() == null
                || tipeComboBox.getSelectedItem().toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Semua field data kriteria wajib diisi.");
        }
        Kriteria kriteria = new Kriteria();
        kriteria.setKode(kodeField.getText().trim());
        kriteria.setNama(namaField.getText().trim());
        kriteria.setBobot(NumberUtil.parsePositiveDouble(bobotField.getText(), "Bobot"));
        kriteria.setTipe(tipeComboBox.getSelectedItem().toString());
        kriteria.setKeterangan("");
        return kriteria;
    }

    private void bersihkanForm() {
        idTerpilih = 0;
        kodeField.setText("");
        namaField.setText("");
        bobotField.setText("");
        tipeComboBox.setSelectedIndex(0);
        tabel.clearSelection();
    }

    private static class KriteriaTableModel extends AbstractTableModel {
        private final String[] kolom = {"ID", "Kode", "Nama Kriteria", "Bobot", "Jenis"};
        private List<Kriteria> data = new ArrayList<>();

        public void setData(List<Kriteria> data) {
            this.data = data;
            fireTableDataChanged();
        }

        public Kriteria ambilKriteria(int baris) {
            return data.get(baris);
        }

        @Override
        public int getRowCount() {
            return data.size();
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
        public Object getValueAt(int baris, int kolomIndex) {
            Kriteria kriteria = data.get(baris);
            switch (kolomIndex) {
                case 0: return kriteria.getId();
                case 1: return kriteria.getKode();
                case 2: return kriteria.getNama();
                case 3: return kriteria.getBobot();
                case 4: return kriteria.getTipe();
                default: return "";
            }
        }
    }
}
