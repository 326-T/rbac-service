CREATE TABLE rbac_endpoints
(
    id SERIAL PRIMARY KEY,
    namespace_id INTEGER NOT NULL,
    path_id INTEGER NOT NULL,
    target_group_id INTEGER NOT NULL,
    method VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT endpoints_namespace_id_path_id_target_group_id_method_unique UNIQUE (namespace_id, path_id, target_group_id, method),
    CONSTRAINT endpoints_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE,
    CONSTRAINT endpoints_path_id_foreign FOREIGN KEY (path_id) REFERENCES rbac_paths (id) ON DELETE CASCADE,
    CONSTRAINT endpoints_target_group_id_foreign FOREIGN KEY (target_group_id) REFERENCES rbac_target_groups (id) ON DELETE CASCADE,
    CONSTRAINT endpoints_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);
