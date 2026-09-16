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
     * @param name name to look up
     * @return true if a word with this value already exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * @param name name to look up
     * @return the matching word, if any (case-insensitive)
     */
    Optional<SensitiveWord> findByNameIgnoreCase(String name);

    /**
     * @return every word, ordered alphabetically - backs the CRUD "list" endpoint
     */
    List<SensitiveWord> findAllByOrderByNameAsc();

    /**
     * @return just the {@code name} column for every row. Used by {@link za.co.flash.sensitivewords.service.SanitizeService},
     * which only needs the text, not the full entity.
     */
    @Query("select sensitiveWord.name from SensitiveWord sensitiveWord")
    List<String> findAllNames();
}
