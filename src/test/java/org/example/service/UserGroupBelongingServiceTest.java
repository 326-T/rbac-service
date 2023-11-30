package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.UserGroupBelonging;
import org.example.persistence.repository.UserGroupBelongingRepository;
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
class UserGroupBelongingServiceTest {

  @InjectMocks
  private UserGroupBelongingService userGroupBelongingService;
  @Mock
  private UserGroupBelongingRepository userGroupBelongingRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(userGroupBelongingRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = userGroupBelongingService.count();
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
        UserGroupBelonging userGroupBelonging1 = UserGroupBelonging.builder()
            .id(1L).namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        UserGroupBelonging userGroupBelonging2 = UserGroupBelonging.builder()
            .id(2L).namespaceId(2L).userId(2L).userGroupId(2L).createdBy(2L).build();
        UserGroupBelonging userGroupBelonging3 = UserGroupBelonging.builder()
            .id(3L).namespaceId(3L).userId(3L).userGroupId(3L).createdBy(3L).build();
        when(userGroupBelongingRepository.findAll()).thenReturn(
            Flux.just(userGroupBelonging1, userGroupBelonging2,
                userGroupBelonging3));
        // when
        Flux<UserGroupBelonging> groupFlux = userGroupBelongingService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                    UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId,
                    UserGroupBelonging::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                    UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId,
                    UserGroupBelonging::getCreatedBy)
                .containsExactly(2L, 2L, 2L, 2L, 2L))
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                    UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId,
                    UserGroupBelonging::getCreatedBy)
                .containsExactly(3L, 3L, 3L, 3L, 3L))
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
        UserGroupBelonging userGroupBelonging1 = UserGroupBelonging.builder()
            .id(1L).namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingRepository.findById(1L)).thenReturn(Mono.just(userGroupBelonging1));
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                    UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId,
                    UserGroupBelonging::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
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
        UserGroupBelonging userGroupBelonging1 = UserGroupBelonging.builder()
            .namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingRepository.save(any(UserGroupBelonging.class))).thenReturn(
            Mono.just(userGroupBelonging1));
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.insert(userGroupBelonging1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                    UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId,
                    UserGroupBelonging::getCreatedBy)
                .containsExactly(null, 1L, 1L, 1L, 1L))
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
        when(userGroupBelongingRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userGroupBelongingService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}