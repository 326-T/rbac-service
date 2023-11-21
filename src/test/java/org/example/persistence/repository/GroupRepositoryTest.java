package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Group;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class GroupRepositoryTest {

  @Autowired
  private GroupRepository groupRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = groupRepository.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Group> userGroupFlux = groupRepository.findAll();
        // then
        StepVerifier.create(userGroupFlux)
            .assertNext(
                group -> assertThat(group)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(1L, "group1", 1L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(2L, "group2", 2L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(3L, "group3", 3L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループをIDで取得できる")
      void findUserById() {
        // when
        Mono<Group> userGroupMono = groupRepository.findById(1L);
        // then
        StepVerifier.create(userGroupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(1L, "group1", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループを更新できる")
      void updateUserGroup() {
        // given
        Group group = Group.builder()
            .id(2L)
            .name("group4")
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Group> userGroupMono = groupRepository.save(group);
        // then
        StepVerifier.create(userGroupMono)
            .assertNext(
                group1 -> assertThat(group1)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(2L, "group4", 1L))
            .verifyComplete();
        groupRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                group1 -> assertThat(group1)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(2L, "group4", 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ユーザグループを新規登録できる")
      void insertUserGroup() {
        // given
        Group group = Group.builder()
            .name("guest")
            .createdBy(1L)
            .build();
        // when
        Mono<Group> userGroupMono = groupRepository.save(group);
        // then
        StepVerifier.create(userGroupMono)
            .assertNext(
                group1 -> assertThat(group1)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(4L, "guest", 1L))
            .verifyComplete();
        groupRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                group1 -> assertThat(group1)
                    .extracting(Group::getId, Group::getName, Group::getCreatedBy)
                    .containsExactly(4L, "guest", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループをIDで削除できる")
      void deleteUserGroupById() {
        // when
        Mono<Void> voidMono = groupRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        groupRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}