CREATE DATABASE IF NOT EXISTS db_magiq_waroenk_bikers
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE db_magiq_waroenk_bikers;

DROP TABLE IF EXISTS hasil_ranking;
DROP TABLE IF EXISTS penilaian;
DROP TABLE IF EXISTS kriteria;
DROP TABLE IF EXISTS barista;
DROP TABLE IF EXISTS pengguna;

CREATE TABLE pengguna (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash CHAR(64) NOT NULL,
  nama_lengkap VARCHAR(100) NOT NULL,
  role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE barista (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_barista VARCHAR(30) NOT NULL UNIQUE,
  nama VARCHAR(100) NOT NULL,
  divisi VARCHAR(100),
  jabatan VARCHAR(100),
  tanggal_masuk DATE NULL,
  status ENUM('AKTIF', 'NONAKTIF') NOT NULL DEFAULT 'AKTIF',
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  diubah_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE kriteria (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode VARCHAR(10) NOT NULL UNIQUE,
  nama VARCHAR(100) NOT NULL,
  bobot DECIMAL(10,4) NOT NULL,
  tipe ENUM('BENEFIT', 'COST') NOT NULL DEFAULT 'BENEFIT',
  keterangan VARCHAR(255),
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  diubah_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE penilaian (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_barista INT NOT NULL,
  id_kriteria INT NOT NULL,
  nilai DECIMAL(10,4) NOT NULL DEFAULT 0,
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  diubah_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_penilaian_barista FOREIGN KEY (id_barista) REFERENCES barista(id) ON DELETE CASCADE,
  CONSTRAINT fk_penilaian_kriteria FOREIGN KEY (id_kriteria) REFERENCES kriteria(id) ON DELETE CASCADE,
  CONSTRAINT uq_barista_kriteria UNIQUE (id_barista, id_kriteria)
) ENGINE=InnoDB;

CREATE TABLE hasil_ranking (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_barista INT NOT NULL,
  nilai_magiq DECIMAL(12,6) NOT NULL,
  peringkat INT NOT NULL,
  dihitung_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_hasil_ranking_barista FOREIGN KEY (id_barista) REFERENCES barista(id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO pengguna (username, password_hash, nama_lengkap, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'ADMIN');

INSERT INTO barista (kode_barista, nama, divisi, jabatan, tanggal_masuk, status) VALUES
('B001', 'Ahmad Fadli', 'Bar', 'Barista', '2021-01-10', 'AKTIF'),
('B002', 'Aldi Pratamas', 'Bar', 'Barista', '2021-01-18', 'AKTIF'),
('B003', 'Andi Saputra', 'Quality Control', 'Senior Barista', '2021-02-03', 'AKTIF'),
('B004', 'Arif Hidayat', 'Service', 'Barista', '2021-02-11', 'AKTIF'),
('B005', 'Bagus Kurniawan', 'Training', 'Asisten Barista', '2021-02-20', 'AKTIF');

INSERT INTO kriteria (kode, nama, bobot, tipe, keterangan) VALUES
('C1', 'Rasa Kopi', 0.4083, 'BENEFIT', 'Tingkat keseimbangan rasa pahit, manis, dan keasaman.'),
('C2', 'Aroma', 0.2417, 'BENEFIT', 'Keharuman kopi yang dihasilkan.'),
('C3', 'Penyajian', 0.1583, 'BENEFIT', 'Tampilan dan kerapihan penyajian kopi.'),
('C4', 'Konsistensi Racikan', 0.1028, 'BENEFIT', 'Konsistensi rasa antara satu penyajian dengan lainnya.'),
('C5', 'Kecepatan Penyajian', 0.0611, 'COST', 'Waktu yang dibutuhkan barista dalam menyajikan kopi; semakin singkat semakin baik.'),
('C6', 'Stabilitas Suhu Penyajian', 0.0278, 'COST', 'Tingkat kestabilan suhu kopi saat disajikan yang diukur menggunakan alat termometer; semakin kecil penyimpangan suhu semakin baik.');

-- Nilai penilaian eksplisit untuk 6 kriteria (skala 1-100; C5 bersifat COST dalam satuan menit; C6 bersifat COST dalam satuan derajat Celsius/penyimpangan suhu yang diukur menggunakan termometer).
INSERT INTO penilaian (id_barista, id_kriteria, nilai)
SELECT b.id, k.id,
  CASE b.kode_barista
    WHEN 'B001' THEN ELT(FIELD(k.kode,'C1','C2','C3','C4','C5','C6'), 88, 73, 84, 95, 10, 3)
    WHEN 'B002' THEN ELT(FIELD(k.kode,'C1','C2','C3','C4','C5','C6'), 95, 80, 91, 76, 4, 2)
    WHEN 'B003' THEN ELT(FIELD(k.kode,'C1','C2','C3','C4','C5','C6'), 76, 87, 72, 83, 6, 4)
    WHEN 'B004' THEN ELT(FIELD(k.kode,'C1','C2','C3','C4','C5','C6'), 83, 94, 79, 90, 8, 3)
    WHEN 'B005' THEN ELT(FIELD(k.kode,'C1','C2','C3','C4','C5','C6'), 90, 75, 86, 71, 10, 5)
  END AS nilai
FROM barista b
CROSS JOIN kriteria k;
