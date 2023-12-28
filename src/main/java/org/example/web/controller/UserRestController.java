package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.User;
import org.example.service.UserService;
import org.example.util.constant.AccessPath;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.USERS)
public class UserRestController {

  private final UserService userService;

  public UserRestController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public Flux<UserResponse> index(@RequestParam(value = "user-group-id", required = false) Long userGroupId) {
    if (userGroupId == null) {
      return userService.findAll().map(UserResponse::new);
    }
    return userService.findByUserGroupId(userGroupId).map(UserResponse::new);
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userService.count();
  }

  @GetMapping("/{id}")
  public Mono<UserResponse> findById(@PathVariable Long id) {
    return userService.findById(id).map(UserResponse::new);
  }

  @GetMapping("/system")
  public Flux<UserResponse> findBySystemRoleId(@RequestParam("system-role-id") Long systemRoleId) {
    return userService.findBySystemRoleId(systemRoleId).map(UserResponse::new);
  }

  @PostMapping
  public Mono<UserResponse> save(@Valid @RequestBody UserInsertRequest request) {
    return userService.insert(request.exportEntity()).map(UserResponse::new);
  }

  @PutMapping("/{id}")
  public Mono<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
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
