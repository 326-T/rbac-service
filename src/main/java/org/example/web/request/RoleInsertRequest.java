package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Role;

@NoArgsConstructor
@Data
public class RoleInsertRequest {

  @NotBlank
  private String name;

  public Role exportEntity() {
    return Role.builder()
        .name(name)
        .build();
  }
}
