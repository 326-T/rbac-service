CREATE TABLE rbac_system_roles
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    namespace_id INTEGER NOT NULL,
    permission VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rbac_role_endpoint_permissions_name_unique UNIQUE (namespace_id, permission),
    CONSTRAINT rbac_role_endpoint_permissions_namespace_id_foreign FOREIGN KEY (namespace_id) REFERENCES rbac_namespaces (id) ON DELETE CASCADE
);