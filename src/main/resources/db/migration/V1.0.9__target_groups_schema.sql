CREATE TABLE rbac_target_groups
(
    id SERIAL PRIMARY KEY,
    namespace_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT target_groups_name_unique UNIQUE (namespace_id, name),
    CONSTRAINT target_groups_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE,
    CONSTRAINT target_groups_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);