package org.example.web.controller;

import org.example.persistence.entity.User;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/users")
public class UserRestController {

  private final UserService userService;

  public UserRestController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public Flux<User> index() {
    return userService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userService.count();
  }

  @GetMapping("/{id}")
  public Mono<User> findById(@PathVariable Long id) {
    return userService.findById(id);
  }

  @PostMapping
  public Mono<User> save(@RequestBody User user) {
    return userService.insert(user);
  }

  @PutMapping("/{id}")
  public Mono<User> update(@PathVariable Long id, @RequestBody User user) {
    user.setId(id);
    return userService.update(user);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userService.deleteById(id);
  }
}
