# BAB IV and App Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the Java/Supabase application and `BAB_IV - Waroenk Bikers.docx` with the BAB II/BAB III concepts and the current 100-row application dataset, while removing the barista profile fields `jabatan`, `divisi`, and `status`.

**Architecture:** Keep the existing Java Swing and Ant project structure. Make the Barista domain model contain only `id`, `kodeBarista`, and `nama`; make all barista queries return all rows because there is no longer a status filter. Generate one deterministic calculation source for the 3-row BAB III example and the 100-row SQL seed, then use that source for the BAB IV tables, mockups, screenshots, and DOCX replacement.

**Tech Stack:** Java 8, Java Swing, Ant/NetBeans project, PostgreSQL/Supabase SQL, bundled Python with `python-docx` and Pillow for document/assets, Microsoft Word or the packaged DOCX renderer for final visual QA.

## Global Constraints

- Backup `output/BAB_IV - Waroenk Bikers.docx` before changing the document.
- Remove `jabatan`, `divisi`, and `status` from the Barista model, DAO, UI, report, seed SQL, migration, and data export.
- Do not mention criterion types, BENEFIT, or COST in the updated BAB IV narrative, tables, mockups, or screenshots.
- Preserve the existing document structure and the monochrome line-art style of the current rancangan layar images.
- Use the BAB III Barista A/B/C values for the 3-row calculation and the application seed formula for all 100 dummy rows.
- Keep the six criteria and their order consistent across BAB II, BAB III, BAB IV, and the app: Rasa Kopi, Aroma, Konsistensi Racikan, Penyajian, Kecepatan Penyajian, Stabilitas Suhu Penyajian.
- Show all rows for the 100-row alternative, decision, normalization, preference, and ranking tables in Point B.
- Do not expose database credentials from `src/config.properties` in logs, generated assets, or the document.

---

## File Map

- Modify: `src/com/gibran/waroenkbikers/model/Barista.java` — retain only the identifier, code, and name properties.
- Modify: `src/com/gibran/waroenkbikers/dao/BaristaDao.java` — remove deleted SQL columns and status-filtered queries.
- Modify: `src/com/gibran/waroenkbikers/ui/BaristaPanel.java` — remove deleted fields from form, table, selection, validation, and persistence.
- Modify: `src/com/gibran/waroenkbikers/ui/PenilaianPanel.java` — use the unfiltered barista list.
- Modify: `src/com/gibran/waroenkbikers/ui/PerhitunganMagiqPanel.java` — remove the deleted barista column from all calculation tables.
- Modify: `src/com/gibran/waroenkbikers/ui/LaporanPanel.java` — remove deleted barista columns from reports.
- Modify: `database/db_magiq_waroenk_bikers.sql` — remove deleted columns and seed values.
- Create: `database/migrations/20260809_drop_barista_profile_fields.sql` — drop `jabatan`, `divisi`, and `status` from an existing database.
- Modify: `README.md` — describe the current PostgreSQL/Supabase schema and the reduced Barista record where the README currently documents the old shape.
- Modify: `output/data-barista-dan-penilaian.xlsx` — remove the three deleted barista columns while retaining the six score columns.
- Create: `output/tangkapan_layar/` — store reproducible copies of the 10 updated app screenshots before embedding them in the DOCX.
- Modify: `output/rancangan_layar/Gambar4.17_rancangan_layar_login.png` through `Gambar4.28_rancangan_layar_laporan_data_ranking_setelah_proses.png` — update all 12 mockups.
- Modify: `output/BAB_IV - Waroenk Bikers.docx` — update Point B, later table caption numbering, consistency prose, and all 22 requested screen figures.
- Create: `test/com/gibran/waroenkbikers/model/BaristaContractTest.java` — reflection contract for the reduced Barista model.

## Task 1: Freeze sources and create the requested DOCX backup

**Files:**
- Create: `output/BAB_IV - Waroenk Bikers (Backup 2026-08-09).docx` (or a same-day unique suffix if that file already exists)
- Read: `C:/Users/mahad/Downloads/BAB 2-3 Rehan 11 Juli 2026.docx`
- Read: `database/db_magiq_waroenk_bikers.sql`

