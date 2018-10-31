-- CREATE USER jooq WITH PASSWORD 'jooq';
-- CREATE DATABASE "test-monitor" OWNER jooq;

-- ALTER SCHEMA public OWNER TO jooq;

DROP SCHEMA public CASCADE;
CREATE SCHEMA public AUTHORIZATION jooq;
COMMENT ON SCHEMA public IS 'standard public schema';

GRANT ALL ON SCHEMA public TO PUBLIC;
GRANT ALL ON SCHEMA public TO jooq;

-- CREATE USER grafanareader WITH PASSWORD 'password';
GRANT USAGE ON SCHEMA public TO grafanareader;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO grafanareader;
