-- POSTGRES_DB env var already creates serMonTimescaleDB; do not CREATE DATABASE here.

CREATE USER sermon_admin WITH PASSWORD 'Admin@123';

GRANT ALL PRIVILEGES ON DATABASE "serMonTimescaleDB" TO sermon_admin;

\c "serMonTimescaleDB";

CREATE EXTENSION IF NOT EXISTS timescaledb;

GRANT ALL ON SCHEMA public TO sermon_admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO sermon_admin;



