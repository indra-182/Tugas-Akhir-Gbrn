# Benefit-only Criteria, Data Acuan, dan Preview Laporan Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Menghapus tipe benefit/cost, menyelaraskan data awal dengan dokumen penelitian, dan menyediakan preview laporan Swing sebelum cetak/PDF.

**Architecture:** Semua kriteria diperlakukan sebagai benefit oleh satu normalizer murni; model, DAO, UI, dan SQL tidak lagi menyimpan atribut tipe. `LaporanPanel` memegang satu snapshot waktu serta satu `Printable`, lalu merendernya di dialog preview dan mengirim objek yang sama ke `PrinterJob`.

**Tech Stack:** Java 8, Swing/AWT Printing, JDBC PostgreSQL, Ant/NetBeans, plain Java executable tests; tanpa dependency baru.

## Global Constraints

- Pertahankan kompatibilitas Java source/target 1.8 dan arsitektur DAO/service/UI yang ada.
- Jangan menambah dependency atau mengubah lockfile/library.
- Jangan membaca atau menampilkan kredensial dari `src/config.properties`.
- Hapus `tipe`, `BENEFIT`, `COST`, dan istilah UI `Jenis` dari model, SQL, DAO, kalkulasi, dan laporan; jangan sekadar menyembunyikannya.
- Data SQL awal wajib memiliki tepat tiga barista aktif: `Barista A`, `Barista B`, `Barista C` dengan kode B001–B003.
- Format tanda tangan wajib `Jakarta, EEEE d MMMM yyyy` dengan locale `id_ID`, misalnya `Jakarta, Minggu 8 Agustus 2026`.

---

### Task 1: Jadikan normalisasi selalu benefit

**Files:**
- Modify: `test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java`
- Modify: `src/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculator.java`
- Modify: `src/com/gibran/waroenkbikers/model/Kriteria.java`
- Modify: `src/com/gibran/waroenkbikers/service/PerhitunganMagiqService.java`
- Modify: `src/com/gibran/waroenkbikers/ui/PerhitunganMagiqPanel.java`

**Interfaces:**
- Consumes: `NormalisasiNilaiCalculator.hitungNilai(double nilai, double[] nilaiKolom)`.
- Produces: matriks normalisasi dengan rumus `nilai / maksimum` untuk setiap kriteria dan daftar urutan kriteria tanpa tipe.

- [ ] **Step 1: Tulis test gagal untuk API normalisasi tanpa flag cost.**

```java
private static void shouldNormalizeAgainstColumnMaximum() {
    double result = NormalisasiNilaiCalculator.hitungNilai(75.0,
            new double[]{50.0, 75.0, 100.0});
    assertClose(0.75, result);
}

private static void shouldNormalizeZeroColumnAsOne() {
    assertClose(1.0, NormalisasiNilaiCalculator.hitungNilai(0.0,
            new double[]{0.0, 0.0}));
}
```

Hapus test cost dari `main`; panggil dua test di atas.

- [ ] **Step 2: Jalankan test untuk memastikan gagal karena signature lama.**

Run: `javac -cp build/classes -d build/test/classes test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java && java -ea -cp build/classes:build/test/classes com.gibran.waroenkbikers.util.NormalisasiNilaiCalculatorTest`

Expected: compile failure karena `hitungNilai` masih membutuhkan argumen boolean.

- [ ] **Step 3: Terapkan API benefit-only minimal.**

```java
public static double hitungNilai(double nilai, double[] nilaiKolom) {
    if (nilaiKolom == null || nilaiKolom.length == 0) {
        throw new IllegalArgumentException("Data nilai untuk normalisasi belum tersedia.");
    }
    double maksimum = nilaiMaksimum(nilaiKolom);
    return maksimum == 0.0 ? 1.0 : nilai / maksimum;
}
```

Hapus `nilaiMinimum`, parameter/percabangan `cost`, konstanta `BENEFIT` dan `COST`, serta field/getter/setter `tipe`. Pada service, panggil signature baru, buang validasi tipe dan elemen tipe dari `buatDataUrutanKriteria`. Pada tabel bobot panel perhitungan, hapus kolom `Tipe` serta akses `data[5]`.

- [ ] **Step 4: Jalankan test fokus hingga lulus.**

Run: `javac -source 8 -target 8 -cp build/classes -d build/test/classes test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java && java -ea -cp build/classes:build/test/classes com.gibran.waroenkbikers.util.NormalisasiNilaiCalculatorTest`

