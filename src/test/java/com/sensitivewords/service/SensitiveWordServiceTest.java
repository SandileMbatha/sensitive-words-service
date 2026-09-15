package com.sensitivewords.service;

import com.sensitivewords.dto.SensitiveWordRequest;
import com.sensitivewords.dto.SensitiveWordResponse;
import com.sensitivewords.entity.SensitiveWord;
import com.sensitivewords.exception.DuplicateSensitiveWordException;
import com.sensitivewords.exception.SensitiveWordNotFoundException;
import com.sensitivewords.repository.SensitiveWordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    void create_shouldSaveWord_whenItDoesNotAlreadyExist() {
        when(sensitiveWordRepository.existsByWordIgnoreCase("DROP")).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class)))
                .thenReturn(word(1L, "DROP"));

        SensitiveWordResponse response = sensitiveWordService.create(new SensitiveWordRequest("DROP"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.word()).isEqualTo("DROP");
    }

    @Test
    void create_shouldThrowDuplicateSensitiveWordException_whenWordAlreadyExists() {
        when(sensitiveWordRepository.existsByWordIgnoreCase("DROP")).thenReturn(true);

        assertThatThrownBy(() -> sensitiveWordService.create(new SensitiveWordRequest("DROP")))
                .isInstanceOf(DuplicateSensitiveWordException.class);

        verify(sensitiveWordRepository, never()).save(any());
    }

    @Test
    void getAll_shouldReturnEveryWordOrderedAlphabetically() {
        when(sensitiveWordRepository.findAllByOrderByWordAsc())
                .thenReturn(List.of(word(1L, "DROP"), word(2L, "SELECT")));

        List<SensitiveWordResponse> result = sensitiveWordService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).word()).isEqualTo("DROP");
        assertThat(result.get(1).word()).isEqualTo("SELECT");
    }

    @Test
    void getById_shouldReturnWord_whenItExists() {
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(word(1L, "DROP")));

        SensitiveWordResponse response = sensitiveWordService.getById(1L);

        assertThat(response.word()).isEqualTo("DROP");
    }

    @Test
    void getById_shouldThrowNotFoundException_whenIdDoesNotExist() {
        when(sensitiveWordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensitiveWordService.getById(99L))
                .isInstanceOf(SensitiveWordNotFoundException.class);
    }

    @Test
    void update_shouldChangeTheWord_whenNewValueIsNotTakenByAnotherRecord() {
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(word(1L, "DROP")));
        when(sensitiveWordRepository.findByWordIgnoreCase("DELETE")).thenReturn(Optional.empty());
        when(sensitiveWordRepository.save(any(SensitiveWord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SensitiveWordResponse response = sensitiveWordService.update(1L, new SensitiveWordRequest("DELETE"));

        assertThat(response.word()).isEqualTo("DELETE");
    }

    @Test
    void update_shouldThrowDuplicateSensitiveWordException_whenNewValueBelongsToAnotherRecord() {
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(word(1L, "DROP")));
        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(word(2L, "SELECT")));

        assertThatThrownBy(() -> sensitiveWordService.update(1L, new SensitiveWordRequest("SELECT")))
                .isInstanceOf(DuplicateSensitiveWordException.class);

        verify(sensitiveWordRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowNotFoundException_whenIdDoesNotExist() {
        when(sensitiveWordRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensitiveWordService.update(99L, new SensitiveWordRequest("DELETE")))
                .isInstanceOf(SensitiveWordNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveWord_whenItExists() {
        SensitiveWord existing = word(1L, "DROP");
        when(sensitiveWordRepository.findById(1L)).thenReturn(Optional.of(existing));

        sensitiveWordService.delete(1L);

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository, times(1)).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenIdDoesNotExist() {
        when(sensitiveWordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensitiveWordService.delete(99L))
                .isInstanceOf(SensitiveWordNotFoundException.class);

        verify(sensitiveWordRepository, never()).delete(any());
    }

    private SensitiveWord word(Long id, String word) {
        return SensitiveWord.builder()
                .id(id)
                .word(word)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
