-- Executed on every startup via spring.sql.init.mode=always.
-- SQLite is not treated as an embedded datasource by Spring Boot, so schema
-- initialisation is off by default and has to be switched on explicitly.
CREATE TABLE IF NOT EXISTS stored_image (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    animal       TEXT    NOT NULL,
    content_type TEXT    NOT NULL,
    source_url   TEXT,
    size_bytes   INTEGER NOT NULL,
    data         BLOB    NOT NULL,
    created_at   TEXT    NOT NULL          -- ISO-8601 instant, UTC
);
