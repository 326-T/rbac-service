package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserGroup;

@NoArgsConstructor
@Data
public class UserGroupInsertRequest {

  private Long namespaceId;
  private String name;

  public UserGroup exportEntity() {
    return UserGroup.builder()
        .namespaceId(namespaceId)
        .name(name)
        .build();
  }
}
