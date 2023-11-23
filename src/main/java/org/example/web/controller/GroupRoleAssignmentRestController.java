package org.example.web.controller;

import org.example.persistence.entity.GroupRoleAssignment;
import org.example.service.GroupRoleAssignmentService;
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
@RequestMapping("/rbac-service/v1/groupRoleAssignments")
public class GroupRoleAssignmentRestController {

  private final GroupRoleAssignmentService groupRoleAssignmentService;

  public GroupRoleAssignmentRestController(GroupRoleAssignmentService groupRoleAssignmentService) {
    this.groupRoleAssignmentService = groupRoleAssignmentService;
  }

  @GetMapping
  public Flux<GroupRoleAssignment> index() {
    return groupRoleAssignmentService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return groupRoleAssignmentService.count();
  }

  @GetMapping("/{id}")
  public Mono<GroupRoleAssignment> findById(@PathVariable Long id) {
    return groupRoleAssignmentService.findById(id);
  }

  @PostMapping
  public Mono<GroupRoleAssignment> save(@RequestBody GroupRoleAssignment groupRoleAssignment) {
    return groupRoleAssignmentService.insert(groupRoleAssignment);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return groupRoleAssignmentService.deleteById(id);
  }
}
