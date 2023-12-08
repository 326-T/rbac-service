package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.persistence.repository.UserGroupRoleAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class UserGroupRoleAssignmentServiceTest {

  @InjectMocks
  private UserGroupRoleAssignmentService userGroupRoleAssignmentService;
  @Mock
  private UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;

  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(userGroupRoleAssignmentRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = userGroupRoleAssignmentService.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment1 = UserGroupRoleAssignment.builder()
            .id(1L).namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        UserGroupRoleAssignment userGroupRoleAssignment2 = UserGroupRoleAssignment.builder()
            .id(2L).namespaceId(2L).userGroupId(2L).roleId(2L).createdBy(2L).build();
        UserGroupRoleAssignment userGroupRoleAssignment3 = UserGroupRoleAssignment.builder()
            .id(3L).namespaceId(3L).userGroupId(3L).roleId(3L).createdBy(3L).build();
        when(userGroupRoleAssignmentRepository.findAll()).thenReturn(
            Flux.just(userGroupRoleAssignment1, userGroupRoleAssignment2,
                userGroupRoleAssignment3));
        // when
        Flux<UserGroupRoleAssignment> groupFlux = userGroupRoleAssignmentService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupRoleAssignment::getId,
                    UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId,
                    UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupRoleAssignment::getId,
                    UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId,
                    UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(2L, 2L, 2L, 2L, 2L))
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupRoleAssignment::getId,
                    UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId,
                    UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(3L, 3L, 3L, 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループをIDで取得できる")
      void findByIdTheIndex() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment1 = UserGroupRoleAssignment.builder()
            .id(1L).namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.findById(1L)).thenReturn(
            Mono.just(userGroupRoleAssignment1));
        // when
        Mono<UserGroupRoleAssignment> groupMono = userGroupRoleAssignmentService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupRoleAssignment::getId,
                    UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId,
                    UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを登録できる")
      void insertTheIndex() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment1 = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.save(any(UserGroupRoleAssignment.class)))
            .thenReturn(Mono.just(userGroupRoleAssignment1));
        when(userGroupRoleAssignmentRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<UserGroupRoleAssignment> groupMono = userGroupRoleAssignmentService.insert(
            userGroupRoleAssignment1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupRoleAssignment::getId,
                    UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId,
                    UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(null, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class irregular {

      @Test
      @DisplayName("ユーザグループとロールの関係を登録できない")
      void cannotCreateDuplicateUserGroupRoleAssignment() {
        // given
        UserGroupRoleAssignment before = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        UserGroupRoleAssignment after = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.save(any(UserGroupRoleAssignment.class)))
            .thenReturn(Mono.just(after));
        when(userGroupRoleAssignmentRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.just(before));
        // when
        Mono<UserGroupRoleAssignment> groupMono = userGroupRoleAssignmentService.insert(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを削除できる")
      void deleteTheIndex() {
        // given
        when(userGroupRoleAssignmentRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userGroupRoleAssignmentService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}