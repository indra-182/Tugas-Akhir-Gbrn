# Benefit-only Criteria, Data Acuan, dan Preview Laporan

## Tujuan

Menyederhanakan penilaian MAGIQ agar seluruh kriteria menggunakan normalisasi
benefit, menyelaraskan data awal dengan dokumen penelitian, dan memberi
pengguna preview laporan sebelum mencetak atau menyimpan PDF.

## Ruang Lingkup

1. Menghapus konsep `BENEFIT`/`COST` secara menyeluruh dari aplikasi.
2. Mengganti data awal menjadi 100 alternatif barista dengan nilai penilaian
   deterministik dalam rentang 60–100 dan seluruhnya kelipatan 5.
3. Menambahkan preview native Swing pada alur cetak laporan.
4. Menampilkan hari dan tanggal pembuatan pada laporan.

## Data dan Perhitungan

Skema `kriteria` tidak lagi memiliki kolom `tipe`, constraint tipe, atau nilai
default tipe. Model, DAO, form kriteria, laporan kriteria, detail perhitungan,
dan validasi tidak lagi mengirim, menyimpan, atau menampilkan tipe kriteria.

Setiap kolom penilaian dinormalisasi dengan rumus berikut:

```
r_ik = x_ik / max(x_k)
```

Data awal menggunakan 100 alternatif dengan kode B001 hingga B100. Urutan nama
adalah `Barista A` hingga `Barista Z`, kemudian `Barista AA` hingga `Barista
ZZ`, `Barista AAA` hingga `Barista ZZZ`, lalu `Barista AAAA` hingga `Barista
VVVV`. Seluruhnya berstatus AKTIF, divisi Bar, dan jabatan Barista.

Kriteria dan prioritas tetap C1 Rasa Kopi (1), C2 Aroma (2), C3 Konsistensi
Racikan (3), C4 Penyajian (4), C5 Kecepatan Penyajian (5), dan C6 Stabilitas
Suhu Penyajian (6). Bobot ROC dihitung otomatis dari prioritas, bukan menjadi
nilai yang dapat diinput manual.

Untuk alternatif indeks nol-based `i` (0 sampai 99), nilai seed ditentukan
secara deterministik dengan rumus berikut; setiap hasil pasti salah satu dari
60, 65, 70, 75, 80, 85, 90, 95, atau 100.

```
C1 = 60 + 5 * ( i        % 9)
C2 = 60 + 5 * ((i / 9)  % 9)
C3 = 60 + 5 * ((i / 81) % 9)
C4 = 60 + 5 * ((i * 5 + 2) % 9)
C5 = 60 + 5 * ((i * 7 + 4) % 9)
C6 = 60 + 5 * ((i * 8 + 6) % 9)
```

Aturan rentang dan kelipatan 5 yang terbaru menggantikan nilai A–C dari dokumen
acuan sebelumnya.

## Migrasi Database

Skrip inisialisasi penuh akan dibuat konsisten dengan skema tanpa `tipe` dan
data awal 100 barista di atas. Migrasi terpisah untuk database yang sudah
berjalan akan menjatuhkan constraint `tipe` bila ada, lalu menghapus kolom
`tipe` bila ada. Migrasi tidak menghapus barista atau penilaian pengguna yang
sudah tersimpan.

File Excel `output/data-barista-dan-penilaian.xlsx` menyediakan satu sheet
`Data Barista dan Penilaian`: satu baris per barista dengan kolom kode, nama,
divisi, jabatan, status, serta C1 hingga C6.

## Preview dan Cetak

Tombol Cetak menampilkan dialog modal Swing yang merender halaman laporan
menggunakan `Printable` yang sama dengan proses cetak. Preview menyediakan
tombol untuk melanjutkan ke dialog printer/Save PDF dan tombol Tutup. Dengan
demikian tampilan yang diperiksa pengguna adalah tampilan yang dicetak.

Baris lokasi/tanggal pada area tanda tangan menggunakan locale Indonesia dan
menampilkan hari, misalnya: `Jakarta, Minggu 8 Agustus 2026`. Hari dan tanggal
diambil saat pengguna memulai proses cetak/preview; tidak berubah di antara
preview dan hasil cetak.

Validasi laporan ranking tetap mewajibkan perhitungan MAGIQ tersedia sebelum
preview atau cetak dapat dilakukan.

## Pengujian

Pengujian unit normalisasi membuktikan bahwa nilai dibagi maksimum kolom dan
masukan API tidak lagi menerima flag cost. Pengujian data seed memverifikasi 100
barista, enam penilaian per barista, rentang 60–100, dan kelipatan 5. File Excel
divalidasi memiliki satu sheet dan memuat tepat 100 baris data.
Build Ant dijalankan setelah perubahan untuk memeriksa seluruh source Java dan
test yang tersedia.
