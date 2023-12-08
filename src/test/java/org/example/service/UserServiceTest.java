package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.User;
import org.example.persistence.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class UserServiceTest {

  @InjectMocks
  private UserService userService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザの件数を取得できる")
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
      @DisplayName("ユーザを全件取得できる")
      void findAllTheIndexes() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("password_digest1")
            .build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("password_digest2")
            .build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("password_digest3")
            .build();
        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2, user3));
        // when
        Flux<User> groupFlux = userService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(1L, "user1", "xxx@example.org", "password_digest1"))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(2L, "user2", "yyy@example.org", "password_digest2"))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(3L, "user3", "zzz@example.org", "password_digest3"))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザをIDで取得できる")
      void findByIdTheIndex() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("password_digest1")
            .build();
        when(userRepository.findById(1L)).thenReturn(Mono.just(user1));
        // when
        Mono<User> userMono = userService.findById(1L);
        // then
        StepVerifier.create(userMono)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(1L, "user1", "xxx@example.org", "password_digest1"))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザを登録できる")
      void insertTheIndex() {
        // given
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4")
            .build();
        when(userRepository.findByEmail("aaa@example.org")).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
        when(passwordEncoder.encode("password_digest4")).thenReturn("password_digest4");
        // when
        Mono<User> userMono = userService.insert(user);
        // then
        StepVerifier.create(userMono)
            .assertNext(user1 -> assertThat(user1)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(null, "user4", "aaa@example.org", "password_digest4"))
            .verifyComplete();
      }
    }

    @Nested
    class irregular {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateWithDuplicateEmail() {
        // given
        User before = User.builder()
            .id(2L).name("user2").email("xxx@example.org")
            .passwordDigest("password_digest2")
            .build();
        User after = User.builder()
            .id(2L).name("user4").email("xxx@example.org")
            .passwordDigest("password_digest4")
            .build();
        when(userRepository.findByEmail("xxx@example.org")).thenReturn(Mono.just(before));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(after));
        when(passwordEncoder.encode("password_digest4")).thenReturn("password_digest4");
        // when
        Mono<User> userMono = userService.insert(after);
        // then
        StepVerifier.create(userMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザを更新できる")
      void updateTheIndex() {
        // given
        User before = User.builder()
            .id(2L).name("user2").email("xxx@example.org")
            .passwordDigest("password_digest2")
            .build();
        User after = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2")
            .build();
        when(userRepository.findById(2L)).thenReturn(Mono.just(before));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(after));
        when(passwordEncoder.encode("PASSWORD_DIGEST2")).thenReturn("PASSWORD_DIGEST2");
        // when
        Mono<User> userMono = userService.update(after);
        // then
        StepVerifier.create(userMono)
            .assertNext(user1 -> assertThat(user1)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2"))
            .verifyComplete();
      }
    }

    @Nested
    class irregular {

      @Test
      @DisplayName("存在しないユーザの場合はエラーになる")
      void notExistingUserCauseException() {
        User after = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2")
            .build();
        when(userRepository.findById(2L)).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(after));
        when(passwordEncoder.encode("PASSWORD_DIGEST2")).thenReturn("PASSWORD_DIGEST2");
        // when
        Mono<User> userMono = userService.update(after);
        // then
        StepVerifier.create(userMono).expectError(NotExistingException.class).verify();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザを削除できる")
      void deleteTheIndex() {
        // given
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> userMono = userService.deleteById(1L);
        // then
        StepVerifier.create(userMono).verifyComplete();
      }
    }
  }
}