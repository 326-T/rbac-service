package org.example.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.example.persistence.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final Base64.Encoder encoder;
  private final Base64.Decoder decoder;
  private final String secretKey;
  private final Long ttl;

  public JwtService(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${jwt.ttl}") Long ttl) {
    encoder = Base64.getEncoder();
    decoder = Base64.getDecoder();
    this.secretKey = secretKey;
    this.ttl = ttl;
  }

  public String encode(User user) {
    Date now = new Date();
    String rawJwt = JWT.create()
        .withJWTId(UUID.randomUUID().toString())
        .withIssuer("org.example")
        .withAudience("org.example")
        .withSubject(user.getId().toString())
        .withClaim("name", user.getName())
        .withClaim("email", user.getEmail())
        .withIssuedAt(now)
        .withNotBefore(now)
        .withExpiresAt(new Date(now.getTime() + ttl))
        .sign(Algorithm.HMAC256(secretKey));
    return encoder.encodeToString(rawJwt.getBytes());
  }

  public User decode(String jwt) throws TokenExpiredException, SignatureVerificationException, IllegalArgumentException {
    String rawJwt = new String(decoder.decode(jwt));
    DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(rawJwt);
    return User.builder()
        .id(Long.parseLong(decodedJWT.getSubject()))
        .name(decodedJWT.getClaim("name").asString())
        .email(decodedJWT.getClaim("email").asString())
        .build();
  }
}
