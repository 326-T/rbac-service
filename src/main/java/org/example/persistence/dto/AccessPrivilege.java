package org.example.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AccessPrivilege {

  @Column("user_id")
  private Long userId;
  @Column("user_name")
  private String userName;
  @Column("namespace_id")
  private Long namespaceId;
  @Column("namespace_name")
  private String namespaceName;
  @Column("user_group_id")
  private Long userGroupId;
  @Column("user_group_name")
  private String userGroupName;
  @Column("role_id")
  private Long roleId;
  @Column("role_name")
  private String roleName;
  @Column("path_id")
  private Long pathId;
  @Column("path_regex")
  private String pathRegex;
  private String method;
  @Column("target_group_id")
  private Long targetGroupId;
  @Column("target_group_name")
  private String targetGroupName;
  @Column("target_id")
  private Long targetId;
  @Column("object_id_regex")
  private String objectIdRegex;
}
