package org.example.util.constant;

public class AccessPath {

  public static final String USERS = "/rbac-service/v1/users";
  public static final String NAMESPACES = "/rbac-service/v1/namespaces";
  public static final String TARGETS = "/rbac-service/v1/{namespace-id}/targets";
  public static final String TARGET_GROUPS = "/rbac-service/v1/{namespace-id}/target-groups";

  public static final String SYSTEM_ROLES = "/rbac-service/v1/{namespace-id}/system-roles";

  private AccessPath() {
  }
}
