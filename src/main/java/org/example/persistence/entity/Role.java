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
@Table("roles")
public class Role {

  @Id
  private Long id;
  private String name;
  private Long createdBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
