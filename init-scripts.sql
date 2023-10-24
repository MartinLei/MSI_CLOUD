CREATE TABLE IF NOT EXISTS file_item (
    id SERIAL PRIMARY KEY,
    item_name VARCHAR(255),
    file_name VARCHAR(255),
    content_type VARCHAR(255),
    data BYTEA
);
