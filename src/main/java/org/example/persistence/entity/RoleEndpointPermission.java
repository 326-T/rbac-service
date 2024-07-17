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
@Table("rbac_role_endpoint_permissions")
public class RoleEndpointPermission extends BaseAuditEntity {

  @Id
  private Long id;
  private Long namespaceId;
  private Long roleId;
  private Long endpointId;
}