- [ ] **Step 1: Verify the exact target and source files exist**

Run:

```powershell
Get-Item -LiteralPath 'output/BAB_IV - Waroenk Bikers.docx'
Get-Item -LiteralPath 'C:/Users/mahad/Downloads/BAB 2-3 Rehan 11 Juli 2026.docx'
```

Expected: both files exist and the target DOCX is readable.

- [ ] **Step 2: Copy the target before any DOCX mutation**

Run:

```powershell
Copy-Item -LiteralPath 'output/BAB_IV - Waroenk Bikers.docx' -Destination 'output/BAB_IV - Waroenk Bikers (Backup 2026-08-09).docx' -Force
```

Expected: the backup exists and has the same byte length as the target before editing.

- [ ] **Step 3: Extract a read-only BAB II/BAB III canonical checklist**

Record these exact values from the reference document and confirm them against the current seed:

```text
Criteria order: Rasa, Aroma, Konsistensi, Penyajian, Kecepatan, Suhu
ROC weights: 0.4083, 0.2417, 0.1583, 0.1028, 0.0611, 0.0278
A: 90, 85, 88, 90, 80, 85
B: 85, 90, 84, 88, 90, 90
C: 88, 82, 90, 85, 85, 88
```

Expected: the checklist is the source of truth for the 3-row Point B block; no BAB IV table uses the old five-barista values.

## Task 2: Add a failing Barista contract test

**Files:**
- Create: `test/com/gibran/waroenkbikers/model/BaristaContractTest.java`

- [ ] **Step 1: Add a reflection test that fails against the current model**

```java
package com.gibran.waroenkbikers.model;

import java.lang.reflect.Field;

public final class BaristaContractTest {
    private BaristaContractTest() {
    }

    public static void main(String[] args) {
        assertFieldExists("id");
        assertFieldExists("kodeBarista");
        assertFieldExists("nama");
        assertFieldAbsent("jabatan");
        assertFieldAbsent("divisi");
        assertFieldAbsent("status");
        assertMethodAbsent("getJabatan");
        assertMethodAbsent("getDivisi");
        assertMethodAbsent("getStatus");
    }

    private static void assertFieldExists(String name) {
        try {
            Barista.class.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Missing field: " + name, ex);
        }
    }

    private static void assertFieldAbsent(String name) {
        try {
            Barista.class.getDeclaredField(name);
            throw new AssertionError("Deleted field still exists: " + name);
        } catch (NoSuchFieldException expected) {
            // Expected.
        }
    }

    private static void assertMethodAbsent(String name) {
        for (java.lang.reflect.Method method : Barista.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                throw new AssertionError("Deleted method still exists: " + name);
            }
        }
    }
}
```

- [ ] **Step 2: Run the focused test and confirm it fails before implementation**

Run:

```powershell
ant -q clean test
```

Expected: the new contract test fails because the current `Barista` still declares the three deleted fields/methods. Preserve this failure as the TDD checkpoint before applying the implementation changes.

## Task 3: Remove the three barista profile fields from Java

**Files:**
- Modify: `src/com/gibran/waroenkbikers/model/Barista.java`
- Modify: `src/com/gibran/waroenkbikers/dao/BaristaDao.java`
- Modify: `src/com/gibran/waroenkbikers/ui/BaristaPanel.java`
- Modify: `src/com/gibran/waroenkbikers/ui/PenilaianPanel.java`
- Modify: `src/com/gibran/waroenkbikers/ui/PerhitunganMagiqPanel.java`
- Modify: `src/com/gibran/waroenkbikers/ui/LaporanPanel.java`

**Interfaces:**
- `Barista` exposes `getId/setId`, `getKodeBarista/setKodeBarista`, `getNama/setNama`, and `toString()` only.
- `BaristaDao.ambilSemua()` returns all barista rows ordered by `kode_barista`.
- All callers use `ambilSemua()`; no production code calls an `ambilAktif()` status filter.

- [ ] **Step 1: Reduce the model**

