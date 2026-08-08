-- Run this once against an existing PostgreSQL/Supabase database after taking a backup.
-- It removes only the obsolete criterion type attribute and preserves user data.
BEGIN;
ALTER TABLE public.kriteria DROP CONSTRAINT IF EXISTS kriteria_tipe_check;
ALTER TABLE public.kriteria DROP COLUMN IF EXISTS tipe;
COMMIT;
