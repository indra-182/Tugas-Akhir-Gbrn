# Desain Penyelarasan BAB IV dan App MAGIQ Waroenk Bikers

## Tujuan

Menyelaraskan aplikasi, database, artefak data, dan file `BAB_IV - Waroenk Bikers.docx` agar seluruh isi BAB IV menggambarkan perilaku aplikasi existing setelah penghapusan atribut jabatan, divisi, dan status dari data barista.

## Ruang Lingkup

### 1. App dan database

- Menghapus field `jabatan`, `divisi`, dan `status` dari model `Barista`.
- Menghapus seluruh consumer ketiga field tersebut dari DAO, form dan tabel UI, panel perhitungan, laporan, seed SQL, migration, serta artefak data ekspor.
- Mempertahankan identitas barista melalui `id`, `kode_barista`, dan `nama`.
- Mempertahankan alur 100 data dummy barista dan 6 kriteria yang digunakan aplikasi.
- Memastikan source dapat dikompilasi tanpa error setelah perubahan.

### 2. Point B BAB IV

Point B akan memuat dua perhitungan lengkap dengan struktur dan rumus yang mengikuti aplikasi:

1. **Data primer 3 alternatif**: menggunakan data Barista A, B, dan C dari file pembanding `BAB 2-3 Rehan 11 Juli 2026.docx`. Seluruh baris matriks keputusan, normalisasi, nilai preferensi, dan ranking ditampilkan.
2. **Data dummy 100 alternatif**: menggunakan dataset deterministik 100 barista dari seed aplikasi. Seluruh 100 baris data alternatif, matriks keputusan, hasil normalisasi, nilai preferensi, dan ranking ditampilkan.

Narasi dan tabel akan menjelaskan enam kriteria, bobot ROC otomatis, normalisasi dengan pembagian nilai terhadap nilai maksimum tiap kolom, nilai preferensi MAGIQ, dan pemeringkatan. Dokumen tidak menyebut jenis kriteria, BENEFIT, COST, atau atribut kriteria yang sudah dihapus dari aplikasi.

### 3. Konsistensi teknis dan narasi

- Mengganti referensi MySQL menjadi PostgreSQL/Supabase sesuai app.
- Mengganti asumsi lima barista menjadi 100 barista untuk dataset aplikasi.
- Menghapus referensi jabatan, divisi, dan status dari narasi, tabel, laporan, mockup, dan screenshot.
- Menyesuaikan deskripsi preview laporan dengan alur aplikasi existing.

### 4. Gambar BAB IV

- Memperbarui seluruh Gambar 4.17–4.28 sebagai rancangan layar yang mengikuti layout app terbaru, dengan gaya garis/monokrom yang dipertahankan dari dokumen saat ini.
- Memperbarui seluruh Gambar 4.29–4.38 sebagai tangkapan layar app existing setelah perubahan.
- Menghapus kolom/label yang tidak lagi ada dan memperbarui angka dashboard serta isi tabel agar konsisten dengan 100 data dummy.

## Strategi Implementasi

1. Membuat backup bertimestamp dari DOCX target sebelum perubahan dokumen.
2. Mengubah source Java dan SQL secara terarah, menambahkan migration penghapusan kolom jika diperlukan, lalu mengompilasi project.
3. Menghasilkan ulang dataset/perhitungan dengan rumus yang sama dengan app sebagai sumber tabel BAB IV.
4. Menghasilkan mockup dan menangkap layar aplikasi dari state existing setelah app berhasil dijalankan.
5. Mengganti gambar dan teks/table Point B secara lokal pada DOCX agar struktur akademik dan format asli tetap terjaga.
6. Merender DOCX dan memeriksa seluruh halaman, termasuk page break dan tabel 100 baris.

## Verifikasi

- `rg` tidak menemukan consumer produksi untuk `getJabatan`, `setJabatan`, `getDivisi`, `setDivisi`, `getStatus`, `setStatus`, atau kolom SQL terkait pada source/seed/migration yang menjadi bagian aplikasi.
- Project Java berhasil dikompilasi.
- Hasil perhitungan 3 data dan 100 data dapat direkonsiliasi dengan rumus serta output aplikasi.
- DOCX hasil akhir memiliki tabel dan gambar yang lengkap tanpa teks/label lama.
- Seluruh halaman hasil render diperiksa secara visual untuk clipping, overlap, tabel terpotong, caption bergeser, dan gambar buram.

