package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.User;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<User> userFlux = userRepository.findAll();
        // then
        StepVerifier.create(userFlux)
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(1L, "privilege", "privilege@example.org", "$2a$10$Weqah6oP9KO2AiOFuRir4.2sQslZM.99vTjgaopnUKOhyzK2zCr22"))
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(2L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(3L, "user2", "yyy@example.org", "$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U."))
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(4L, "user3", "zzz@example.org", "$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y"))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーをIDで取得できる")
      void findUserById() {
        // when
        Mono<User> userMono = userRepository.findById(2L);
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user -> assertThat(user)
                    .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(2L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByEmail {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーをメールアドレスで取得できる")
      void findUserByEmail() {
        // when
        Mono<User> userMono = userRepository.findByEmail("xxx@example.org");
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user -> assertThat(user)
                    .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(1L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"));
      }
    }
  }

  @Order(1)
  @Nested
  class FindByUserGroupId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーをグループIDで取得できる")
      void findAllTheIndexes() {
        // when
        Flux<User> userFlux = userRepository.findByUserGroupId(1L);
        // then
        StepVerifier.create(userFlux)
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(2L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindBySystemRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーをシステムロールIDで取得できる")
      void canFindBySystemRoleId() {
        // when
        Flux<User> userFlux = userRepository.findBySystemRoleId(2L);
        // then
        StepVerifier.create(userFlux)
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(1L, "privilege", "privilege@example.org", "$2a$10$Weqah6oP9KO2AiOFuRir4.2sQslZM.99vTjgaopnUKOhyzK2zCr22"))
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(2L, "user1", "xxx@example.org", "$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq"))
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
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーをIDで更新できる")
      void updateUserById() {
        // given
        User user = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now()).build();
        // when
        Mono<User> userMono = userRepository.save(user);
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2"))
            .verifyComplete();
        userRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2"))
            .verifyComplete();
      }

      @Test
      @DisplayName("ユーザーを新規登録できる")
      void insertUser() {
        // given
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4").build();
        // when
        Mono<User> userMono = userRepository.save(user);
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(5L, "user4", "aaa@example.org", "password_digest4"))
            .verifyComplete();
        userRepository.findById(5L).as(StepVerifier::create)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail, User::getPasswordDigest)
                    .containsExactly(5L, "user4", "aaa@example.org", "password_digest4"))
            .verifyComplete();

      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーをIDで削除できる")
      void deleteUserById() {
        // when
        Mono<Void> voidMono = userRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        userRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}