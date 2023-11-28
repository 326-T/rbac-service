CREATE TABLE rbac_target_group_belongings
(
    id SERIAL PRIMARY KEY,
    target_id INTEGER NOT NULL,
    target_group_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT target_group_belongings_target_id_target_group_id_unique UNIQUE (target_id, target_group_id),
    CONSTRAINT target_group_belongings_target_id_foreign FOREIGN KEY (target_id) REFERENCES rbac_targets (id) ON DELETE CASCADE,
    CONSTRAINT target_group_belongings_target_group_id_foreign FOREIGN KEY (target_group_id) REFERENCES rbac_target_groups (id) ON DELETE CASCADE,
    CONSTRAINT target_group_belongings_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);