package org.example.web.response;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.User;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@Data
public class UserResponse {

  @Id
  private Long id;
  private String name;
  private String email;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public UserResponse(User user) {
    this.id = user.getId();
    this.name = user.getName();
    this.email = user.getEmail();
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();
  }
}
