package com.sensitivewords.repository;

import com.sensitivewords.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    boolean existsByWordIgnoreCase(String word);

    Optional<SensitiveWord> findByWordIgnoreCase(String word);

    List<SensitiveWord> findAllByOrderByWordAsc();

    // Only pulls back the word column - the sanitize endpoint doesn't need the rest of the entity.
    @Query("select w.word from SensitiveWord w")
    List<String> findAllWords();
}
