# Benefit-only Criteria, Data Acuan, dan Preview Laporan

## Tujuan

Menyederhanakan penilaian MAGIQ agar seluruh kriteria menggunakan normalisasi
benefit, menyelaraskan data awal dengan dokumen penelitian, dan memberi
pengguna preview laporan sebelum mencetak atau menyimpan PDF.

## Ruang Lingkup

1. Menghapus konsep `BENEFIT`/`COST` secara menyeluruh dari aplikasi.
2. Mengganti data awal menjadi tiga alternatif: `Barista A`, `Barista B`, dan
   `Barista C`, dengan nilai dari `BAB 2-3 Rehan 11 Juli 2026.docx`.
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

Data awal menggunakan prioritas dan nilai berikut.

| Kode | Kriteria | Prioritas | A | B | C |
| --- | --- | ---: | ---: | ---: | ---: |
| C1 | Rasa Kopi | 1 | 90 | 85 | 88 |
| C2 | Aroma | 2 | 85 | 90 | 82 |
| C3 | Konsistensi Racikan | 3 | 88 | 84 | 90 |
| C4 | Penyajian | 4 | 90 | 88 | 85 |
| C5 | Kecepatan Penyajian | 5 | 80 | 90 | 85 |
| C6 | Stabilitas Suhu Penyajian | 6 | 85 | 90 | 88 |

Kode barista tetap B001, B002, dan B003. Namanya persis `Barista A`, `Barista
B`, dan `Barista C`; statusnya AKTIF dan jabatannya Barista. Bobot ROC dihitung
otomatis dari prioritas, bukan menjadi nilai yang dapat diinput manual.

## Migrasi Database

Skrip inisialisasi penuh akan dibuat konsisten dengan skema tanpa `tipe` dan
data awal di atas. Migrasi terpisah untuk database yang sudah berjalan akan
menjatuhkan constraint `tipe` bila ada, lalu menghapus kolom `tipe` bila ada.
Migrasi tidak menghapus barista atau penilaian pengguna yang sudah tersimpan.

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
masukan API tidak lagi menerima flag cost. Pengujian perhitungan memakai matriks
acuan dan memverifikasi ranking akhir A, B, C. Build Ant dijalankan setelah
perubahan untuk memeriksa seluruh source Java dan test yang tersedia.
