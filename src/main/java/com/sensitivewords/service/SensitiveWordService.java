package com.sensitivewords.service;

import com.sensitivewords.dto.SensitiveWordRequest;
import com.sensitivewords.dto.SensitiveWordResponse;
import com.sensitivewords.entity.SensitiveWord;
import com.sensitivewords.exception.DuplicateSensitiveWordException;
import com.sensitivewords.exception.SensitiveWordNotFoundException;
import com.sensitivewords.repository.SensitiveWordRepository;
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

    @CacheEvict(value = SanitizeService.SENSITIVE_WORDS_CACHE, allEntries = true)
    public SensitiveWordResponse create(SensitiveWordRequest request) {
        String word = request.word().trim();
        if (sensitiveWordRepository.existsByWordIgnoreCase(word)) {
            throw new DuplicateSensitiveWordException(word);
        }

        SensitiveWord saved = sensitiveWordRepository.save(SensitiveWord.builder().word(word).build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SensitiveWordResponse> getAll() {
        return sensitiveWordRepository.findAllByOrderByWordAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SensitiveWordResponse getById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    @CacheEvict(value = SanitizeService.SENSITIVE_WORDS_CACHE, allEntries = true)
    public SensitiveWordResponse update(Long id, SensitiveWordRequest request) {
        SensitiveWord existing = findEntityOrThrow(id);

        String word = request.word().trim();
        sensitiveWordRepository.findByWordIgnoreCase(word)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new DuplicateSensitiveWordException(word);
                });

        existing.setWord(word);
        return toResponse(sensitiveWordRepository.save(existing));
    }

    @CacheEvict(value = SanitizeService.SENSITIVE_WORDS_CACHE, allEntries = true)
    public void delete(Long id) {
        SensitiveWord existing = findEntityOrThrow(id);
        sensitiveWordRepository.delete(existing);
    }

    private SensitiveWord findEntityOrThrow(Long id) {
        return sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new SensitiveWordNotFoundException(id));
    }

    private SensitiveWordResponse toResponse(SensitiveWord entity) {
        return new SensitiveWordResponse(
                entity.getId(),
                entity.getWord(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
