CREATE TABLE rbac_user_system_role_permissions
(
    id SERIAL PRIMARY KEY,
    namespace_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    system_role_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rbac_user_system_role_permissions_namespace_id_user_id_system_role_id_unique UNIQUE (namespace_id, user_id, system_role_id),
    CONSTRAINT rbac_user_system_role_permissions_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE,
    CONSTRAINT rbac_user_system_role_permissions_user_id_foreign FOREIGN KEY (user_id) REFERENCES rbac_users (id) ON DELETE CASCADE,
    CONSTRAINT rbac_user_system_role_permissions_system_role_id_foreign FOREIGN KEY (system_role_id) REFERENCES rbac_system_roles (id) ON DELETE CASCADE
);