package za.co.flash.sensitivewords.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sanitizes free-text messages by starring out any word that appears on the sensitive words list.
 * <p>
 * Matching is done token by token (case-insensitive, whole word) rather than as a plain substring
 * replace, so a sensitive word only gets starred out when it appears as its own word - e.g.
 * "SELECT" is replaced but "sensitiveWords" is left alone even though it is not a match.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SanitizeService {

    public static final String SENSITIVE_WORDS_CACHE = "sensitiveWords";

    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    private final SensitiveWordService sensitiveWordService;

    /**
     * Replaces every sensitive word in the message with asterisks of the same length. Matching is
     * case-insensitive and whole-word only, so a word is only starred out when it appears as its
     * own token - substrings of other words are left alone.
     *
     * @param message the raw message to sanitize
     * @return the message with sensitive words starred out
     */
    public String sanitizeMessage(String message) {
        Set<String> sensitiveWords = sensitiveWordService.getSensitiveWordsUppercase();

        Matcher matcher = WORD_PATTERN.matcher(message);
        StringBuilder sanitized = new StringBuilder();

        int redactedCount = 0;
        while (matcher.find()) {
            String token = matcher.group();
            boolean isSensitive = sensitiveWords.contains(token.toUpperCase());
            if (isSensitive) {
                redactedCount++;
            }
            String replacement = isSensitive ? "*".repeat(token.length()) : token;
            matcher.appendReplacement(sanitized, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sanitized);

        log.debug("Sanitized message of length {} - {} word(s) redacted", message.length(), redactedCount);

        return sanitized.toString();
    }
}