Remove the `divisi`, `jabatan`, and `status` fields plus all related accessors from `Barista.java`; keep `toString()` as `kodeBarista + " - " + nama`.

- [ ] **Step 2: Rewrite DAO SQL and mapping**

Use:

```java
private static final String KOLOM_BARISTA = "id, kode_barista, nama";
```

Change insert/update bindings to `(kode_barista, nama)` and map only those three columns. Remove the status parameter and the status-filtered query; update service/UI callers to `ambilSemua()`.

- [ ] **Step 3: Simplify the Barista panel**

Keep only `Kode Barista`, `Nama`, and `Search Data` in the form. Change the table headers to `No`, `Kode Barista`, `Nama`. Remove status selection, deleted-field validation, deleted-field selection population, and deleted-field persistence.

- [ ] **Step 4: Simplify dependent UI tables**

In `PenilaianPanel`, load all baristas via `ambilSemua()`. In `PerhitunganMagiqPanel`, remove the `Jabatan` column from the initial and processed decision tables. In `LaporanPanel`, emit only `Kode Barista` and `Nama` for barista reports and remove all deleted getters.

- [ ] **Step 5: Run the focused model contract and compile**

Run:

```powershell
ant -q clean test
ant -q jar
```

Expected: the contract test and existing utility tests pass, and the JAR target completes without Java compilation errors.

## Task 4: Remove the fields from SQL, migration, README, and export data

**Files:**
- Modify: `database/db_magiq_waroenk_bikers.sql`
- Create: `database/migrations/20260809_drop_barista_profile_fields.sql`
- Modify: `README.md`
- Modify: `output/data-barista-dan-penilaian.xlsx`

- [ ] **Step 1: Update the clean seed schema and seed insert**

The `barista` table definition must contain only `id`, `kode_barista`, and `nama` among the profile attributes. The 100-row seed insert must be:

```sql
INSERT INTO barista (kode_barista, nama)
SELECT 'B' || LPAD((i + 1)::TEXT, 3, '0'),
       'Barista ' || REPEAT(CHR(65 + (i % 26)), (i / 26) + 1)
FROM generate_series(0, 99) AS data(i);
```

Retain the six criteria and existing deterministic score CASE expressions.

- [ ] **Step 2: Add the existing-database migration**

Create:

```sql
ALTER TABLE barista DROP COLUMN IF EXISTS jabatan;
ALTER TABLE barista DROP COLUMN IF EXISTS divisi;
ALTER TABLE barista DROP COLUMN IF EXISTS status;
```

The migration must not delete barista rows or assessment rows.

- [ ] **Step 3: Update the export workbook**

Keep one sheet with headers `Kode Barista`, `Nama Barista`, `C1`, `C2`, `C3`, `C4`, `C5`, `C6`, followed by all 100 seed rows. Remove `Divisi`, `Jabatan`, and `Status` columns without changing the score values.

- [ ] **Step 4: Run structural absence checks**

Run:

```powershell
rg -n -i "getJabatan|setJabatan|getDivisi|setDivisi|getStatus|setStatus|jabatan|divisi|status" src database/db_magiq_waroenk_bikers.sql README.md
```

Expected: no production source, clean seed, or README references remain; historical migration/spec text may be reviewed separately but must not be used by the compiled application.

## Task 5: Generate the two calculation datasets and reconcile BAB II/BAB III/BAB IV

**Files:**
- Create: `output/bab4_work/calculation_source.json` (temporary working source)
- Read: `database/db_magiq_waroenk_bikers.sql`
- Read: `C:/Users/mahad/Downloads/BAB 2-3 Rehan 11 Juli 2026.docx`

- [ ] **Step 1: Implement the deterministic calculation source**

Use the following calculation contract:

```text
weights = [0.4083333333333333, 0.24166666666666667,
           0.15833333333333333, 0.10277777777777777,
           0.06111111111111111, 0.027777777777777776]
normalized[i][j] = decision[i][j] / max(decision[*][j])
preference[i] = sum(weights[j] * normalized[i][j] for j in 0..5)
ranking = preference descending, then name ascending for ties
```

