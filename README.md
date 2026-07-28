# SPK Penilaian Kualitas Racikan Kopi Barista Menggunakan Metode MAGIQ

Project ini dibuat untuk skripsi dengan judul:

**Rancang Bangun Sistem Pendukung Keputusan Penilaian Kualitas Racikan Kopi Barista dengan Metode MAGIQ (Multi-Attribute Global Inference of Quality) di Waroenk Bikers**

Teknologi yang digunakan:

- Java 1.8
- Java Swing
- NetBeans 8.2 (Ant project)
- PostgreSQL melalui Supabase
- PostgreSQL JDBC Driver 42.7.13
- Metode MAGIQ

## Fitur

1. Login admin.
2. Kelola data barista sebagai alternatif penilaian.
3. Kelola data kriteria, bobot prioritas, dan tipe kriteria benefit/cost.
4. Input nilai kualitas racikan kopi per barista dan per kriteria.
5. Perhitungan MAGIQ otomatis.
6. Ranking barista berdasarkan nilai MAGIQ terbesar.
7. Cetak laporan (barista, kriteria, penilaian, ranking) menggunakan fitur print Swing.
8. Validasi cetak: laporan ranking hanya bisa dicetak setelah perhitungan diproses.

## Struktur Project

```text
src/com/gibran/waroenkbikers
├── dao       -> akses database
├── model     -> entity/model data
├── service   -> business logic, termasuk perhitungan MAGIQ
├── ui        -> Java Swing form/panel
└── util      -> helper koneksi, dialog, angka, password

src/assets/images
└── waroenk-bikers.jpg -> logo laporan; dimuat sebagai resource internal
```

## Cara Menjalankan

### 1. Import Database ke Supabase

Buka **Supabase Dashboard** -> project -> **SQL Editor**, lalu jalankan isi file:

```text
database/db_magiq_waroenk_bikers.sql
```

Skrip ini membuat ulang tabel pada skema `public`, sehingga jangan dijalankan
pada database produksi yang sudah berisi data penting. File SQL menyediakan 5
data barista, 6 kriteria racikan kopi, dan nilai penilaian awal.

### 2. Buat File Konfigurasi Database

File `src/config.properties` **tidak ikut repository** karena berisi kredensial.
Salin `src/config.properties.example` menjadi `src/config.properties`.

Di Supabase Dashboard, klik **Connect** -> **Session Pooler** dan gunakan
parameter yang ditampilkan di sana. Session Pooler dipakai agar aplikasi dapat
terhubung dari jaringan IPv4 maupun IPv6.

Isi file konfigurasi seperti ini:

```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://aws-REGION.pooler.supabase.com:5432/postgres?sslmode=require&gssEncMode=disable&connectTimeout=10&tcpKeepAlive=true&ApplicationName=SPK-MAGIQ-Waroenk-Bikers
db.user=postgres.PROJECT_REF
db.password=ISI_PASSWORD_DATABASE_SUPABASE
```

Gunakan host, user, dan password persis dari Supabase. Jangan gunakan direct
connection `db.<project-ref>.supabase.co:5432` pada jaringan yang tidak
mendukung IPv6. File konfigurasi dibundel ke JAR saat build; lakukan `Clean and
Build` setiap kali mengubahnya.

### 3. Library PostgreSQL JDBC

Driver tersedia di `lib/postgresql-42.7.13.jar` dan sudah
terdaftar di project properties. Tidak perlu setup tambahan selama struktur
folder tidak diubah.

### 4. Jalankan Project

1. Buka NetBeans.
2. `File` -> `Open Project` -> pilih folder project ini.
3. Klik kanan project -> `Clean and Build` -> `Run`.

JAR hasil build ada di `dist/SPK-MAGIQ-Waroenk-Bikers.jar`. Untuk menjalankan
di luar NetBeans, letakkan driver pada `dist/lib/postgresql-42.7.13.jar`, lalu
jalankan dari folder `dist`:

```text
java -jar SPK-MAGIQ-Waroenk-Bikers.jar
```

Default login:

```text
Username: admin
Password: admin123
```

## Kriteria Default

| Kode | Kriteria | Keterangan | Tipe |
| --- | --- | --- | --- |
| C1 | Rasa Kopi | Tingkat keseimbangan rasa pahit, manis, dan keasaman | Benefit |
| C2 | Aroma | Keharuman kopi yang dihasilkan | Benefit |
| C3 | Penyajian | Tampilan dan kerapihan penyajian kopi | Benefit |
| C4 | Konsistensi Racikan | Konsistensi rasa antara satu penyajian dengan lainnya | Benefit |
| C5 | Kecepatan Penyajian | Waktu yang dibutuhkan dalam membuat kopi (menit) | Cost |
| C6 | Stabilitas Suhu Penyajian | Penyimpangan suhu kopi saat disajikan, diukur dengan termometer (derajat Celsius) | Cost |

## Ringkasan Metode MAGIQ

MAGIQ menggunakan peringkat kepentingan kriteria dan peringkat alternatif pada setiap kriteria. Bobot dihitung dengan Rank Order Centroid (ROC):

```text
w_j = (1 / m) * sum(1 / k), untuk k = j sampai m
```

Langkah perhitungan:

1. Menentukan alternatif barista.
2. Menentukan kriteria dan urutan prioritas kriteria.
3. Menghitung bobot kriteria menggunakan ROC.
4. Mengurutkan barista pada setiap kriteria berdasarkan nilai penilaian.
5. Menghitung skor lokal alternatif menggunakan ROC.
6. Menghitung nilai akhir MAGIQ:

```text
Nilai MAGIQ = sum(bobot_kriteria * skor_lokal_alternatif)
```

7. Ranking ditentukan dari nilai MAGIQ terbesar ke terkecil.

## Catatan Pengembangan

- Koneksi database memakai satu koneksi bersama (shared connection) agar tetap cepat saat memakai database cloud.
- Logo laporan berada di `src/assets/images/waroenk-bikers.jpg`; jangan menggantinya dengan path absolut komputer.
- Jangan commit atau membagikan `src/config.properties` karena file tersebut berisi kredensial database.
- Keputusan akhir tetap berada pada pemilik usaha; sistem hanya alat bantu rekomendasi.
