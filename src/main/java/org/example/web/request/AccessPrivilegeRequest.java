package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AccessPrivilegeRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotBlank
  private String path;
  @NotBlank
  private String method;
  @NotBlank
  private String objectId;
}
