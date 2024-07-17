INSERT INTO rbac_system_roles (
  namespace_id,
  name, permission,
  created_by, updated_by
)
VALUES
    (1, 'develop_参照権限', 'READ', 1, 1),
    (1, 'develop_編集権限', 'WRITE', 1, 1),
    (2, 'staging_参照権限', 'READ', 2, 2),
    (2, 'staging_編集権限', 'WRITE', 2, 2),
    (3, 'production_参照権限', 'READ', 3, 3),
    (3, 'production_編集権限', 'WRITE', 3, 3);