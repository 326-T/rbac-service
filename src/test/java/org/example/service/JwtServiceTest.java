package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.example.persistence.entity.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtServiceTest {

  @Autowired
  private JwtService jwtService;


  @BeforeAll
  void beforeAll() {
    ReflectionTestUtils.setField(jwtService, "secretKey", "secret");
    ReflectionTestUtils.setField(jwtService, "ttl", 1000L);
  }

  @Nested
  class encode {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("JWTを生成できる")
      void encode() {
        // given
        User user = User.builder().id(1L).name("test").email("aaa@example.org").build();
        // when
        String jwt = jwtService.encode(user);
        // then
        String base64Decoded = new String(java.util.Base64.getDecoder().decode(jwt));
        DecodedJWT jwtDecoded = JWT.require(Algorithm.HMAC256("secret")).build().verify(base64Decoded);
        assertThat(jwtDecoded.getIssuer()).isEqualTo("org.example");
        assertThat(jwtDecoded.getAudience()).isEqualTo(List.of("org.example"));
        assertThat(jwtDecoded.getSubject()).isEqualTo("1");
        assertThat(jwtDecoded.getClaim("name").asString()).isEqualTo("test");
        assertThat(jwtDecoded.getClaim("email").asString()).isEqualTo("aaa@example.org");
      }
    }
  }

  @Nested
  class decode {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("JWTをデコードできる")
      void decode() {
        // given
        Date now = new Date();
        String rawJwt = JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withIssuer("org.example")
            .withAudience("org.example")
            .withSubject("1")
            .withClaim("name", "privilege")
            .withClaim("email", "privilege@example.org")
            .withIssuedAt(now)
            .withNotBefore(now)
            .withExpiresAt(new Date(now.getTime() + 1000L))
            .sign(Algorithm.HMAC256("secret"));
        String jwt = java.util.Base64.getEncoder().encodeToString(rawJwt.getBytes());
        // when
        User user = jwtService.decode(jwt);
        // then
        assertThat(user)
            .extracting(User::getId, User::getName, User::getEmail)
            .containsExactly(1L, "privilege", "privilege@example.org");
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("JWTの署名が不正な場合は例外が発生する")
      void differentKey() {
        // given
        Date now = new Date();
        String rawJwt = JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withIssuer("org.example")
            .withAudience("org.example")
            .withSubject("1")
            .withClaim("name", "privilege")
            .withClaim("email", "privilege@example.org")
            .withIssuedAt(now)
            .withNotBefore(now)
            .withExpiresAt(new Date(now.getTime() + 1000L))
            .sign(Algorithm.HMAC256("invalid_secret"));
        String jwt = java.util.Base64.getEncoder().encodeToString(rawJwt.getBytes());
        // when, then
        assertThrows(SignatureVerificationException.class, () -> jwtService.decode(jwt));
      }

      @Test
      @DisplayName("JWTの有効期限が切れている場合は例外が発生する")
      void expired() {
        // given
        Date now = new Date();
        String rawJwt = JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withIssuer("org.example")
            .withAudience("org.example")
            .withSubject("1")
            .withClaim("name", "privilege")
            .withClaim("email", "privilege@example.org")
            .withIssuedAt(now)
            .withNotBefore(now)
            .withExpiresAt(new Date(now.getTime() - 1000L))
            .sign(Algorithm.HMAC256("secret"));
        String jwt = java.util.Base64.getEncoder().encodeToString(rawJwt.getBytes());
        // when, then
        assertThrows(TokenExpiredException.class, () -> jwtService.decode(jwt));
      }

      @Test
      void notJwtCase() {
        // when, then
        assertThrows(IllegalArgumentException.class, () -> jwtService.decode("not_jwt"));
      }
    }
  }
}