CREATE TABLE rbac_user_group_role_assignments
(
    id SERIAL PRIMARY KEY,
    namespace_id INTEGER NOT NULL,
    user_group_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_group_role_assignments_user_group_id_role_id_unique UNIQUE (user_group_id, role_id),
    CONSTRAINT user_group_role_assignments_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE,
    CONSTRAINT user_group_role_assignments_user_group_id_foreign FOREIGN KEY (user_group_id) REFERENCES rbac_user_groups (id) ON DELETE CASCADE,
    CONSTRAINT user_group_role_assignments_role_id_foreign FOREIGN KEY (role_id) REFERENCES rbac_roles (id) ON DELETE CASCADE,
    CONSTRAINT user_group_role_assignments_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);