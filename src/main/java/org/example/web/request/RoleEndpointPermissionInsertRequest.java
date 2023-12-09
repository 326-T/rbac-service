package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.RoleEndpointPermission;

@NoArgsConstructor
@Data
public class RoleEndpointPermissionInsertRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotNull
  @Min(1)
  private Long roleId;
  @NotNull
  @Min(1)
  private Long endpointId;

  public RoleEndpointPermission exportEntity() {
    return RoleEndpointPermission.builder()
        .namespaceId(namespaceId)
        .roleId(roleId)
        .endpointId(endpointId)
        .build();
  }
}
