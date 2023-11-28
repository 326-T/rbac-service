package org.example.persistence.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table("rbac_users")
public class User {

  @Id
  private Long id;
  private String name;
  private String email;
  private String passwordDigest;
  private String token;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

