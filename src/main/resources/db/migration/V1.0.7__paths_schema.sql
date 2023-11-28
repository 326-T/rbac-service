CREATE TABLE rbac_paths
(
    id SERIAL PRIMARY KEY,
    service_id INTEGER NOT NULL,
    regex VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT paths_regex_unique UNIQUE (service_id, regex),
    CONSTRAINT paths_service_id_foreign FOREIGN KEY (service_id) REFERENCES rbac_services (id) ON DELETE CASCADE,
    CONSTRAINT paths_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);