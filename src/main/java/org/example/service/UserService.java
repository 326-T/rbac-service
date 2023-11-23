package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.User;
import org.example.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Mono<Long> count() {
    return userRepository.count();
  }

  public Flux<User> findAll() {
    return userRepository.findAll();
  }

  public Mono<User> findById(Long id) {
    return userRepository.findById(id);
  }

  public Mono<User> insert(User user) {
    if (Objects.nonNull(user.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return userRepository.save(user);
  }

  public Mono<User> update(User user) {
    return userRepository.findById(user.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("User not found"));
      }
      user.setUpdatedAt(LocalDateTime.now());
      user.setCreatedAt(present.getCreatedAt());
      return userRepository.save(user);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return userRepository.deleteById(id);
  }
}
