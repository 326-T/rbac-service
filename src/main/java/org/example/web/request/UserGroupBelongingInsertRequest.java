package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserGroupBelonging;

@NoArgsConstructor
@Data
public class UserGroupBelongingInsertRequest {

  private Long namespaceId;
  private Long userId;
  private Long userGroupId;

  public UserGroupBelonging exportEntity() {
    return UserGroupBelonging.builder()
        .namespaceId(namespaceId)
        .userId(userId)
        .userGroupId(userGroupId)
        .build();
  }
}
