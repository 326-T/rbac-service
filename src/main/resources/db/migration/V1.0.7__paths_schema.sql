CREATE TABLE rbac_paths
(
    id SERIAL PRIMARY KEY,
    namespace_id INTEGER NOT NULL,
    regex VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT paths_regex_unique UNIQUE (namespace_id, regex),
    CONSTRAINT paths_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE,
    CONSTRAINT paths_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);