Generate the 3-row dataset from the BAB III values and the 100-row dataset from the SQL CASE expressions. Preserve unrounded values for calculations and format displayed values to four decimals.

- [ ] **Step 2: Validate the 3-row output against BAB III**

Assert the criteria order and source values match the canonical checklist from Task 1. Assert the ranking table contains exactly three rows and the rounded preference values match the BAB III example.

- [ ] **Step 3: Validate the 100-row output against the seed contract**

Assert codes are exactly `B001` through `B100`, each row has six scores in the `60, 65, ..., 100` domain, each matrix has 100 rows, and each ranking position from 1 through 100 appears exactly once.

- [ ] **Step 4: Reconcile the BAB IV prose before inserting it**

Use the same terminology as BAB II/BAB III for SPK, MCDM, MAGIQ, ROC, criteria names, and formula symbols. Keep the app-specific database statement as PostgreSQL/Supabase. Do not copy the reference document's old MySQL or deleted barista profile fields into BAB IV.

## Task 6: Rebuild Point B tables and text in the DOCX

**Files:**
- Modify: `output/BAB_IV - Waroenk Bikers.docx`

- [ ] **Step 1: Replace the Point B narrative and tables surgically**

Keep the existing BAB IV heading hierarchy and source captions. Update the Point B block so it contains:

1. six criteria in the BAB II/BAB III order;
2. the shared ROC weight table;
3. a complete 3-row data-primer alternative table;
4. a complete 3-row decision table;
5. a complete 3-row normalization table;
6. a 3-row preference-and-ranking table with the preference value and final rank;
7. a complete 100-row dummy alternative table;
8. a complete 100-row dummy decision table;
9. a complete 100-row normalization table;
10. a complete 100-row preference table and ranking table.

Use repeated header rows, allow rows to expand, keep numbers at four displayed decimals, and preserve the original table style. Do not use fixed row heights.

- [ ] **Step 2: Renumber later table captions and references**

Because the 100-row calculation block adds five tables to the existing Point B, shift the existing captions `Tabel 4.7`–`Tabel 4.13` to `Tabel 4.12`–`Tabel 4.18` and update any prose that refers to those captions. The 3-row preference value remains combined with its ranking table, so no extra 3-row table is added. Figure numbering remains unchanged.

- [ ] **Step 3: Correct application-alignment prose**

Update Point C and related prose from MySQL to PostgreSQL/Supabase, remove deleted barista profile fields, remove criterion-type terminology, and describe the 100-row application dataset and preview report flow.

- [ ] **Step 4: Run DOCX structural checks**

Use a bundled Python inspection script to assert that Point B contains the expected table headers and row counts, that all 100-row tables contain 100 data rows plus one header, and that old text such as `Jabatan`, `Divisi`, `Status`, `Tipe`, `BENEFIT`, `COST`, `lima barista`, and `MySQL` is absent from document text.

## Task 7: Update the 12 rancangan layar images

**Files:**
- Modify: `output/rancangan_layar/Gambar4.17_rancangan_layar_login.png`
- Modify: `output/rancangan_layar/Gambar4.18_rancangan_layar_menu_utama.png`
- Modify: `output/rancangan_layar/Gambar4.19_rancangan_layar_data_barista.png`
- Modify: `output/rancangan_layar/Gambar4.20_rancangan_layar_data_kriteria.png`
- Modify: `output/rancangan_layar/Gambar4.21_rancangan_layar_data_penilaian.png`
- Modify: `output/rancangan_layar/Gambar4.22_rancangan_layar_proses_perhitungan_magiq.png`
- Modify: `output/rancangan_layar/Gambar4.23_rancangan_layar_cetak_laporan.png`
- Modify: `output/rancangan_layar/Gambar4.24_rancangan_layar_laporan_data_barista.png`
- Modify: `output/rancangan_layar/Gambar4.25_rancangan_layar_laporan_data_kriteria.png`
- Modify: `output/rancangan_layar/Gambar4.26_rancangan_layar_laporan_data_penilaian.png`
- Modify: `output/rancangan_layar/Gambar4.27_rancangan_layar_laporan_data_ranking_sebelum_proses_perhitungan.png`
- Modify: `output/rancangan_layar/Gambar4.28_rancangan_layar_laporan_data_ranking_setelah_proses_perhitungan.png`

