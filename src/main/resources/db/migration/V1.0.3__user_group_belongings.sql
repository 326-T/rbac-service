CREATE TABLE rbac_user_group_belongings
(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    user_group_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_group_belongings_user_id_user_group_id_unique UNIQUE (user_id, user_group_id),
    CONSTRAINT user_group_belongings_user_id_foreign FOREIGN KEY (user_id) REFERENCES rbac_users (id) ON DELETE CASCADE,
    CONSTRAINT user_group_belongings_user_group_id_foreign FOREIGN KEY (user_group_id) REFERENCES rbac_user_groups (id) ON DELETE CASCADE,
    CONSTRAINT user_group_belongings_created_by_foreign FOREIGN KEY (created_by) REFERENCES rbac_users (id) ON DELETE CASCADE
);