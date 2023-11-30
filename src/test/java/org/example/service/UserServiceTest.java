package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.persistence.repository.UserRepository;
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
class UserServiceTest {

  @InjectMocks
  private UserService userService;
  @Mock
  private UserRepository userRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(userRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = userService.count();
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
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("password_digest1").token("token1")
            .build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("password_digest2").token("token2")
            .build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("password_digest3").token("token3")
            .build();
        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2, user3));
        // when
        Flux<User> groupFlux = userService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(user -> assertThat(user)
                .extracting(
                    User::getId, User::getName, User::getEmail,
                    User::getPasswordDigest, User::getToken)
                .containsExactly(1L, "user1", "xxx@example.org", "password_digest1", "token1"))
            .assertNext(user -> assertThat(user)
                .extracting(
                    User::getId, User::getName, User::getEmail,
                    User::getPasswordDigest, User::getToken)
                .containsExactly(2L, "user2", "yyy@example.org", "password_digest2", "token2"))
            .assertNext(user -> assertThat(user)
                .extracting(
                    User::getId, User::getName, User::getEmail,
                    User::getPasswordDigest, User::getToken)
                .containsExactly(3L, "user3", "zzz@example.org", "password_digest3", "token3"))
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
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("password_digest1").token("token1")
            .build();
        when(userRepository.findById(1L)).thenReturn(Mono.just(user1));
        // when
        Mono<User> groupMono = userService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail,
                    User::getPasswordDigest, User::getToken)
                .containsExactly(1L, "user1", "xxx@example.org", "password_digest1", "token1"))
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
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4").token("token4")
            .build();
        when(userRepository.save(any(User.class)))
            .thenReturn(Mono.just(user));
        // when
        Mono<User> groupMono = userService.insert(user);
        // then
        StepVerifier.create(groupMono)
            .assertNext(user1 -> assertThat(user1)
                .extracting(User::getId, User::getName, User::getEmail,
                    User::getPasswordDigest, User::getToken)
                .containsExactly(null, "user4", "aaa@example.org", "password_digest4", "token4"))
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
        User user = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2").token("TOKEN2")
            .build();
        when(userRepository.findById(2L)).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenReturn(
            Mono.just(user));
        // when
        Mono<User> groupMono = userService.update(user);
        // then
        StepVerifier.create(groupMono)
            .assertNext(user1 -> assertThat(user1)
                .extracting(User::getId, User::getName, User::getEmail,
                    User::getPasswordDigest, User::getToken)
                .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2", "TOKEN2"))
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
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}