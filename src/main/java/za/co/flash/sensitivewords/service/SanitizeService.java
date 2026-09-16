package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sanitizes free-text messages by starring out any word that appears on the sensitive words list.
 * <p>
 * Matching is done token by token (case-insensitive, whole word) rather than as a plain substring
 * replace, so a sensitive word only gets starred out when it appears as its own word - e.g.
 * "SELECT" is replaced but "sensitiveWords" is left alone even though it is not a match.
 */
@Service
@RequiredArgsConstructor
public class SanitizeService {

    public static final String SENSITIVE_WORDS_CACHE = "sensitiveWords";

    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    private final SensitiveWordRepository sensitiveWordRepository;

    /**
     * Replaces every sensitive word in the message with asterisks of the same length. Matching is
     * case-insensitive and whole-word only, so a word is only starred out when it appears as its
     * own token - substrings of other words are left alone.
     *
     * @param message the raw message to sanitize
     * @return the message with sensitive words starred out
     */
    public String sanitizeMessage(String message) {
        Set<String> sensitiveWords = getSensitiveWordsUppercase();

        Matcher matcher = WORD_PATTERN.matcher(message);
        StringBuilder sanitized = new StringBuilder();

        while (matcher.find()) {
            String token = matcher.group();
            String replacement = sensitiveWords.contains(token.toUpperCase())
                    ? "*".repeat(token.length())
                    : token;
            matcher.appendReplacement(sanitized, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sanitized);

        return sanitized.toString();
    }

    /**
     * The word list rarely changes and is read on every sanitize call, so it is cached in memory.
     * The cache is evicted by {@link SensitiveWordService} whenever a word is created, updated or deleted.
     *
     * @return every sensitive word, upper-cased, for fast case-insensitive lookup
     */
    @Cacheable(SENSITIVE_WORDS_CACHE)
    public Set<String> getSensitiveWordsUppercase() {
        return sensitiveWordRepository.findAllWords().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}
