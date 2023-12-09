package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Path;
import org.example.persistence.repository.PathRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PathService {

  private final PathRepository pathRepository;

  public PathService(PathRepository pathRepository) {
    this.pathRepository = pathRepository;
  }

  public Mono<Long> count() {
    return pathRepository.count();
  }

  public Flux<Path> findAll() {
    return pathRepository.findAll();
  }

  public Mono<Path> findById(Long id) {
    return pathRepository.findById(id);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param path 保存するPath
   *
   * @return 保存されたPath
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<Path> insert(Path path) {
    path.setCreatedAt(LocalDateTime.now());
    path.setUpdatedAt(LocalDateTime.now());
    return pathRepository.findDuplicate(path.getNamespaceId(), path.getRegex())
        .flatMap(present -> Mono.<Path>error(new RedundantException("Path already exists")))
        .switchIfEmpty(Mono.just(path))
        .flatMap(pathRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. 変更内容をセットする
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param path regexのみ変更可能
   *
   * @return 更新されたPath
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<Path> update(Path path) {
    Mono<Path> pathMono = pathRepository.findById(path.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("Path not found")))
        .flatMap(present -> {
          present.setRegex(path.getRegex());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return pathMono.flatMap(e -> pathRepository.findDuplicate(e.getNamespaceId(), e.getRegex()))
        .flatMap(present -> Mono.<Path>error(new RedundantException("Path already exists")))
        .switchIfEmpty(pathMono)
        .flatMap(pathRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return pathRepository.deleteById(id);
  }
}
