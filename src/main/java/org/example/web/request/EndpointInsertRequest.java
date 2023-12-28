package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Endpoint;

@NoArgsConstructor
@Data
public class EndpointInsertRequest {

  @NotNull
  @Min(1)
  private Long pathId;
  @NotNull
  @Min(1)
  private Long targetGroupId;
  @NotBlank
  private String method;

  public Endpoint exportEntity() {
    return Endpoint.builder()
        .pathId(pathId)
        .targetGroupId(targetGroupId)
        .method(method)
        .build();
  }
}
