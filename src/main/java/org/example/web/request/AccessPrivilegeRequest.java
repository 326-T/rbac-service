package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AccessPrivilegeRequest {

  private Long namespaceId;
  private String path;
  private String method;
  private String objectId;
}
