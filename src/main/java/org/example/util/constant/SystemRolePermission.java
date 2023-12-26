package org.example.util.constant;

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
}
