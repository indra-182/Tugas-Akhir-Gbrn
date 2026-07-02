# SPK Penilaian Kualitas Racikan Kopi Barista Menggunakan Metode MAGIQ

Project ini dibuat untuk skripsi dengan judul:

**Rancang Bangun Sistem Pendukung Keputusan Penilaian Kualitas Racikan Kopi Barista dengan Metode MAGIQ (Multi-Attribute Global Inference of Quality) di Waroenk Bikers**

Teknologi yang digunakan:

- Java 1.8
- Java Swing
- NetBeans 8.2
- MySQL/XAMPP
- JDBC
- Metode MAGIQ

## Fitur

1. Login admin.
2. Kelola data barista sebagai alternatif penilaian.
3. Kelola data kriteria, bobot prioritas, dan tipe kriteria benefit/cost.
4. Input nilai kualitas racikan kopi per barista dan per kriteria.
5. Perhitungan MAGIQ otomatis.
6. Ranking barista berdasarkan nilai MAGIQ terbesar.
7. Cetak laporan ranking menggunakan fitur print Swing.

## Struktur Project

```text
src/com/gibran/waroenkbikers
├── dao       -> akses database
├── model     -> entity/model data
├── service   -> business logic, termasuk perhitungan MAGIQ
├── ui        -> Java Swing form/panel
└── util      -> helper koneksi, dialog, angka, password
```

Package utama project:

```text
com.gibran.waroenkbikers
```

## Cara Menjalankan

### 1. Import Database

1. Buka XAMPP.
2. Start Apache dan MySQL.
3. Buka `http://localhost/phpmyadmin`.
4. Import file:

```text
database/db_magiq_waroenk_bikers.sql
```

Database yang dibuat:

```text
db_magiq_waroenk_bikers
```

File SQL menyediakan 5 data barista, 5 kriteria racikan kopi, dan nilai penilaian awal.

### 2. Tambahkan MySQL Connector/J

Project ini memakai MySQL Connector/J versi Java 8:

```text
lib/mysql-connector-java-5.1.49.jar
```

Jika library belum terbaca di NetBeans:

1. Klik kanan project.
2. Pilih `Properties`.
3. Pilih `Libraries`.
4. Klik `Add JAR/Folder`.
5. Pilih file `mysql-connector-java-5.1.49.jar`.

### 3. Cek Konfigurasi Database

File konfigurasi:

```text
src/config.properties
```

Default konfigurasi:

```properties
db.url=jdbc:mysql://localhost:3306/db_magiq_waroenk_bikers?useSSL=false&useUnicode=true&characterEncoding=UTF-8
db.user=root
db.password=
db.driver=com.mysql.jdbc.Driver
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
| C5 | Kecepatan Penyajian | Waktu yang dibutuhkan dalam membuat kopi | Cost |

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
