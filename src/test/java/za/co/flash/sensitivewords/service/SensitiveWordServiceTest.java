package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.dto.SensitiveWordRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordResponse;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.exception.DuplicateSensitiveWordException;
import za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @InjectMocks
    private SensitiveWordService sensitiveWordService;

    @Test
    void givenWordDoesNotAlreadyExist_whenCreate_thenWordIsSavedAndReturned() {
        // given
        when(sensitiveWordRepository.existsByNameIgnoreCase("DROP")).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenReturn(buildSensitiveWord(1L, "DROP"));

        // when
        SensitiveWordResponse response = sensitiveWordService.createSensitiveWord(new SensitiveWordRequest("DROP"));

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("DROP");
    }

    @Test
    void givenWordAlreadyExists_whenCreate_thenDuplicateSensitiveWordExceptionIsThrown() {
        // given
        when(sensitiveWordRepository.existsByNameIgnoreCase("DROP")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> sensitiveWordService.createSensitiveWord(new SensitiveWordRequest("DROP")))
                .isInstanceOf(DuplicateSensitiveWordException.class);
        verify(sensitiveWordRepository, never()).save(any());
    }

    @Test
    void givenWordsExist_whenGetAll_thenEveryWordIsReturnedOrderedAlphabetically() {
        // given
        when(sensitiveWordRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(buildSensitiveWord(1L, "DROP"), buildSensitiveWord(2L, "SELECT")));

        // when
        List<SensitiveWordResponse> allSensitiveWords = sensitiveWordService.getAllSensitiveWords();

        // then
        assertThat(allSensitiveWords).hasSize(2);
        assertThat(allSensitiveWords.get(0).name()).isEqualTo("DROP");
        assertThat(allSensitiveWords.get(1).name()).isEqualTo("SELECT");
    }

    @Test
    void givenWordExists_whenGetByName_thenWordIsReturned() {
        // given
        when(sensitiveWordRepository.findByNameIgnoreCase("DROP")).thenReturn(Optional.of(buildSensitiveWord(1L, "DROP")));

        // when
        SensitiveWordResponse response = sensitiveWordService.getSensitiveWordByName("DROP");

        // then
        assertThat(response.name()).isEqualTo("DROP");
    }

    @Test
    void givenNameHasLeadingAndTrailingSpaces_whenGetByName_thenSpacesAreTrimmedBeforeLookup() {
        // given
        when(sensitiveWordRepository.findByNameIgnoreCase("DROP")).thenReturn(Optional.of(buildSensitiveWord(1L, "DROP")));

        // when
        SensitiveWordResponse response = sensitiveWordService.getSensitiveWordByName("  DROP  ");

        // then
        assertThat(response.name()).isEqualTo("DROP");
        verify(sensitiveWordRepository).findByNameIgnoreCase("DROP");
    }

    @Test
    void givenWordDoesNotExist_whenGetByName_thenSensitiveWordNotFoundExceptionIsThrown() {
        // given
        when(sensitiveWordRepository.findByNameIgnoreCase("MISSING")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sensitiveWordService.getSensitiveWordByName("MISSING"))
                .isInstanceOf(SensitiveWordNotFoundException.class);
    }

    @Test
    void givenNewValueIsNotTakenByAnotherRecord_whenUpdate_thenWordIsChanged() {
        // given
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(buildSensitiveWord(1L, "DROP")));
        when(sensitiveWordRepository.findByNameIgnoreCase("DELETE")).thenReturn(Optional.empty());
        when(sensitiveWordRepository.save(any(SensitiveWord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SensitiveWordResponse response = sensitiveWordService.updateSensitiveWord(1L, new SensitiveWordRequest("DELETE"));

        // then
        assertThat(response.name()).isEqualTo("DELETE");
    }

    @Test
    void givenNewValueBelongsToAnotherRecord_whenUpdate_thenDuplicateSensitiveWordExceptionIsThrown() {
        // given
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(buildSensitiveWord(1L, "DROP")));
        when(sensitiveWordRepository.findByNameIgnoreCase("SELECT"))
                .thenReturn(Optional.of(buildSensitiveWord(2L, "SELECT")));

        // when / then
        assertThatThrownBy(() -> sensitiveWordService.updateSensitiveWord(1L, new SensitiveWordRequest("SELECT")))
                .isInstanceOf(DuplicateSensitiveWordException.class);
        verify(sensitiveWordRepository, never()).save(any());
    }

    @Test
    void givenIdDoesNotExist_whenUpdate_thenSensitiveWordNotFoundExceptionIsThrown() {
        // given
        when(sensitiveWordRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sensitiveWordService.updateSensitiveWord(99L, new SensitiveWordRequest("DELETE")))
                .isInstanceOf(SensitiveWordNotFoundException.class);
    }

    @Test
    void givenWordExists_whenDelete_thenWordIsRemoved() {
        // given
        SensitiveWord existing = buildSensitiveWord(1L, "DROP");
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(existing));

        // when
        sensitiveWordService.deleteSensitiveWord(1L);

        // then
        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository, times(1)).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void givenIdDoesNotExist_whenDelete_thenSensitiveWordNotFoundExceptionIsThrown() {
        // given
        when(sensitiveWordRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sensitiveWordService.deleteSensitiveWord(99L))
                .isInstanceOf(SensitiveWordNotFoundException.class);
        verify(sensitiveWordRepository, never()).delete(any());
    }

    @Test
    void givenWordsExist_whenGetSensitiveWordsUppercase_thenEveryNameIsReturnedUppercased() {
        // given
        when(sensitiveWordRepository.findAllNames()).thenReturn(List.of("select", "Drop"));

        // when
        Set<String> uppercaseWords = sensitiveWordService.getSensitiveWordsUppercase();

        // then
        assertThat(uppercaseWords).containsExactlyInAnyOrder("SELECT", "DROP");
    }

    private SensitiveWord buildSensitiveWord(Long id, String name) {
        return SensitiveWord.builder()
                .id(id)
                .name(name)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
