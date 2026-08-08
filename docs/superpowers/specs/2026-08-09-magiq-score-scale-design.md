# Perbaikan Skala Nilai MAGIQ — Desain

## Tujuan

Menyelaraskan skala penilaian pada aplikasi, seed database, data Excel, dan BAB II–IV sehingga setiap kriteria memakai skala maksimum 100, sementara nilai primer tetap sama dengan data pada BAB II–III dan data dummy C3 tidak lagi hanya berisi 60 dan 65.

## Keputusan Desain

1. Normalisasi aplikasi memakai maksimum skala tetap 100 untuk setiap kriteria: `nilai_normalisasi = nilai / 100`.
2. Data primer tetap memakai nilai BAB II–III:
   - Barista A: 90, 85, 88, 90, 80, 85
   - Barista B: 85, 90, 84, 88, 90, 90
   - Barista C: 88, 82, 90, 85, 85, 88
   Semua nilai tersebut dinormalisasi dengan pembagi 100 agar tabel primer memiliki maksimum skala 100.
3. Seed dummy C3 diubah menjadi pola berulang 60, 65, 70, 75, 80, 85, 90, 95, 100 melalui `60 + 5 * (i % 9)`. Dengan 100 data, maksimum aktual C3 juga menjadi 100.
4. Bobot ROC, urutan kriteria, 100 barista, dan tie-break ranking tetap mengikuti aplikasi existing.
5. Excel diperbarui dengan data C3 baru dan rumus maksimum/normalisasi yang menunjukkan 100 untuk C1–C6.
6. BAB II–III dan BAB IV diperbarui pada tabel/perhitungan yang terdampak. Narasi tetap tidak menyebut jenis kriteria.
7. Gambar tangkapan layar BAB IV hanya diganti bila menampilkan nilai, tabel, atau hasil perhitungan yang berubah; warna/layout aplikasi tetap mengikuti rancangan yang telah disetujui.

## Alur Data

`seed SQL / data input` → `perhitungan aplikasi (normalisasi ÷ 100)` → `hasil Excel formula` → `tabel dan tangkapan layar BAB II–IV`.

## Pengujian dan Penerimaan

- Unit test gagal sebelum perubahan untuk kasus nilai 75 dengan data kolom tanpa nilai 100, lalu lulus setelah normalisasi memakai 100.
- Build/test Java berhasil tanpa error compile.
- Seed C3 menghasilkan seluruh level 60–100 dan maksimum 100.
- Workbook menunjukkan maksimum 100 pada E4:E9, rumus perhitungan tetap aktif, dan tidak ada error formula.
- Nilai primer, nilai dummy, ranking, tabel BAB IV, dan gambar yang ditampilkan saling konsisten.
- DOCX dirender dengan `render_docx.py`; seluruh halaman hasil akhir diperiksa visualnya untuk clipping, tabel berpindah, gambar terpotong, atau layout rusak.

