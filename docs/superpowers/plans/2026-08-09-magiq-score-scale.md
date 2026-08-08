# MAGIQ Score Scale 100 Correction Implementation Plan
> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan with verification checkpoints.

**Goal:** Make the application, database seed, Excel workbook, BAB II–III, BAB IV, and affected screenshots use a fixed maximum score of 100 for all six criteria while preserving the exact three primary-data rows from BAB II–III and changing dummy C3 values to the approved 60–100 cycle.

**Architecture:** Keep ROC weighting and ranking order unchanged. Centralize normalization in the application at `nilai / 100.0`; expose the same scale in the workbook parameter sheet; regenerate document calculation tables from one fixed-100 calculation source; and refresh only screenshots whose displayed calculation results change.

**Tech Stack:** Java 8/NetBeans Ant application, PostgreSQL/Supabase seed SQL, Python and `python-docx` for document maintenance, artifact-tool for `.xlsx`, and the bundled DOCX renderer for visual QA.

## Global Constraints

- Preserve the exact primary rows from BAB II–III: A `90,85,88,90,80,85`; B `85,90,84,88,90,90`; C `88,82,90,85,85,88`.
- Use fixed maximum `100` for every criterion in both primary and dummy calculations.
- Generate dummy C3 with `60 + 5 * (i % 9)` so the sequence is 60, 65, 70, 75, 80, 85, 90, 95, 100 and repeats.
- Do not reintroduce or describe criterion-type fields; preserve the existing six criterion names and ROC order.
- Preserve the plain/no-color Excel presentation and the approved BAB IV table placement/geometry.
- Create timestamped backups before modifying each user-facing source/output artifact.

---

## Task 1: Baseline, backups, and regression test

**Files:** `output/BAB_IV - Waroenk Bikers.docx`, `output/Data Barista dan Penilaian.xlsx`, `C:/Users/mahad/Downloads/BAB 2-3 Rehan 11 Juli 2026.docx`, application sources, SQL, and related generated assets.

1. Record the current git status and render/inspect the current BAB IV and workbook baseline.
2. Copy the current files into a new dated backup directory/name without overwriting previous backups.
3. Modify `test/.../NormalisasiNilaiCalculatorTest.java` first:
   - assert 75 against `{50,75,90}` equals `0.75`, proving normalization no longer uses a column maximum;
   - assert a zero score/zero column returns `0.0`.
4. Run the focused test or Ant test command and record the expected RED failure before changing production code.

## Task 2: Application and data-source behavior

**Files:** `src/com/gibran/waroenkbikers/util/NormalisasiNilaiCalculator.java`, test file, `database/db_magiq_waroenk_bikers.sql`, `README.md`.

1. Change normalization to divide by the fixed scale 100 and return 0 for a zero score.
2. Change the dummy C3 seed expression to `60 + 5 * (i % 9)` and update the seed comment.
3. Update user-facing calculation narrative to say normalization uses the fixed 100 scale, without mentioning criterion types.
4. Run the focused regression test, then `ant -q clean test` and compile/build verification.

## Task 3: Unified calculation source, workbook, and BAB II–III

**Files:** `output/bab4_work/generate_calculations.py`, `output/bab4_work/calculation_source.json`, `output/Data Barista dan Penilaian.xlsx`, and the primary source document.

1. Update the calculation generator to use fixed maxima `[100] * 6`, preserve the primary rows, and generate the new C3 dummy cycle.
2. Regenerate and inspect expected primary values (approximately A `0.8773`, B `0.8680`, C `0.8638`) and dummy ranking data.
3. Update the workbook formulas/parameter sheet so every maximum is visibly 100 and all normalized cells reference those parameters; update every dummy C3 input cell; keep the workbook plain.
4. Update BAB II–III decision/normalization/ranking tables and narrative to use the same primary values and fixed-100 formulas.
5. Verify workbook formulas calculate without errors and compare the workbook’s primary/dummy results against the generated source.

## Task 4: BAB IV and screenshots

**Files:** `output/bab4_work/rebuild_docx_reference.py`, `output/BAB_IV - Waroenk Bikers.docx`, `output/rancangan_layar/*`, `output/tangkapan_layar/*`.

1. Rebuild only Point B from the approved backup/reference layout using the regenerated fixed-100 calculation source.
2. Update all related BAB IV narrative, tables, formulas, and displayed values; retain table placement and dimensions.
3. Refresh mockups and actual calculation/ranking/report screenshots whose values changed; retain the existing color style for design screens.
4. Render every page of BAB IV and inspect all pages for table placement, page breaks, clipping, and inconsistent values.

## Task 5: Final verification and handoff

1. Run application tests/build, SQL static checks, calculation-source checks, workbook formula/error checks, and document render checks.
2. Confirm backups exist and original files remain recoverable.
3. Review git diff/status and summarize exact output paths and verification results.

