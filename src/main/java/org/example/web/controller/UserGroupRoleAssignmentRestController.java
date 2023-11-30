package org.example.web.controller;

import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.service.UserGroupRoleAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/group-role-assignments")
public class UserGroupRoleAssignmentRestController {

  private final UserGroupRoleAssignmentService userGroupRoleAssignmentService;

  public UserGroupRoleAssignmentRestController(
      UserGroupRoleAssignmentService userGroupRoleAssignmentService) {
    this.userGroupRoleAssignmentService = userGroupRoleAssignmentService;
  }

  @GetMapping
  public Flux<UserGroupRoleAssignment> index() {
    return userGroupRoleAssignmentService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userGroupRoleAssignmentService.count();
  }

  @GetMapping("/{id}")
  public Mono<UserGroupRoleAssignment> findById(@PathVariable Long id) {
    return userGroupRoleAssignmentService.findById(id);
  }

  @PostMapping
  public Mono<UserGroupRoleAssignment> save(
      @RequestBody UserGroupRoleAssignment userGroupRoleAssignment) {
    return userGroupRoleAssignmentService.insert(userGroupRoleAssignment);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userGroupRoleAssignmentService.deleteById(id);
  }
}
