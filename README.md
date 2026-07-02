# SPK Penilaian Kualitas Racikan Kopi Barista Menggunakan Metode MAGIQ

Project ini dibuat untuk skripsi dengan judul:

**Rancang Bangun Sistem Pendukung Keputusan Penilaian Kualitas Racikan Kopi Barista dengan Metode MAGIQ (Multi-Attribute Global Inference of Quality) di Waroenk Bikers**

Teknologi yang digunakan:

- Java 1.8
- Java Swing
- NetBeans 8.2 (Ant project)
- MySQL (lokal atau cloud Aiven)
- JDBC + MySQL Connector/J 8.0.33
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
```

## Cara Menjalankan

### 1. Import Database

Jalankan MySQL lokal, lalu import file:

```text
database/db_magiq_waroenk_bikers.sql
```

Bisa lewat phpMyAdmin atau command line:

```text
mysql -u root -p < database/db_magiq_waroenk_bikers.sql
```

Database yang dibuat bernama `db_magiq_waroenk_bikers`. File SQL menyediakan
5 data barista, 6 kriteria racikan kopi, dan nilai penilaian awal.

### 2. Buat File Konfigurasi Database

File `src/config.properties` **tidak ikut repository** karena berisi kredensial.
Buat file tersebut secara manual dengan isi:

```properties
db.driver=com.mysql.cj.jdbc.Driver

# --- LOKAL (MySQL di komputer sendiri) ---
db.url=jdbc:mysql://localhost:3306/db_magiq_waroenk_bikers?sslMode=DISABLED&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&connectTimeout=10000
db.user=root
db.password=ISI_PASSWORD_MYSQL

# --- CLOUD (contoh: Aiven MySQL, wajib TLS) ---
#db.url=jdbc:mysql://HOST_CLOUD:PORT/db_magiq_waroenk_bikers?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&connectTimeout=10000&tcpKeepAlive=true&cachePrepStmts=true&prepStmtCacheSize=250&useLocalSessionState=true
#db.user=USER_CLOUD
#db.password=PASSWORD_CLOUD
```

Aktifkan salah satu blok (LOKAL atau CLOUD) dengan memindahkan tanda `#`.
File ini ter-bundle ke dalam JAR, jadi setiap kali diubah harus
`Clean and Build` ulang.

### 3. Library MySQL Connector/J

Driver sudah tersedia di `lib/mysql-connector-j-8.0.33.jar` dan sudah
terdaftar di project properties. Tidak perlu setup tambahan selama struktur
folder tidak diubah.

### 4. Jalankan Project

1. Buka NetBeans.
2. `File` -> `Open Project` -> pilih folder project ini.
3. Klik kanan project -> `Clean and Build` -> `Run`.

JAR hasil build ada di `dist/SPK-MAGIQ-Waroenk-Bikers.jar` dan bisa dijalankan
langsung dengan double-click (butuh Java terpasang).

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
- Keputusan akhir tetap berada pada pemilik usaha; sistem hanya alat bantu rekomendasi.
