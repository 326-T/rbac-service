package org.example.service;

import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class Base64Service {

  private final Base64.Encoder encoder;
  private final Base64.Decoder decoder;

  public Base64Service() {
    encoder = Base64.getEncoder();
    decoder = Base64.getDecoder();
  }

  public String encode(String raw) {
    return encoder.encodeToString(raw.getBytes());
  }

  public String decode(String encoded) {
    return new String(decoder.decode(encoded));
  }
}
