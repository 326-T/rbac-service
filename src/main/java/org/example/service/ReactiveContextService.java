package org.example.service;

import org.example.persistence.entity.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveContextService {

  public Mono<User> getCurrentUser() {
    return Mono.deferContextual(Mono::just).map(context -> context.get(User.class));
  }
}
