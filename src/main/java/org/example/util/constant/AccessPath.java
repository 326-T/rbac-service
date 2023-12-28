package org.example.util.constant;

public class AccessPath {

  public static final String USERS = "/rbac-service/v1/users";
  public static final String NAMESPACES = "/rbac-service/v1/namespaces";
  public static final String USER_GROUPS = "/rbac-service/v1/{namespace-id}/user-groups";
  public static final String USER_GROUP_BELONGINGS = "/rbac-service/v1/{namespace-id}/user-group-belongings";
  public static final String ROLES = "/rbac-service/v1/{namespace-id}/roles";
  public static final String USER_GROUP_ROLE_ASSIGNMENTS = "/rbac-service/v1/{namespace-id}/group-role-assignments";
  public static final String PATHS = "/rbac-service/v1/{namespace-id}/paths";
  public static final String METHODS = "/rbac-service/v1/methods";
  public static final String TARGETS = "/rbac-service/v1/{namespace-id}/targets";
  public static final String TARGET_GROUPS = "/rbac-service/v1/{namespace-id}/target-groups";
  public static final String TARGET_GROUP_BELONGINGS = "/rbac-service/v1/{namespace-id}/target-group-belongings";
  public static final String ENDPOINTS = "/rbac-service/v1/{namespace-id}/endpoints";
  public static final String ROLE_ENDPOINT_PERMISSIONS = "/rbac-service/v1/{namespace-id}/role-endpoint-permissions";
  public static final String SYSTEM_ROLES = "/rbac-service/v1/{namespace-id}/system-roles";
  public static final String USER_SYSTEM_ROLE_PERMISSIONS = "/rbac-service/v1/{namespace-id}/user-system-role-permissions";

  private AccessPath() {
  }
}
