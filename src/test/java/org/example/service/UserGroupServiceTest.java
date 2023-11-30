package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.UserGroup;
import org.example.persistence.repository.UserGroupRepository;
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
class UserGroupServiceTest {

  @InjectMocks
  private UserGroupService userGroupService;
  @Mock
  private UserGroupRepository userGroupRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(userGroupRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = userGroupService.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Nested
  class FindAll {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        UserGroup userGroup1 = UserGroup.builder()
            .id(1L).namespaceId(1L).name("group1").createdBy(1L).build();
        UserGroup userGroup2 = UserGroup.builder()
            .id(2L).namespaceId(2L).name("group2").createdBy(2L).build();
        UserGroup userGroup3 = UserGroup.builder()
            .id(3L).namespaceId(3L).name("group3").createdBy(3L).build();
        when(userGroupRepository.findAll()).thenReturn(Flux.just(userGroup1, userGroup2,
            userGroup3));
        // when
        Flux<UserGroup> groupFlux = userGroupService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(
                group -> assertThat(group)
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "group1", 1L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(2L, 2L, "group2", 2L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(3L, 3L, "group3", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループをIDで取得できる")
      void findByIdTheIndex() {
        // given
        UserGroup userGroup1 = UserGroup.builder()
            .id(1L).namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(userGroup1));
        // when
        Mono<UserGroup> groupMono = userGroupService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "group1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループを登録できる")
      void insertTheIndex() {
        // given
        UserGroup userGroup1 = UserGroup.builder()
            .namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(
            Mono.just(userGroup1));
        // when
        Mono<UserGroup> groupMono = userGroupService.insert(userGroup1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(null, 1L, "group1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループを更新できる")
      void updateTheIndex() {
        // given
        UserGroup userGroup1 = UserGroup.builder()
            .id(1L).namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(userGroup1));
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(
            Mono.just(userGroup1));
        // when
        Mono<UserGroup> groupMono = userGroupService.update(userGroup1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "group1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループを削除できる")
      void deleteTheIndex() {
        // given
        when(userGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userGroupService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}