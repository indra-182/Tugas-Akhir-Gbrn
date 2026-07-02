package com.gibran.waroenkbikers.ui;

import com.gibran.waroenkbikers.dao.HasilRankingDao;
import com.gibran.waroenkbikers.dao.BaristaDao;
import com.gibran.waroenkbikers.dao.KriteriaDao;
import com.gibran.waroenkbikers.dao.PenilaianDao;
import com.gibran.waroenkbikers.model.HasilRanking;
import com.gibran.waroenkbikers.model.Barista;
import com.gibran.waroenkbikers.model.Kriteria;
import com.gibran.waroenkbikers.service.PerhitunganMagiqService;
import com.gibran.waroenkbikers.util.DialogUtil;
import com.gibran.waroenkbikers.util.NumberUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LaporanPanel extends JPanel {
    private static final String LOKASI_LOGO = "C:\\Users\\mahad\\Downloads\\waroenk-bikers.jpg";
    private static final String NAMA_PERUSAHAAN = "WAROENK BIKERS";
    private static final String ALAMAT_BARIS_1 = "Jl. D. Tondano No.3, RT.1/RW.4, Bend. Hilir";
    private static final String ALAMAT_BARIS_2 = "Kec. Tanah Abang, Kota Jakarta Pusat";
    private static final String ALAMAT_BARIS_3 = "RT.11/RW.6, Bend. Hilir, Kecamatan Tanah Abang";
    private static final String ALAMAT_BARIS_4 = "Jakarta, Daerah Khusus Ibukota Jakarta 10210";
    private static final String TELEPON = "Telepon: -";
    private static final String NAMA_PENANDATANGAN = "Pimpinan Waroenk Bikers";

    private static final String LAPORAN_DATA_RANKING = "Laporan Data Ranking";
    private static final String LAPORAN_DATA_BARISTA = "Laporan Data Barista";
    private static final String LAPORAN_DATA_KRITERIA = "Laporan Data Kriteria";
    private static final String LAPORAN_DATA_PENILAIAN = "Laporan Data Penilaian";

    private final BaristaDao baristaDao = new BaristaDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();

    private final JComboBox<String> jenisLaporanComboBox = new JComboBox<>(new String[]{
        LAPORAN_DATA_RANKING,
        LAPORAN_DATA_BARISTA,
        LAPORAN_DATA_KRITERIA,
        LAPORAN_DATA_PENILAIAN
    });
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable tabel = new JTable(tableModel);
    private final JPanel judulPanel = new JPanel(new BorderLayout());

    public LaporanPanel() {
        setLayout(new BorderLayout(10, 22));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEventPanel();
        muatLaporan();
    }

    private void buatTampilan() {
        judulPanel.setBackground(Color.WHITE);
        add(judulPanel, BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 18));
        isiPanel.setBackground(Color.WHITE);
        isiPanel.add(buatPanelAksi(), BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        TampilanUtil.rapikanTabel(tabel);
        isiPanel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(isiPanel, BorderLayout.CENTER);
    }

    private JPanel buatPanelAksi() {
        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelAksi.setBackground(Color.WHITE);
        JButton cetakButton = TampilanUtil.buatTombolAksi("Tambah");
        cetakButton.setText("Cetak");

        jenisLaporanComboBox.addActionListener(e -> muatLaporan());
        cetakButton.addActionListener(e -> cetakLaporanPdf());

        panelAksi.add(jenisLaporanComboBox);
        panelAksi.add(cetakButton);
        return panelAksi;
    }

    private void pasangEventPanel() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                jenisLaporanComboBox.setSelectedItem(LAPORAN_DATA_RANKING);
                muatLaporan();
            }
        });
    }

    private void muatLaporan() {
        try {
            String jenis = jenisLaporanComboBox.getSelectedItem().toString();
            judulPanel.removeAll();
            judulPanel.add(TampilanUtil.buatJudul(jenis.toUpperCase()), BorderLayout.CENTER);

            switch (jenis) {
                case LAPORAN_DATA_BARISTA:
                    muatLaporanBarista();
                    break;
                case LAPORAN_DATA_KRITERIA:
                    muatLaporanKriteria();
                    break;
                case LAPORAN_DATA_PENILAIAN:
                    muatLaporanPenilaian();
                    break;
                default:
                    muatLaporanRanking();
                    break;
            }

            judulPanel.revalidate();
            judulPanel.repaint();
        } catch (Exception ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void muatLaporanBarista() throws Exception {
        tableModel.setDataVector(new Object[][]{}, new Object[]{"Kode Barista", "Nama", "Jabatan", "Status"});
        List<Barista> daftarBarista = baristaDao.ambilSemua();
        daftarBarista.forEach((barista) -> {
            tableModel.addRow(new Object[]{
                barista.getKodeBarista(),
                barista.getNama(),
                barista.getJabatan(),
                barista.getStatus()
            });
        });
    }

    private void muatLaporanKriteria() throws Exception {
        tableModel.setDataVector(new Object[][]{}, new Object[]{"Kode", "Nama Kriteria", "Bobot", "Jenis"});
        List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
        daftarKriteria.forEach((kriteria) -> {
            tableModel.addRow(new Object[]{
                kriteria.getKode(),
                kriteria.getNama(),
                kriteria.getBobot(),
                kriteria.getTipe()
            });
        });
    }

    private void muatLaporanPenilaian() throws Exception {
        tableModel.setDataVector(new Object[][]{}, new Object[]{
            "Kode Barista", "Nama Barista", "Kode Kriteria", "Kriteria", "Nilai"
        });
        List<Object[]> data = penilaianDao.ambilLaporanPenilaian();
        data.forEach((baris) -> {
            tableModel.addRow(baris);
        });
    }

    private void muatLaporanRanking() throws Exception {
        if (!PerhitunganMagiqService.apakahPerhitunganSudahDiproses()) {
            tampilkanPesanProsesPerhitungan();
            return;
        }

        List<HasilRanking> daftarRanking = hasilRankingDao.ambilSemua();
        if (daftarRanking.isEmpty()) {
            tampilkanPesanProsesPerhitungan();
            return;
        }

        tableModel.setDataVector(new Object[][]{}, new Object[]{
            "Peringkat", "Kode Barista", "Nama Barista", "Nilai MAGIQ"
        });
        daftarRanking.forEach((ranking) -> {
            tableModel.addRow(new Object[]{
                ranking.getPeringkat(),
                ranking.getKodeBarista(),
                ranking.getNamaBarista(),
                NumberUtil.format(ranking.getNilaiMagiq())
            });
        });
    }

    private void tampilkanPesanProsesPerhitungan() {
        tableModel.setDataVector(new Object[][]{
            {"Proses Perhitungan Dahulu untuk Menampilkan Data"}
        }, new Object[]{"Informasi"});
    }

    private void cetakLaporanPdf() {
        String jenis = jenisLaporanComboBox.getSelectedItem().toString();
        if (LAPORAN_DATA_RANKING.equals(jenis) && !PerhitunganMagiqService.apakahPerhitunganSudahDiproses()) {
            DialogUtil.showWarning(this, "Proses Perhitungan Dahulu di Menu Perhitungan");
            return;
        }

        if (tableModel.getRowCount() == 0) {
            DialogUtil.showWarning(this, "Belum ada data laporan untuk dicetak.");
            return;
        }

        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName(jenisLaporanComboBox.getSelectedItem().toString());
            printerJob.setPrintable(new CetakLaporanPrintable());
            if (printerJob.printDialog()) {
                printerJob.print();
            }
        } catch (HeadlessException | PrinterException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private class CetakLaporanPrintable implements Printable {
        private static final int MARGIN_KONTEN = 20;
        private static final int TINGGI_BARIS = 18;
        private static final int TINGGI_HEADER_TABEL = 22;
        private static final int LEBAR_LOGO = 90;
        private static final int TINGGI_LOGO = 75;

        private final Image logo = muatLogo();
        private final Font fontJudul = new Font("Serif", Font.BOLD, 14);
        private final Font fontSubJudul = new Font("Serif", Font.BOLD, 12);
        private final Font fontNormal = new Font("Serif", Font.PLAIN, 10);
        private final Font fontTabel = new Font("SansSerif", Font.PLAIN, 8);
        private final Font fontTabelHeader = new Font("SansSerif", Font.BOLD, 8);

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            Graphics2D grafik = (Graphics2D) graphics;
            int xAwal = (int) pageFormat.getImageableX() + MARGIN_KONTEN;
            int yAwal = (int) pageFormat.getImageableY() + MARGIN_KONTEN;
            int lebarKonten = (int) pageFormat.getImageableWidth() - (MARGIN_KONTEN * 2);
            int tinggiKonten = (int) pageFormat.getImageableHeight() - (MARGIN_KONTEN * 2);
            int yTabel = gambarKopLaporan(grafik, xAwal, yAwal, lebarKonten);
            int tinggiAreaTabel = tinggiKonten - (yTabel - yAwal) - 150;
            int jumlahBarisPerHalaman = Math.max(1, (tinggiAreaTabel - TINGGI_HEADER_TABEL) / TINGGI_BARIS);
            int barisMulai = pageIndex * jumlahBarisPerHalaman;

            if (barisMulai >= tableModel.getRowCount()) {
                return NO_SUCH_PAGE;
            }

            int barisAkhir = Math.min(tableModel.getRowCount(), barisMulai + jumlahBarisPerHalaman);
            gambarTabel(grafik, xAwal, yTabel, lebarKonten, barisMulai, barisAkhir);

            if (barisAkhir >= tableModel.getRowCount()) {
                gambarTandaTangan(grafik, xAwal, yAwal + tinggiKonten - 130, lebarKonten);
            }
            return PAGE_EXISTS;
        }

        private Image muatLogo() {
            try {
                File fileLogo = new File(LOKASI_LOGO);
                return fileLogo.exists() ? ImageIO.read(fileLogo) : null;
            } catch (IOException ex) {
                return null;
            }
        }

        private int gambarKopLaporan(Graphics2D grafik, int xAwal, int yAwal, int lebarKonten) {
            grafik.setColor(Color.BLACK);
            grafik.drawRect(xAwal - 10, yAwal - 10, lebarKonten + 20, 760);

            if (logo != null) {
                grafik.drawImage(logo, xAwal + 18, yAwal + 8, LEBAR_LOGO, TINGGI_LOGO, null);
            }

            int tengah = xAwal + (lebarKonten / 2);
            int y = yAwal + 18;
            gambarTeksTengah(grafik, NAMA_PERUSAHAAN, tengah, y, fontJudul);
            y += 16;
            gambarTeksTengah(grafik, ALAMAT_BARIS_1, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, ALAMAT_BARIS_2, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, ALAMAT_BARIS_3, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, ALAMAT_BARIS_4, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, TELEPON, tengah, y, fontNormal);
            y += 34;
            gambarTeksTengah(grafik, jenisLaporanComboBox.getSelectedItem().toString().toUpperCase(), tengah, y, fontSubJudul);
            return y + 20;
        }

        private void gambarTabel(Graphics2D grafik, int xAwal, int yAwal, int lebarKonten, int barisMulai, int barisAkhir) {
            int jumlahKolom = tableModel.getColumnCount();
            int lebarKolom = Math.max(35, lebarKonten / jumlahKolom);
            int y = yAwal;

            grafik.setFont(fontTabelHeader);
            grafik.setColor(new Color(248, 203, 203));
            grafik.fillRect(xAwal, y, lebarKolom * jumlahKolom, TINGGI_HEADER_TABEL);
            grafik.setColor(Color.BLACK);

            for (int kolom = 0; kolom < jumlahKolom; kolom++) {
                int x = xAwal + (kolom * lebarKolom);
                grafik.drawRect(x, y, lebarKolom, TINGGI_HEADER_TABEL);
                gambarTeksPotong(grafik, tableModel.getColumnName(kolom), x + 3, y + 14, lebarKolom - 6);
            }

            grafik.setFont(fontTabel);
            y += TINGGI_HEADER_TABEL;
            for (int baris = barisMulai; baris < barisAkhir; baris++) {
                for (int kolom = 0; kolom < jumlahKolom; kolom++) {
                    int x = xAwal + (kolom * lebarKolom);
                    grafik.drawRect(x, y, lebarKolom, TINGGI_BARIS);
                    Object nilai = tableModel.getValueAt(baris, kolom);
                    gambarTeksPotong(grafik, String.valueOf(nilai), x + 3, y + 13, lebarKolom - 6);
                }
                y += TINGGI_BARIS;
            }
        }

        private void gambarTandaTangan(Graphics2D grafik, int xAwal, int yAwal, int lebarKonten) {
            int lebarAreaTandaTangan = 190;
            int xTandaTangan = xAwal + lebarKonten - lebarAreaTandaTangan - 12;
            String tanggal = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID")).format(new Date());

            grafik.setFont(fontNormal);
            grafik.setColor(Color.BLACK);
            gambarTeksTengah(grafik, "Jakarta, " + tanggal, xTandaTangan + (lebarAreaTandaTangan / 2), yAwal, fontNormal);
            gambarTeksTengah(grafik, "Mengetahui,", xTandaTangan + (lebarAreaTandaTangan / 2), yAwal + 32, fontNormal);
            gambarTeksTengah(grafik, NAMA_PENANDATANGAN, xTandaTangan + (lebarAreaTandaTangan / 2), yAwal + 116, fontNormal);
        }

        private void gambarTeksTengah(Graphics2D grafik, String teks, int tengah, int y, Font font) {
            grafik.setFont(font);
            int lebarTeks = grafik.getFontMetrics().stringWidth(teks);
            grafik.drawString(teks, tengah - (lebarTeks / 2), y);
        }

        private void gambarTeksPotong(Graphics2D grafik, String teks, int x, int y, int lebarMaksimum) {
            if (teks == null) {
                teks = "";
            }
            String teksTampil = teks;
            while (grafik.getFontMetrics().stringWidth(teksTampil) > lebarMaksimum && teksTampil.length() > 3) {
                teksTampil = teksTampil.substring(0, teksTampil.length() - 4) + "...";
            }
            grafik.drawString(teksTampil, x, y);
        }
    }
}
