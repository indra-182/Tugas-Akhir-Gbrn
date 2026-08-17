-- Sinkronkan database dengan sheet "Data Barista dan Penilaian" versi terbaru.
-- Jalankan setelah database/db_magiq_waroenk_bikers.sql pada PostgreSQL/Supabase.
-- Nilai maksimum hasil data dummy: C1=100, C2=100, C3=60, C4=100, C5=100, C6=100.

BEGIN;

TRUNCATE TABLE hasil_ranking, penilaian, barista RESTART IDENTITY CASCADE;

INSERT INTO barista (kode_barista, nama)
SELECT 'B' || LPAD((i + 1)::TEXT, 3, '0'),
       'Barista ' || REPEAT(CHR(65 + (i % 26)), (i / 26) + 1)
FROM generate_series(0, 99) AS data(i);

INSERT INTO kriteria (kode, nama, bobot, urutan_prioritas, keterangan)
VALUES
    ('C1', 'Rasa Kopi', 0.4083, 1, 'Tingkat keseimbangan rasa pahit, manis, dan keasaman.'),
    ('C2', 'Aroma', 0.2417, 2, 'Keharuman kopi yang dihasilkan.'),
    ('C3', 'Konsistensi Racikan', 0.1583, 3, 'Konsistensi rasa antara satu penyajian dengan lainnya.'),
    ('C4', 'Penyajian', 0.1028, 4, 'Tampilan dan kerapihan penyajian kopi.'),
    ('C5', 'Kecepatan Penyajian', 0.0611, 5, 'Kecepatan barista dalam menyajikan kopi.'),
    ('C6', 'Stabilitas Suhu Penyajian', 0.0278, 6, 'Kestabilan suhu kopi saat disajikan.')
ON CONFLICT (kode) DO UPDATE
SET nama = EXCLUDED.nama,
    bobot = EXCLUDED.bobot,
    urutan_prioritas = EXCLUDED.urutan_prioritas,
    keterangan = EXCLUDED.keterangan;

INSERT INTO penilaian (id_barista, id_kriteria, nilai)
SELECT b.id,
       k.id,
       CASE k.kode
           WHEN 'C1' THEN 60 + 5 * (i % 9)
           WHEN 'C2' THEN 60 + 5 * ((i / 9) % 9)
           WHEN 'C3' THEN 60
           WHEN 'C4' THEN 60 + 5 * ((i * 5 + 2) % 9)
           WHEN 'C5' THEN 60 + 5 * ((i * 7 + 4) % 9)
           WHEN 'C6' THEN 60 + 5 * ((i * 8 + 6) % 9)
       END AS nilai
FROM barista AS b
CROSS JOIN LATERAL (
    SELECT SUBSTRING(b.kode_barista FROM 2)::INTEGER - 1 AS i
) AS data
CROSS JOIN kriteria AS k;

COMMIT;

-- Verifikasi jumlah data dan maksimum terbaru per kriteria.
SELECT COUNT(*) AS jumlah_barista FROM barista;

SELECT k.kode AS kode_kriteria, MAX(p.nilai) AS maksimum
FROM penilaian AS p
JOIN kriteria AS k ON k.id = p.id_kriteria
GROUP BY k.id, k.kode
ORDER BY k.id;

-- Verifikasi ranking yang sama dengan workbook.
WITH maksimum AS (
    SELECT id_kriteria, MAX(nilai) AS nilai_maksimum
    FROM penilaian
    GROUP BY id_kriteria
), skor AS (
    SELECT b.kode_barista,
           b.nama,
           SUM((p.nilai / m.nilai_maksimum) * k.bobot) AS nilai_magiq
    FROM barista AS b
    JOIN penilaian AS p ON p.id_barista = b.id
    JOIN kriteria AS k ON k.id = p.id_kriteria
    JOIN maksimum AS m ON m.id_kriteria = p.id_kriteria
    GROUP BY b.id, b.kode_barista, b.nama
)
SELECT ROW_NUMBER() OVER (ORDER BY nilai_magiq DESC, nama) AS peringkat,
       kode_barista,
       nama,
       ROUND(nilai_magiq, 10) AS nilai_magiq
FROM skor
ORDER BY peringkat;
