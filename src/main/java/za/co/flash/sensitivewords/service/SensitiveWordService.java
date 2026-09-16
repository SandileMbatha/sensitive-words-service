package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.dto.SensitiveWordRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordResponse;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.exception.DuplicateSensitiveWordException;
import za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD operations for the sensitive words that back {@link SanitizeService}.
 * These endpoints are for internal/admin use only - see the README for details.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    /**
     * Creates a new sensitive word and evicts the sanitize cache so the change takes effect immediately.
     *
     * @param request the word to add
     * @return the created word
     * @throws DuplicateSensitiveWordException if the word already exists
     */
    @CacheEvict(value = SanitizeService.SENSITIVE_WORDS_CACHE, allEntries = true)
    public SensitiveWordResponse createSensitiveWord(SensitiveWordRequest request) {
        String word = request.word().trim();
        if (sensitiveWordRepository.existsByWordIgnoreCase(word)) {
            throw new DuplicateSensitiveWordException("Sensitive word '" + word + "' already exists");
        }

        SensitiveWord saved = sensitiveWordRepository.save(SensitiveWord.builder().word(word).build());
        return toSensitiveWordResponse(saved);
    }

    /**
     * @return every sensitive word, ordered alphabetically
     */
    @Transactional(readOnly = true)
    public List<SensitiveWordResponse> getAllSensitiveWords() {
        return sensitiveWordRepository.findAllByOrderByWordAsc().stream()
                .map(this::toSensitiveWordResponse)
                .toList();
    }

    /**
     * @param id id of the word to fetch
     * @return the matching word
     * @throws SensitiveWordNotFoundException if no word has that id
     */
    @Transactional(readOnly = true)
    public SensitiveWordResponse getSensitiveWordById(Long id) {
        return toSensitiveWordResponse(findSensitiveWordOrThrow(id));
    }

    /**
     * Updates the value of an existing sensitive word and evicts the sanitize cache.
     *
     * @param id      id of the word to update
     * @param request the new value
     * @return the updated word
     * @throws SensitiveWordNotFoundException  if no word has that id
     * @throws DuplicateSensitiveWordException if another word already has the new value
     */
    @CacheEvict(value = SanitizeService.SENSITIVE_WORDS_CACHE, allEntries = true)
    public SensitiveWordResponse updateSensitiveWord(Long id, SensitiveWordRequest request) {
        SensitiveWord existing = findSensitiveWordOrThrow(id);

        String word = request.word().trim();
        sensitiveWordRepository.findByWordIgnoreCase(word)
                .filter(duplicateWord -> !duplicateWord.getId().equals(id))
                .ifPresent(duplicateWord -> {
                    throw new DuplicateSensitiveWordException("Sensitive word '" + word + "' already exists");
                });

        existing.setWord(word);
        return toSensitiveWordResponse(sensitiveWordRepository.save(existing));
    }

    /**
     * Deletes a sensitive word and evicts the sanitize cache.
     *
     * @param id id of the word to delete
     * @throws SensitiveWordNotFoundException if no word has that id
     */
    @CacheEvict(value = SanitizeService.SENSITIVE_WORDS_CACHE, allEntries = true)
    public void deleteSensitiveWord(Long id) {
        SensitiveWord existing = findSensitiveWordOrThrow(id);
        sensitiveWordRepository.delete(existing);
    }

    private SensitiveWord findSensitiveWordOrThrow(Long id) {
        return sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new SensitiveWordNotFoundException("Sensitive word with id " + id + " not found"));
    }

    private SensitiveWordResponse toSensitiveWordResponse(SensitiveWord entity) {
        return new SensitiveWordResponse(
                entity.getId(),
                entity.getWord(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
