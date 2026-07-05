-- Run once per environment (dev/staging/prod). Not applied automatically —
-- this project has no Flyway/Liquibase (ddl-auto: update only manages tables/columns).
--
-- Usage: psql -h <host> -U <user> -d smeet -f src/main/resources/sql/enable_unaccent_search.sql
-- or:    docker exec -i smeet-postgres psql -U <user> -d smeet < src/main/resources/sql/enable_unaccent_search.sql

CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Postgres' built-in unaccent() is STABLE, not IMMUTABLE, so it can't be used
-- directly in an index expression. Wrap it so the planner treats it as pure —
-- safe here because the 'unaccent' dictionary never changes at runtime.
CREATE OR REPLACE FUNCTION immutable_unaccent(text)
    RETURNS text AS
$$
SELECT public.unaccent('public.unaccent'::regdictionary, $1)
$$ LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT;

CREATE INDEX IF NOT EXISTS idx_users_username_trgm
    ON users USING GIN (immutable_unaccent(lower(username)) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_users_display_name_trgm
    ON users USING GIN (immutable_unaccent(lower(display_name)) gin_trgm_ops);
