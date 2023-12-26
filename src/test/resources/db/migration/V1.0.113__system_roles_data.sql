INSERT INTO rbac_system_roles (namespace_id, name, permission)
VALUES
    (1, 'develop_参照権限', 'READ'),
    (1, 'develop_編集権限', 'WRITE'),
    (2, 'staging_参照権限', 'READ'),
    (2, 'staging_編集権限', 'WRITE'),
    (3, 'production_参照権限', 'READ'),
    (3, 'production_編集権限', 'WRITE');