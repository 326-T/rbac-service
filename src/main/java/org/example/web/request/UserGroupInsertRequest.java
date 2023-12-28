package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.UserGroup;

@NoArgsConstructor
@Data
public class UserGroupInsertRequest {

  @NotBlank
  private String name;

  public UserGroup exportEntity() {
    return UserGroup.builder()
        .name(name)
        .build();
  }
}
