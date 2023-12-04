package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.User;

@NoArgsConstructor
@Data
public class UserInsertRequest {

  private String name;
  private String email;
  private String password;

  public User exportEntity() {
    return User.builder()
        .name(name)
        .email(email)
        .passwordDigest(password)
        .build();
  }
}
