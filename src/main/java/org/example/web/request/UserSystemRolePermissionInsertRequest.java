package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserSystemRolePermission;

@NoArgsConstructor
@Data
public class UserSystemRolePermissionInsertRequest {

  @NotNull
  @Min(1)
  private Long userId;
  @NotNull
  @Min(1)
  private Long systemRoleId;

  public UserSystemRolePermission exportEntity() {
    return UserSystemRolePermission.builder()
        .userId(userId)
        .systemRoleId(systemRoleId)
        .build();
  }
}
