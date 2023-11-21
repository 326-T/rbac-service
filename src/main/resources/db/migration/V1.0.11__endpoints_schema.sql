CREATE TABLE endpoints
(
    id SERIAL PRIMARY KEY,
    path_id INTEGER NOT NULL,
    cluster_id INTEGER NOT NULL,
    method VARCHAR(255) NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT endpoints_path_id_method_unique UNIQUE (path_id, method),
    CONSTRAINT endpoints_path_id_foreign FOREIGN KEY (path_id) REFERENCES paths (id) ON DELETE CASCADE,
    CONSTRAINT endpoints_cluster_id_foreign FOREIGN KEY (cluster_id) REFERENCES clusters (id) ON DELETE CASCADE,
    CONSTRAINT endpoints_created_by_foreign FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
);
