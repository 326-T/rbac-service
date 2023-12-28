package org.example.util.constant;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum SystemRolePermission {
  READ("参照権限", "READ"),
  WRITE("編集権限", "WRITE");
  private final String name;
  private final String permission;

  SystemRolePermission(String name, String permission) {
    this.name = name;
    this.permission = permission;
  }

  public static SystemRolePermission of(String permission) {
    return Arrays.stream(SystemRolePermission.values())
        .filter(systemRolePermission -> systemRolePermission.getPermission().equals(permission))
        .findFirst().orElse(READ);
  }
}
