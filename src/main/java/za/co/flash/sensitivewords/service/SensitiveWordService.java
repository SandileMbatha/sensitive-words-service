package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.dto.SensitiveWordRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordResponse;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.exception.DuplicateSensitiveWordException;
import za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        String name = request.name().trim();
        if (sensitiveWordRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateSensitiveWordException("Sensitive word '" + name + "' already exists");
        }

        SensitiveWord saved = sensitiveWordRepository.save(SensitiveWord.builder().name(name).build());
        return toSensitiveWordResponse(saved);
    }

    /**
     * @return every sensitive word, ordered alphabetically
     */
    @Transactional(readOnly = true)
    public List<SensitiveWordResponse> getAllSensitiveWords() {
        return sensitiveWordRepository.findAllByOrderByNameAsc().stream()
                .map(this::toSensitiveWordResponse)
                .toList();
    }

    /**
     * @param name the name to fetch
     * @return the matching word
     * @throws SensitiveWordNotFoundException if no word matches
     */
    @Transactional(readOnly = true)
    public SensitiveWordResponse getSensitiveWordByName(String name) {
        String trimmedName = name.trim();
        SensitiveWord entity = sensitiveWordRepository.findByNameIgnoreCase(trimmedName)
                .orElseThrow(() -> new SensitiveWordNotFoundException("Sensitive word '" + trimmedName + "' not found"));
        return toSensitiveWordResponse(entity);
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

        String name = request.name().trim();
        sensitiveWordRepository.findByNameIgnoreCase(name)
                .filter(duplicateWord -> !duplicateWord.getId().equals(id))
                .ifPresent(duplicateWord -> {
                    throw new DuplicateSensitiveWordException("Sensitive word '" + name + "' already exists");
                });

        existing.setName(name);
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

    /**
     * The word list rarely changes and is read on every sanitize call, so it is cached in memory.
     * The cache is evicted by the create/update/delete methods above whenever the list changes.
     *
     * @return every sensitive word, upper-cased, for fast case-insensitive lookup
     */
    @Transactional(readOnly = true)
    @Cacheable(SanitizeService.SENSITIVE_WORDS_CACHE)
    public Set<String> getSensitiveWordsUppercase() {
        return sensitiveWordRepository.findAllNames().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    private SensitiveWordResponse toSensitiveWordResponse(SensitiveWord entity) {
        return new SensitiveWordResponse(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
