CREATE TABLE target_cluster_belongings
(
    id SERIAL PRIMARY KEY,
    target_id INTEGER NOT NULL,
    cluster_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT target_cluster_belongings_target_id_cluster_id_unique UNIQUE (target_id, cluster_id),
    CONSTRAINT target_belongs_groups_created_by_foreign FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT target_belongs_groups_target_id_foreign FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE,
    CONSTRAINT target_belongs_groups_cluster_id_foreign FOREIGN KEY (cluster_id) REFERENCES clusters (id) ON DELETE CASCADE
);