Expected: exit code 0.

- [ ] **Step 5: Commit perubahan perhitungan.**

```bash
git add test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java \
  src/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculator.java \
  src/com/gibran/waroenkbikers/model/Kriteria.java \
  src/com/gibran/waroenkbikers/service/PerhitunganMagiqService.java \
  src/com/gibran/waroenkbikers/ui/PerhitunganMagiqPanel.java
git commit -m "refactor: make MAGIQ criteria benefit-only"
```

### Task 2: Hapus tipe kriteria dari persistensi dan UI

**Files:**
- Modify: `src/com/gibran/waroenkbikers/dao/KriteriaDao.java`
- Modify: `src/com/gibran/waroenkbikers/ui/KriteriaPanel.java`
- Modify: `src/com/gibran/waroenkbikers/ui/LaporanPanel.java`

**Interfaces:**
- Consumes: `Kriteria` hanya dengan `id`, `kode`, `nama`, `bobot`, `urutanPrioritas`, `keterangan`.
- Produces: query `kriteria` tanpa kolom tipe dan tampilan data kriteria tanpa field/kolom Jenis.

- [ ] **Step 1: Tambahkan assertion source-level yang gagal untuk memastikan nama tipe tidak tersisa di KriteriaPanel.**

Tambahkan ke `test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java` method berikut (import `java.nio.file.Files`, `java.nio.file.Paths`, dan `java.nio.charset.StandardCharsets`):

```java
private static void shouldNotExposeCriterionTypeInCriteriaPanel() throws Exception {
    String source = new String(Files.readAllBytes(Paths.get(
            "src/com/gibran/waroenkbikers/ui/KriteriaPanel.java")), StandardCharsets.UTF_8);
    if (source.contains("tipeComboBox") || source.contains("\"Jenis\"")) {
        throw new AssertionError("Criterion type controls must be removed");
    }
}
```

Panggil method dari `main`.

- [ ] **Step 2: Jalankan test dan pastikan assertion gagal pada source saat ini.**

Run: `javac -source 8 -target 8 -cp build/classes -d build/test/classes test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java && java -ea -cp build/classes:build/test/classes com.gibran.waroenkbikers.util.NormalisasiNilaiCalculatorTest`

Expected: `AssertionError: Criterion type controls must be removed`.

- [ ] **Step 3: Hapus field dan query tipe secara menyeluruh.**

Di `KriteriaDao`, hapus `tipe` dari SELECT, INSERT, UPDATE dan pemetaan `ResultSet`; INSERT menjadi `(kode, nama, bobot, urutan_prioritas, keterangan)` dan UPDATE hanya memperbarui kode, nama, keterangan. Di `KriteriaPanel`, hapus import `JComboBox`, field combo box, baris input `Jenis`, validasi, binding saat tabel dipilih, reset field, setter model, serta kolom `Jenis`/case indeks terakhir. Di laporan, ganti kolom `Kode, Nama Kriteria, Bobot, Jenis` menjadi `Kode, Nama Kriteria, Bobot` dan hapus nilai tipe.

- [ ] **Step 4: Jalankan test source-level dan kompilasi seluruh produksi.**

Run: `ant clean jar`

Expected: `BUILD SUCCESSFUL`. Lalu jalankan test Task 2 lagi dan pastikan exit code 0.

- [ ] **Step 5: Commit perubahan persistensi/UI.**

```bash
git add src/com/gibran/waroenkbikers/dao/KriteriaDao.java \
  src/com/gibran/waroenkbikers/ui/KriteriaPanel.java \
  src/com/gibran/waroenkbikers/ui/LaporanPanel.java \
  test/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculatorTest.java
git commit -m "refactor: remove criterion type fields"
```

### Task 3: Selaraskan skema, migrasi, dan data awal

**Files:**
- Modify: `database/db_magiq_waroenk_bikers.sql`
- Create: `database/migrations/20260808_drop_kriteria_type.sql`
- Modify: `README.md`

**Interfaces:**
- Consumes: tabel `kriteria` tanpa kolom `tipe`; relasi `penilaian` tetap mengacu pada ID kriteria/barista.
- Produces: skrip instalasi baru berisi 3×6 nilai acuan dan migrasi idempoten yang tidak menghapus barista/penilaian existing.

- [ ] **Step 1: Tulis verifikasi SQL yang akan gagal terhadap seed lama.**

