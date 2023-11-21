CREATE TABLE group_role_assignments
(
    id SERIAL PRIMARY KEY,
    group_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_role_assignments_group_id_role_id_unique UNIQUE (group_id, role_id),
    CONSTRAINT group_role_assignments_group_id_foreign FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT group_role_assignments_role_id_foreign FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT group_role_assignments_created_by_foreign FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
);