package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.RoleEndpointPermission;

@NoArgsConstructor
@Data
public class RoleEndpointPermissionInsertRequest {

  private Long namespaceId;
  private Long roleId;
  private Long endpointId;

  public RoleEndpointPermission exportEntity() {
    return RoleEndpointPermission.builder()
        .namespaceId(namespaceId)
        .roleId(roleId)
        .endpointId(endpointId)
        .build();
  }
}
