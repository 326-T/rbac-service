package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.User;
import org.example.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
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

  public Mono<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Mono<User> insert(User user) {
    user.setPasswordDigest(passwordEncoder.encode(user.getPasswordDigest()));
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return userRepository.findByEmail(user.getEmail())
        .flatMap(present -> Mono.<User>error(new RedundantException("User already exists")))
        .switchIfEmpty(Mono.just(user))
        .flatMap(userRepository::save);
  }

  public Mono<User> update(User user) {
    return userRepository.findById(user.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("User not found")))
        .flatMap(present -> {
          present.setName(user.getName());
          present.setEmail(user.getEmail());
          present.setPasswordDigest(passwordEncoder.encode(user.getPasswordDigest()));
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        })
        .flatMap(userRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return userRepository.deleteById(id);
  }
}
