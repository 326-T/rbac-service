CREATE TABLE rbac_targets
(
    id SERIAL PRIMARY KEY,
    namespace_id INTEGER NOT NULL,
    object_id_regex TEXT NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT targets_object_id_unique UNIQUE (namespace_id, object_id_regex),
    CONSTRAINT targets_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE,
    CONSTRAINT targets_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);
