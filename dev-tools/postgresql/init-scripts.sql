CREATE TABLE IF NOT EXISTS fileitem (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    data BYTEA
);
