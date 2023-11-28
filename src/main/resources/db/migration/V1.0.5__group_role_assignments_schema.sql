CREATE TABLE rbac_group_role_assignments
(
    id SERIAL PRIMARY KEY,
    user_group_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_role_assignments_user_group_id_role_id_unique UNIQUE (user_group_id, role_id),
    CONSTRAINT group_role_assignments_user_group_id_foreign FOREIGN KEY (user_group_id) REFERENCES rbac_user_groups (id) ON DELETE CASCADE,
    CONSTRAINT group_role_assignments_role_id_foreign FOREIGN KEY (role_id) REFERENCES rbac_roles (id) ON DELETE CASCADE,
    CONSTRAINT group_role_assignments_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);