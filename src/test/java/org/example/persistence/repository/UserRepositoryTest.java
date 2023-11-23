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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザーの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = userRepository.count();
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
      @DisplayName("ユーザーを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<User> userFlux = userRepository.findAll();
        // then
        StepVerifier.create(userFlux)
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(1L, "user1", "xxx@example.org", "password_digest1", "token1"))
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(2L, "user2", "yyy@example.org", "password_digest2", "token2"))
            .assertNext(
                user -> assertThat(user)
                    .extracting(
                        User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(3L, "user3", "zzz@example.org", "password_digest3", "token3"))
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
      @DisplayName("ユーザーをIDで取得できる")
      void findUserById() {
        // when
        Mono<User> userMono = userRepository.findById(1L);
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user -> assertThat(user)
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(1L, "user1", "xxx@example.org", "password_digest1", "token1"))
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
    class regular {

      @Test
      @DisplayName("ユーザーをIDで更新できる")
      void updateUserById() {
        // given
        User user = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2").token("TOKEN2")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now()).build();
        // when
        Mono<User> userMono = userRepository.save(user);
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2", "TOKEN2"))
            .verifyComplete();
        userRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2", "TOKEN2"))
            .verifyComplete();
      }

      @Test
      @DisplayName("ユーザーを新規登録できる")
      void insertUser() {
        // given
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4").token("token4").build();
        // when
        Mono<User> userMono = userRepository.save(user);
        // then
        StepVerifier.create(userMono)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(4L, "user4", "aaa@example.org", "password_digest4", "token4"))
            .verifyComplete();
        userRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                user1 -> assertThat(user1)
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(4L, "user4", "aaa@example.org", "password_digest4", "token4"))
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
    class regular {

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