package za.co.flash.sensitivewords.repository;

import za.co.flash.sensitivewords.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link SensitiveWord}.
 */
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    /**
     * @param word word to look up
     * @return true if a word with this value already exists (case-insensitive)
     */
    boolean existsByWordIgnoreCase(String word);

    /**
     * @param word word to look up
     * @return the matching word, if any (case-insensitive)
     */
    Optional<SensitiveWord> findByWordIgnoreCase(String word);

    /**
     * @return every word, ordered alphabetically - backs the CRUD "list" endpoint
     */
    List<SensitiveWord> findAllByOrderByWordAsc();

    /**
     * @return just the {@code word} column for every row. Used by {@link za.co.flash.sensitivewords.service.SanitizeService},
     * which only needs the text, not the full entity.
     */
    @Query("select sensitiveWord.word from SensitiveWord sensitiveWord")
    List<String> findAllWords();
}
