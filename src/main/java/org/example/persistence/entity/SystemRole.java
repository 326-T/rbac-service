package org.example.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Table("rbac_system_roles")
public class SystemRole extends BaseAuditEntity {

  @Id
  private Long id;
  private String name;
  private Long namespaceId;
  private String permission;
}
