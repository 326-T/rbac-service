package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.User;

@NoArgsConstructor
@Data
public class UserInsertRequest {

  @NotBlank
  private String name;
  @NotBlank
  private String email;
  @NotBlank
  private String password;

  public User exportEntity() {
    return User.builder()
        .name(name)
        .email(email)
        .passwordDigest(password)
        .build();
  }
}
