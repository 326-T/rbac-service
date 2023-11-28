CREATE TABLE rbac_targets
(
    id SERIAL PRIMARY KEY,
    service_id INTEGER NOT NULL,
    object_id_regex TEXT NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT targets_object_id_unique UNIQUE (service_id, object_id_regex),
    CONSTRAINT targets_service_id_foreign FOREIGN KEY (service_id) REFERENCES rbac_services (id) ON DELETE CASCADE,
    CONSTRAINT targets_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);
