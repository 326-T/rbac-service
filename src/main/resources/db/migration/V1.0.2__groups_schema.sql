CREATE TABLE groups
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_groups_name_unique UNIQUE (name),
    CONSTRAINT user_groups_created_by_foreign FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
);