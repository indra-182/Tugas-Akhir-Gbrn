-- Run this once against an existing PostgreSQL/Supabase database after taking a backup.
-- It adds explicit criterion priorities and derives their initial values from
-- the existing ROC weights without deleting criteria or assessment data.
BEGIN;

DO $$
DECLARE
  perlu_inisialisasi BOOLEAN := FALSE;
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'kriteria'
      AND column_name = 'urutan_prioritas'
  ) THEN
    ALTER TABLE public.kriteria ADD COLUMN urutan_prioritas INTEGER;
    perlu_inisialisasi := TRUE;
  ELSIF EXISTS (
    SELECT 1
    FROM public.kriteria
    WHERE urutan_prioritas IS NULL
  ) THEN
    perlu_inisialisasi := TRUE;
  END IF;

  IF perlu_inisialisasi THEN
    WITH prioritas_awal AS (
      SELECT id,
             ROW_NUMBER() OVER (ORDER BY bobot DESC, kode, id)::INTEGER AS urutan
      FROM public.kriteria
    )
    UPDATE public.kriteria k
    SET urutan_prioritas = p.urutan
    FROM prioritas_awal p
    WHERE k.id = p.id;
  END IF;
END;
$$;

ALTER TABLE public.kriteria
  ALTER COLUMN urutan_prioritas SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_kriteria_urutan_prioritas'
  ) THEN
    ALTER TABLE public.kriteria
      ADD CONSTRAINT uq_kriteria_urutan_prioritas UNIQUE (urutan_prioritas);
  END IF;
END;
$$;

COMMIT;