- [ ] **Step 1: Use the existing monochrome visual tokens**

Preserve the current white canvas, black/dark-gray outlines, bold section labels, and line-art treatment. Use the actual Swing labels and button names from `TampilanUtil`, `MainFrame`, and the relevant panels.

- [ ] **Step 2: Remove obsolete fields and update data states**

The data barista mockup has only code/name; the criteria mockup has code/name/priority/automatic weight; the assessment mockup has code/criteria/weight/value without type; the dashboard shows 100 baristas and 6 criteria; calculation and report mockups reflect the five app calculation tables and preview flow.

- [ ] **Step 3: Inspect every mockup before embedding**

Open all 12 PNGs and check that labels are not clipped, tables do not show deleted fields, and the visual style remains consistent with the existing rancangan layar set.

## Task 8: Capture the 10 updated app screenshots

**Files:**
- Create/modify: `output/tangkapan_layar/Gambar4.29_tampilan_layar_login.png` through `Gambar4.38_tampilan_layar_setelah_logout.png`
- Modify: `output/BAB_IV - Waroenk Bikers.docx`

- [ ] **Step 1: Build and launch the updated app with the 100-row seed**

Use the configured application connection without printing the values from `src/config.properties`. Log in using the documented admin account and verify the dashboard shows 100 baristas and 6 criteria.

- [ ] **Step 2: Capture the exact ten states represented in BAB IV**

Capture Login, Dashboard, Data Barista, Data Kriteria, Data Penilaian, Perhitungan sebelum proses, Perhitungan setelah proses, Laporan Data Ranking, Cetak/Preview Laporan, and the post-Logout Login screen. The Data Barista and all report tables must not show the deleted fields.

- [ ] **Step 3: Inspect and map screenshots to captions**

Check every image at native resolution, then map each image to Gambar 4.29–4.38 by its caption rather than by old media filenames. Embed the images inline next to the existing captions.

## Task 9: Replace embedded DOCX media and complete final verification

**Files:**
- Modify: `output/BAB_IV - Waroenk Bikers.docx`

- [ ] **Step 1: Replace all 22 requested figure media parts**

Use inline images, preserve the existing figure-caption order, and keep the source caption after each image. Confirm the document still contains the unrelated UML/activity/sequence/class figures unchanged.

- [ ] **Step 2: Run source and data verification**

Run:

```powershell
ant -q clean test
ant -q jar
rg -n -i "getJabatan|setJabatan|getDivisi|setDivisi|getStatus|setStatus|Jabatan|Divisi|Status|Tipe|BENEFIT|COST|MySQL|lima barista" src database README.md
```

Expected: Ant tests and JAR compilation pass; the remaining search hits, if any, are only historical documentation outside the production/app/seed scope and are reviewed explicitly.

- [ ] **Step 3: Render the final DOCX**

First run the canonical renderer:

```powershell
& 'C:/Users/mahad/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' `
  'C:/Users/mahad/.codex/plugins/cache/openai-primary-runtime/documents/26.805.11740/skills/documents/render_docx.py' `
  'output/BAB_IV - Waroenk Bikers.docx' --output_dir 'output/qa_final' --emit_pdf
```

If LibreOffice is unavailable, use the installed Microsoft Word COM conversion to PDF, rasterize with the bundled `pdftoppm.cmd`, and record that fallback in the final handoff.

- [ ] **Step 4: Inspect every rendered page**

Check every page for clipped text, broken/repeated table headers, row overflow, figure/caption separation, image stretching, page-number/footer drift, and unexpected blank pages. Re-edit and rerender until all pages are clean.

- [ ] **Step 5: Confirm the backup and final artifact**

Verify the backup remains unchanged, the final DOCX opens, the 100-row tables exist, and the final file is the only DOCX cited as the deliverable.
