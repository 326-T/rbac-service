package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserGroupBelonging;

@NoArgsConstructor
@Data
public class UserGroupBelongingInsertRequest {

  @NotNull
  @Min(1)
  private Long userId;
  @NotNull
  @Min(1)
  private Long userGroupId;

  public UserGroupBelonging exportEntity() {
    return UserGroupBelonging.builder()
        .userId(userId)
        .userGroupId(userGroupId)
        .build();
  }
}
