package org.example.web.controller;

import org.example.persistence.entity.User;
import org.example.service.UserService;
import org.example.web.request.UserInsertRequest;
import org.example.web.request.UserUpdateRequest;
import org.example.web.response.UserResponse;
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
  public Flux<UserResponse> index() {
    return userService.findAll().map(UserResponse::new);
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userService.count();
  }

  @GetMapping("/{id}")
  public Mono<UserResponse> findById(@PathVariable Long id) {
    return userService.findById(id).map(UserResponse::new);
  }

  @PostMapping
  public Mono<UserResponse> save(@RequestBody UserInsertRequest request) {
    return userService.insert(request.exportEntity()).map(UserResponse::new);
  }

  @PutMapping("/{id}")
  public Mono<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
    User user = request.exportEntity();
    user.setId(id);
    return userService.update(user).map(UserResponse::new);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userService.deleteById(id);
  }
}
