CREATE TABLE user_group_belongings
(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    group_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_group_belongings_user_id_group_id_unique UNIQUE (user_id, group_id),
    CONSTRAINT user_group_belongings_user_id_foreign FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_group_belongings_group_id_foreign FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT user_group_belongings_created_by_foreign FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
);