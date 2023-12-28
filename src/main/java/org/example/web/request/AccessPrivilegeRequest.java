package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AccessPrivilegeRequest {

  @NotBlank
  private String path;
  @NotBlank
  private String method;
  @NotBlank
  private String objectId;
}
