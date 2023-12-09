package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserGroupRoleAssignment;

@NoArgsConstructor
@Data
public class UserGroupRoleAssignmentInsertRequest {

  private Long namespaceId;
  private Long userGroupId;
  private Long roleId;

  public UserGroupRoleAssignment exportEntity() {
    return UserGroupRoleAssignment.builder()
        .namespaceId(namespaceId)
        .userGroupId(userGroupId)
        .roleId(roleId)
        .build();
  }
}
