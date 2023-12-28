package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserGroupRoleAssignment;

@NoArgsConstructor
@Data
public class UserGroupRoleAssignmentInsertRequest {

  @NotNull
  @Min(1)
  private Long userGroupId;
  @NotNull
  @Min(1)
  private Long roleId;

  public UserGroupRoleAssignment exportEntity() {
    return UserGroupRoleAssignment.builder()
        .userGroupId(userGroupId)
        .roleId(roleId)
        .build();
  }
}
