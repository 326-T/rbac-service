package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Role;

@NoArgsConstructor
@Data
public class RoleInsertRequest {

  private Long namespaceId;
  private String name;

  public Role exportEntity() {
    return Role.builder()
        .namespaceId(namespaceId)
        .name(name)
        .build();
  }
}
