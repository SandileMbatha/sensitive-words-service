package com.sensitivewords.service;

import com.sensitivewords.repository.SensitiveWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanitizeServiceTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @InjectMocks
    private SanitizeService sanitizeService;

    @BeforeEach
    void setUp() {
        when(sensitiveWordRepository.findAllWords())
                .thenReturn(List.of("SELECT", "DROP", "WHERE", "TABLE"));
    }

    @Test
    void sanitize_shouldStarOutSensitiveWordsOnly_matchingTheAssignmentExample() {
        String result = sanitizeService.sanitize("SELECT * FROM sensitiveWords");

        // "SELECT" is on the list (6 chars) -> starred. "FROM" and "sensitiveWords" are not -> untouched.
        assertThat(result).isEqualTo("****** * FROM sensitiveWords");
    }

    @Test
    void sanitize_shouldMatchCaseInsensitively() {
        String result = sanitizeService.sanitize("please select and drop the table");

        assertThat(result).isEqualTo("please ****** and **** the *****");
    }

    @Test
    void sanitize_shouldOnlyReplaceWholeWords_notPartialMatches() {
        // "TABLE" is sensitive but "TABLETOP" is a different word and must be left untouched.
        String result = sanitizeService.sanitize("TABLETOP game");

        assertThat(result).isEqualTo("TABLETOP game");
    }

    @Test
    void sanitize_shouldReturnMessageUnchanged_whenNoSensitiveWordsPresent() {
        String result = sanitizeService.sanitize("Hello there, how are you today?");

        assertThat(result).isEqualTo("Hello there, how are you today?");
    }

    @Test
    void sanitize_shouldReturnEmptyString_whenMessageIsEmpty() {
        String result = sanitizeService.sanitize("");

        assertThat(result).isEmpty();
    }
}
