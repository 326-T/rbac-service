package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Endpoint;

@NoArgsConstructor
@Data
public class EndpointInsertRequest {

  private Long namespaceId;
  private Long pathId;
  private Long targetGroupId;
  private String method;

  public Endpoint exportEntity() {
    return Endpoint.builder()
        .namespaceId(namespaceId)
        .pathId(pathId)
        .targetGroupId(targetGroupId)
        .method(method)
        .build();
  }
}
