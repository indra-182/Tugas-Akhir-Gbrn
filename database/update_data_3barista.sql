-- Menyesuaikan data berjalan dengan BAB 2-3 Rehan 11 Juli 2026.
-- Database menggunakan PostgreSQL/Supabase.
-- Query ini membuat database hanya berisi Barista A, B, dan C.

BEGIN;

-- Hasil ranking lama tidak berlaku setelah nilai penilaian diubah.
DELETE FROM hasil_ranking;

-- B001-B003 dipakai sebagai kode Barista A-C pada data awal proyek.
INSERT INTO barista (kode_barista, nama) VALUES
    ('B001', 'Barista A'),
    ('B002', 'Barista B'),
    ('B003', 'Barista C')
ON CONFLICT (kode_barista) DO UPDATE
SET nama = EXCLUDED.nama;

-- Hapus barista dummy selain tiga barista pada dokumen.
DELETE FROM barista
WHERE kode_barista NOT IN ('B001', 'B002', 'B003');

-- Perbarui nilai C1-C6. Jika baris penilaian belum ada, baris dibuat.
INSERT INTO penilaian (id_barista, id_kriteria, nilai)
SELECT b.id, k.id, d.nilai
FROM (VALUES
    ('B001', 'C1', 90), ('B001', 'C2', 85), ('B001', 'C3', 88),
    ('B001', 'C4', 90), ('B001', 'C5', 80), ('B001', 'C6', 85),
    ('B002', 'C1', 85), ('B002', 'C2', 90), ('B002', 'C3', 84),
    ('B002', 'C4', 88), ('B002', 'C5', 90), ('B002', 'C6', 90),
    ('B003', 'C1', 88), ('B003', 'C2', 82), ('B003', 'C3', 90),
    ('B003', 'C4', 85), ('B003', 'C5', 85), ('B003', 'C6', 88)
) AS d(kode_barista, kode_kriteria, nilai)
JOIN barista b ON b.kode_barista = d.kode_barista
JOIN kriteria k ON k.kode = d.kode_kriteria
ON CONFLICT (id_barista, id_kriteria) DO UPDATE
SET nilai = EXCLUDED.nilai;

COMMIT;

-- Verifikasi hasil.
SELECT b.kode_barista, b.nama, k.kode AS kode_kriteria, p.nilai
FROM barista b
JOIN penilaian p ON p.id_barista = b.id
JOIN kriteria k ON k.id = p.id_kriteria
ORDER BY b.kode_barista, k.kode;
