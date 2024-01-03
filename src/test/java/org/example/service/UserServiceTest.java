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
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを全件取得できる")
      void findAllTheIndexes() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq").build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.").build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y").build();
        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2, user3));
        // when
        Flux<User> groupFlux = userService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(1L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(2L, "user2", "yyy@example.org", "$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U."))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(3L, "user3", "zzz@example.org", "$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y"))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindByEmail {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザをメールアドレスで取得できる")
      void findByEmail() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq").build();
        when(userRepository.findByEmail("xxx@example.org")).thenReturn(Mono.just(user1));
        // when
        Mono<User> userMono = userService.findByEmail("xxx@example.org");
        // then
        StepVerifier.create(userMono)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(1L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindByUserGroupId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザをuserGroupIdで全件取得できる")
      void findByUserGroupId() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq").build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.").build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y").build();
        when(userRepository.findByUserGroupId(1L)).thenReturn(Flux.just(user1, user2, user3));
        // when
        Flux<User> groupFlux = userService.findByUserGroupId(1L);
        // then
        StepVerifier.create(groupFlux)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(1L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(2L, "user2", "yyy@example.org", "$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U."))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(3L, "user3", "zzz@example.org", "$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y"))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindBySystemRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザをsystemRoleIdで全件取得できる")
      void findBySystemRoleId() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq").build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.").build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y").build();
        when(userRepository.findBySystemRoleId(1L)).thenReturn(Flux.just(user1, user2, user3));
        // when
        Flux<User> userFlux = userService.findBySystemRoleId(1L);
        // then
        StepVerifier.create(userFlux)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(1L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(2L, "user2", "yyy@example.org", "$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U."))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                .containsExactly(3L, "user3", "zzz@example.org", "$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y"))
            .verifyComplete();
      }
    }
  }

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを登録できる")
      void insertTheIndex() {
        // given
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4").build();
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
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateWithDuplicateEmail() {
        // given
        User before = User.builder()
            .id(2L).name("user2").email("xxx@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.").build();
        User after = User.builder()
            .id(2L).name("user4").email("xxx@example.org")
            .passwordDigest("password_digest4").build();
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
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを更新できる")
      void updateTheIndex() {
        // given
        User before = User.builder()
            .id(2L).name("user2").email("xxx@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.").build();
        User after = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2").build();
        when(userRepository.findById(2L)).thenReturn(Mono.just(before));
        when(userRepository.findByEmail("bbb@example.org")).thenReturn(Mono.empty());
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
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないユーザの場合はエラーになる")
      void notExistingUserCauseException() {
        User after = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2").build();
        when(userRepository.findById(2L)).thenReturn(Mono.empty());
        when(userRepository.findByEmail("bbb@example.org")).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(after));
        when(passwordEncoder.encode("PASSWORD_DIGEST2")).thenReturn("PASSWORD_DIGEST2");
        // when
        Mono<User> userMono = userService.update(after);
        // then
        StepVerifier.create(userMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicateEmail() {
        // given
        User before = User.builder()
            .id(2L).name("user2").email("xxx@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.").build();
        User after = User.builder()
            .id(2L).name("USER2").email("yyy@example.org")
            .passwordDigest("PASSWORD_DIGEST2").build();
        User duplicate = User.builder()
            .id(3L).name("user3").email("yyy@example.org")
            .passwordDigest("$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y").build();
        when(userRepository.findById(2L)).thenReturn(Mono.just(before));
        when(userRepository.findByEmail("yyy@example.org")).thenReturn(Mono.just(duplicate));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(after));
        when(passwordEncoder.encode("PASSWORD_DIGEST2")).thenReturn("PASSWORD_DIGEST2");
        // when
        Mono<User> userMono = userService.update(after);
        // then
        StepVerifier.create(userMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

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