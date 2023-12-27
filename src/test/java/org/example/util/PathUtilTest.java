package org.example.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PathUtilTest {

  @Nested
  class GetNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @ParameterizedTest
      @ValueSource(strings = {"/rbac-service/v1/1/targets", "/rbac-service/v2/1/roles"})
      @DisplayName("パスからnamespaceIdを取得する")
      void getNamespaceId(String path) {
        // when
        Long namespaceId = PathUtil.getNamespaceId(path);
        // then
        assertThat(namespaceId).isEqualTo(1L);
      }
    }
  }
}