Tambahkan blok berikut ke akhir skrip inisialisasi sementara untuk dipakai setelah insert (hapus kembali blok ini sebelum commit jika SQL Editor tidak mendukung assertion):

```sql
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM barista) <> 3 OR (SELECT COUNT(*) FROM penilaian) <> 18 THEN
    RAISE EXCEPTION 'Seed must contain exactly 3 baristas and 18 assessments';
  END IF;
END;
$$;
```

Jalankan pada database uji PostgreSQL/Supabase bila tersedia; jika tidak tersedia, verifikasi struktur dan literal seed dengan `rg` pada langkah 4.

- [ ] **Step 2: Konfirmasi seed lama tidak sesuai.**

Run: `rg -n "B004|B005|Ahmad Fadli|COST|tipe" database/db_magiq_waroenk_bikers.sql`

Expected: hasil menunjukkan data/tipe lama sebelum diubah.

- [ ] **Step 3: Perbarui SQL dan tulis migrasi non-destruktif.**

Hilangkan definisi `tipe` dan constraint `CHECK` dari `CREATE TABLE kriteria`, lalu hilangkan kolom/nilai tipe dari INSERT kriteria. Seed barista:

```sql
('B001', 'Barista A', 'Bar', 'Barista', 'AKTIF'),
('B002', 'Barista B', 'Bar', 'Barista', 'AKTIF'),
('B003', 'Barista C', 'Bar', 'Barista', 'AKTIF');
```

Seed C1–C6 harus memakai urutan `Rasa Kopi`, `Aroma`, `Konsistensi Racikan`, `Penyajian`, `Kecepatan Penyajian`, `Stabilitas Suhu Penyajian`, lalu CASE nilai tepat sesuai tabel spesifikasi.

Isi migrasi baru dengan:

```sql
BEGIN;
ALTER TABLE public.kriteria DROP CONSTRAINT IF EXISTS kriteria_tipe_check;
ALTER TABLE public.kriteria DROP COLUMN IF EXISTS tipe;
COMMIT;
```

Gunakan nama constraint yang sebenarnya bila berbeda: query `pg_constraint` pada database sebelum menjalankan migrasi. README wajib menyatakan bahwa seed baru berisi 3 barista/6 kriteria dan bahwa migrasi hanya menghapus atribut tipe, bukan data pengguna.

- [ ] **Step 4: Verifikasi literal SQL dan dokumentasi.**

Run: `rg -n "B004|B005|COST|BENEFIT|tipe|Barista [ABC]|WHEN 'C[1-6]'" database/db_magiq_waroenk_bikers.sql database/migrations/20260808_drop_kriteria_type.sql README.md`

Expected: hanya referensi `tipe` pada migrasi/penjelasan migrasi; tidak ada B004/B005, COST, atau BENEFIT dalam seed.

- [ ] **Step 5: Commit skema dan seed.**

```bash
git add database/db_magiq_waroenk_bikers.sql \
  database/migrations/20260808_drop_kriteria_type.sql README.md
git commit -m "feat: align MAGIQ seed data with research"
```

### Task 4: Tambahkan preview cetak dan hari pada tanda tangan

**Files:**
- Create: `test/com/gibran/waroenkbikers/util/TanggalLaporanFormatterTest.java`
- Create: `src/com/gibran/waroenkbikers/util/TanggalLaporanFormatter.java`
- Modify: `src/com/gibran/waroenkbikers/ui/LaporanPanel.java`

**Interfaces:**
- Consumes: `TanggalLaporanFormatter.format(Date tanggal)` dan `Printable` laporan yang dibangun dari snapshot data/tanggal.
- Produces: teks `Jakarta, Minggu 8 Agustus 2026`, dialog preview modal, dan aksi Cetak yang meneruskan printable yang sama ke `PrinterJob`.

- [ ] **Step 1: Tulis test gagal formatter tanggal Indonesia.**

```java
public static void main(String[] args) throws Exception {
    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    parser.setLenient(false);
    String result = TanggalLaporanFormatter.format(parser.parse("2026-08-08"));
    if (!"Jakarta, Sabtu 8 Agustus 2026".equals(result)) {
        throw new AssertionError("Unexpected report date: " + result);
    }
}
```

Gunakan tanggal yang benar kalendernya pada ekspektasi test; untuk contoh user `Minggu 8 Agustus 2026`, formatter harus tetap menghasilkan hari kalender sebenarnya untuk objek `Date` yang diberikan.

- [ ] **Step 2: Jalankan test dan pastikan gagal karena kelas belum tersedia.**

