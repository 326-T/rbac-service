package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.error.exception.UnauthenticatedException;
import org.example.persistence.entity.User;
import org.example.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Flux<User> findAll() {
    return userRepository.findAll();
  }

  public Mono<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Flux<User> findByUserGroupId(Long userGroupId) {
    return userRepository.findByUserGroupId(userGroupId);
  }

  public Flux<User> findBySystemRoleId(Long systemRoleId) {
    return userRepository.findBySystemRoleId(systemRoleId);
  }

  /**
   * 1. 重複がないか確認する
   * 2. パスワードをハッシュ化する
   * 3. 保存する
   *
   * @param user パスワードが平文の状態で渡される
   *
   * @return 保存されたユーザー
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<User> insert(User user) {
    user.setPasswordDigest(passwordEncoder.encode(user.getPasswordDigest()));
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return userRepository.findByEmail(user.getEmail())
        .flatMap(present -> Mono.<User>error(new RedundantException("User already exists")))
        .switchIfEmpty(Mono.just(user))
        .flatMap(userRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. 変更内容をセットする
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param user name, emailのみ更新可能
   *
   * @return 保存されたユーザー
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<User> update(User user) {
    Mono<User> userMono = userRepository.findById(user.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("User not found")))
        .flatMap(present -> {
          present.setName(user.getName());
          present.setEmail(user.getEmail());
          present.setPasswordDigest(passwordEncoder.encode(user.getPasswordDigest()));
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return userMono.flatMap(u -> userRepository.findByEmail(u.getEmail()))
        .flatMap(present -> Mono.<User>error(new RedundantException("User already exists")))
        .switchIfEmpty(userMono)
        .flatMap(userRepository::save);
  }

  public Mono<User> login(String email, String password) {
    return userRepository.findByEmail(email)
        .filter(user -> passwordEncoder.matches(password, user.getPasswordDigest()))
        .switchIfEmpty(Mono.error(new UnauthenticatedException("email or password is incorrect")));
  }

  public Mono<Void> deleteById(Long id) {
    return userRepository.deleteById(id);
  }
}
