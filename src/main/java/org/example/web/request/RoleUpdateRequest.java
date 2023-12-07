package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Role;

@NoArgsConstructor
@Data
public class RoleUpdateRequest {

  private String name;

  public Role exportEntity() {
    return Role.builder()
        .name(name)
        .build();
  }
}