Run: `javac -source 8 -target 8 -cp build/classes -d build/test/classes test/com/gibran/waroenkbikers/util/TanggalLaporanFormatterTest.java`

Expected: compile failure `cannot find symbol: TanggalLaporanFormatter`.

- [ ] **Step 3: Tambahkan formatter murni dan preview Swing.**

```java
public final class TanggalLaporanFormatter {
    private static final Locale INDONESIA = new Locale("id", "ID");
    public static String format(Date tanggal) {
        return "Jakarta, " + new SimpleDateFormat("EEEE d MMMM yyyy", INDONESIA).format(tanggal);
    }
}
```

Di `LaporanPanel`, saat tombol Cetak ditekan, validasi ranking/data seperti sekarang, buat satu `Date tanggalCetak`, satu `CetakLaporanPrintable(tanggalCetak)`, dan satu `PrinterJob`. Render halaman pertama printable ke `BufferedImage` berukuran A4 skala tampilan dalam `JLabel`/`JScrollPane` pada `JDialog` modal. Sediakan `Cetak / Simpan PDF` yang menutup preview lalu memanggil `printDialog` dan worker print yang sudah ada; sediakan `Tutup` yang hanya menutup dialog. Pastikan `gambarTandaTangan` memakai `TanggalLaporanFormatter.format(tanggalCetak)` dan bukan `new Date()` per render.

- [ ] **Step 4: Jalankan test formatter dan build.**

Run: `ant clean jar && javac -source 8 -target 8 -cp build/classes -d build/test/classes test/com/gibran/waroenkbikers/util/TanggalLaporanFormatterTest.java && java -ea -cp build/classes:build/test/classes com.gibran.waroenkbikers.util.TanggalLaporanFormatterTest`

Expected: `BUILD SUCCESSFUL` dan exit code 0.

- [ ] **Step 5: Lakukan verifikasi visual manual preview.**

Run aplikasi melalui NetBeans atau `java -cp "build/classes:lib/postgresql-42.7.13.jar" com.gibran.waroenkbikers.Main` dengan konfigurasi database pengembangan yang tersedia.

Expected: pilih setiap jenis laporan, klik Cetak, preview muncul sebelum dialog printer; area tanda tangan memperlihatkan hari; Tutup tidak membuat file; Cetak membuka dialog printer; ranking tanpa hasil tetap ditolak.

- [ ] **Step 6: Commit preview laporan.**

```bash
git add src/com/gibran/waroenkbikers/util/TanggalLaporanFormatter.java \
  src/com/gibran/waroenkbikers/ui/LaporanPanel.java \
  test/com/gibran/waroenkbikers/util/TanggalLaporanFormatterTest.java
git commit -m "feat: preview reports before printing"
```

### Task 5: Verifikasi akhir dan dokumentasi hasil

**Files:**
- Modify: `README.md`
- Modify: `docs/superpowers/specs/2026-08-08-benefit-only-preview-laporan-design.md` only if implementation exposes a spec mismatch.

**Interfaces:**
- Consumes: seluruh source/test/database migration dari task sebelumnya.
- Produces: build terverifikasi dan README yang akurat.

- [ ] **Step 1: Jalankan seluruh test executable dengan assertion aktif.**

Run:

```bash
for class in MagiqPreferenceCalculatorTest NormalisasiNilaiCalculatorTest PrioritasKriteriaValidatorTest RocWeightCalculatorTest TanggalLaporanFormatterTest; do
  java -ea -cp build/classes:build/test/classes com.gibran.waroenkbikers.util.$class || exit 1
done
```

Expected: setiap test exit code 0.

- [ ] **Step 2: Periksa bahwa nama tipe tidak tersisa dalam source produksi.**

Run: `rg -n "BENEFIT|COST|getTipe|setTipe|tipeComboBox|\"Jenis\"" src database/db_magiq_waroenk_bikers.sql`

Expected: tidak ada hasil.

- [ ] **Step 3: Periksa diff dan status repository.**

Run: `git diff --check && git status --short && git log -5 --oneline`

Expected: tidak ada whitespace error; hanya perubahan yang terkait ruang lingkup ini.

- [ ] **Step 4: Commit dokumentasi akhir bila ada perubahan README/spec tersisa.**

```bash
git add README.md docs/superpowers/specs/2026-08-08-benefit-only-preview-laporan-design.md
git commit -m "docs: update MAGIQ setup guidance"
```
