package org.example.persistence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EndpointDetail {

  private Long id;
  @Column("namespace_id")
  private Long namespaceId;
  @Column("path_id")
  private Long pathId;
  @Column("path_regex")
  private String pathRegex;
  @Column("target_group_id")
  private Long targetGroupId;
  @Column("target_group_name")
  private String targetGroupName;
  private String method;
  @Column("created_by")
  private Long createdBy;
  @Column("created_at")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @Column("updated_at")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;
}
