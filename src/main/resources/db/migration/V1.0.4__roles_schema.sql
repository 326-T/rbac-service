CREATE TABLE rbac_roles
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT roles_name_unique UNIQUE (name),
    CONSTRAINT roles_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);