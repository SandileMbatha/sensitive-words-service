package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
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
        // given: a small, known sensitive word list backing every test in this class
        when(sensitiveWordRepository.findAllWords())
                .thenReturn(List.of("SELECT", "DROP", "WHERE", "TABLE"));
    }

    @Test
    void givenMessageContainingASensitiveWord_whenSanitize_thenOnlyThatWordIsStarredOut() {
        // given
        String message = "SELECT * FROM sensitiveWords";

        // when
        String sanitizedMessage = sanitizeService.sanitizeMessage(message);

        // then: "SELECT" is on the list (6 chars) -> starred. "FROM"/"sensitiveWords" are not -> untouched.
        assertThat(sanitizedMessage).isEqualTo("****** * FROM sensitiveWords");
    }

    @Test
    void givenSensitiveWordsInMixedCase_whenSanitize_thenMatchingIsCaseInsensitive() {
        // given
        String message = "please select and drop the table";

        // when
        String sanitizedMessage = sanitizeService.sanitizeMessage(message);

        // then
        assertThat(sanitizedMessage).isEqualTo("please ****** and **** the *****");
    }

    @Test
    void givenWordThatOnlyPartiallyMatchesASensitiveWord_whenSanitize_thenItIsLeftUntouched() {
        // given: "TABLE" is sensitive but "TABLETOP" is a different, whole word
        String message = "TABLETOP game";

        // when
        String sanitizedMessage = sanitizeService.sanitizeMessage(message);

        // then
        assertThat(sanitizedMessage).isEqualTo("TABLETOP game");
    }

    @Test
    void givenMessageWithNoSensitiveWords_whenSanitize_thenMessageIsReturnedUnchanged() {
        // given
        String message = "Hello there, how are you today?";

        // when
        String sanitizedMessage = sanitizeService.sanitizeMessage(message);

        // then
        assertThat(sanitizedMessage).isEqualTo(message);
    }

    @Test
    void givenEmptyMessage_whenSanitize_thenEmptyStringIsReturned() {
        // given
        String message = "";

        // when
        String sanitizedMessage = sanitizeService.sanitizeMessage(message);

        // then
        assertThat(sanitizedMessage).isEmpty();
    }
}
