-- 1. Drop the hypertables explicitly using CASCADE to break foreign key blocks
DROP TABLE IF EXISTS public.cpu_metrics CASCADE;
DROP TABLE IF EXISTS public.memory_metrics CASCADE;
DROP TABLE IF EXISTS public.disk_metrics CASCADE;
DROP TABLE IF EXISTS public.network_metrics CASCADE;

-- 2. Drop the parent lookup table that the hypertables depend on
DROP TABLE IF EXISTS public.nodes CASCADE;

-- 3. Catch-all: Drop any remaining regular tables in public schema safely (excluding Flyway history)
        DO $$
DECLARE
    r RECORD;
BEGIN
            FOR r IN (
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = 'public'
          AND tablename != 'flyway_schema_history'
    ) LOOP
    EXECUTE 'DROP TABLE IF EXISTS public.' || quote_ident(r.tablename) || ' CASCADE';
END LOOP;
END $$;