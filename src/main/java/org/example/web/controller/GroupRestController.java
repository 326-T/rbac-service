package org.example.web.controller;

import org.example.persistence.entity.Group;
import org.example.service.GroupService;
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
@RequestMapping("/rbac-service/v1/groups")
public class GroupRestController {

  private final GroupService groupService;

  public GroupRestController(GroupService groupService) {
    this.groupService = groupService;
  }

  @GetMapping
  public Flux<Group> index() {
    return groupService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return groupService.count();
  }

  @GetMapping("/{id}")
  public Mono<Group> findById(@PathVariable Long id) {
    return groupService.findById(id);
  }

  @PostMapping
  public Mono<Group> save(@RequestBody Group group) {
    return groupService.insert(group);
  }

  @PutMapping("/{id}")
  public Mono<Group> update(@PathVariable Long id, @RequestBody Group group) {
    group.setId(id);
    return groupService.update(group);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return groupService.deleteById(id);
  }
}
