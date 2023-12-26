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
@Table("rbac_user_system_role_permissions")
public class UserSystemRolePermission {

  @Id
  private Long id;
  private Long userId;
  private Long systemRoleId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
