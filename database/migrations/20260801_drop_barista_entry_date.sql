-- Run this once against an existing PostgreSQL/Supabase database after taking a backup.
-- It removes only the obsolete entry-date columns and keeps all other barista data.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'barista' AND column_name = 'date_entry'
  ) THEN
    ALTER TABLE public.barista DROP COLUMN date_entry;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'barista' AND column_name = 'tanggal_masuk'
  ) THEN
    ALTER TABLE public.barista DROP COLUMN tanggal_masuk;
  END IF;
END;
$$;
