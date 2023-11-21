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
@Table("paths")
public class Path {

  @Id
  private Long id;
  private Long serviceId;
  private String regex;
  private Long createdBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
