CREATE TABLE role_endpoint_permissions
(
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL,
    endpoint_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT role_endpoint_permissions_role_id_endpoint_id_unique UNIQUE (role_id, endpoint_id),
    CONSTRAINT group_has_roles_created_by_foreign FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT group_has_roles_role_id_foreign FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT role_endpoint_permissions_endpoint_id_foreign FOREIGN KEY (endpoint_id) REFERENCES endpoints (id) ON DELETE CASCADE
);