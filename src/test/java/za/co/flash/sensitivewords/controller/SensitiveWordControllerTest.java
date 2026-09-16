package za.co.flash.sensitivewords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.flash.sensitivewords.dto.SensitiveWordRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordResponse;
import za.co.flash.sensitivewords.exception.DuplicateSensitiveWordException;
import za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException;
import za.co.flash.sensitivewords.service.SensitiveWordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensitiveWordController.class)
class SensitiveWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SensitiveWordService sensitiveWordService;

    @Test
    void givenValidRequest_whenCreate_then201IsReturnedWithTheCreatedWord() throws Exception {
        // given
        SensitiveWordResponse response = new SensitiveWordResponse(1L, "DROP", LocalDateTime.now(), LocalDateTime.now());
        when(sensitiveWordService.createSensitiveWord(any())).thenReturn(response);

        // when
        var result = mockMvc.perform(post("/api/v1/sensitive-words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SensitiveWordRequest("DROP"))));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.word").value("DROP"));
    }

    @Test
    void givenBlankWord_whenCreate_then400IsReturned() throws Exception {
        // given
        SensitiveWordRequest blankRequest = new SensitiveWordRequest(" ");

        // when
        var result = mockMvc.perform(post("/api/v1/sensitive-words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void givenWordAlreadyExists_whenCreate_then409IsReturned() throws Exception {
        // given
        when(sensitiveWordService.createSensitiveWord(any()))
                .thenThrow(new DuplicateSensitiveWordException("Sensitive word 'DROP' already exists"));

        // when
        var result = mockMvc.perform(post("/api/v1/sensitive-words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SensitiveWordRequest("DROP"))));

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Sensitive word 'DROP' already exists"));
    }

    @Test
    void givenWordsExist_whenGetAll_then200IsReturnedWithTheFullList() throws Exception {
        // given
        when(sensitiveWordService.getAllSensitiveWords()).thenReturn(List.of(
                new SensitiveWordResponse(1L, "DROP", LocalDateTime.now(), LocalDateTime.now()),
                new SensitiveWordResponse(2L, "SELECT", LocalDateTime.now(), LocalDateTime.now())
        ));

        // when
        var result = mockMvc.perform(get("/api/v1/sensitive-words"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].word").value("DROP"));
    }

    @Test
    void givenWordExists_whenGetById_then200IsReturnedWithTheWord() throws Exception {
        // given
        when(sensitiveWordService.getSensitiveWordById(1L))
                .thenReturn(new SensitiveWordResponse(1L, "DROP", LocalDateTime.now(), LocalDateTime.now()));

        // when
        var result = mockMvc.perform(get("/api/v1/sensitive-words/{id}", 1L));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("DROP"));
    }

    @Test
    void givenWordDoesNotExist_whenGetById_then404IsReturned() throws Exception {
        // given
        when(sensitiveWordService.getSensitiveWordById(99L))
                .thenThrow(new SensitiveWordNotFoundException("Sensitive word with id 99 not found"));

        // when
        var result = mockMvc.perform(get("/api/v1/sensitive-words/{id}", 99L));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void givenValidRequest_whenUpdate_then200IsReturnedWithTheUpdatedWord() throws Exception {
        // given
        when(sensitiveWordService.updateSensitiveWord(eq(1L), any()))
                .thenReturn(new SensitiveWordResponse(1L, "DELETE", LocalDateTime.now(), LocalDateTime.now()));

        // when
        var result = mockMvc.perform(put("/api/v1/sensitive-words/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SensitiveWordRequest("DELETE"))));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("DELETE"));
    }

    @Test
    void givenWordExists_whenDelete_then204IsReturned() throws Exception {
        // given
        doNothing().when(sensitiveWordService).deleteSensitiveWord(1L);

        // when
        var result = mockMvc.perform(delete("/api/v1/sensitive-words/{id}", 1L));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    void givenWordDoesNotExist_whenDelete_then404IsReturned() throws Exception {
        // given
        doThrow(new SensitiveWordNotFoundException("Sensitive word with id 99 not found"))
                .when(sensitiveWordService).deleteSensitiveWord(99L);

        // when
        var result = mockMvc.perform(delete("/api/v1/sensitive-words/{id}", 99L));

        // then
        result.andExpect(status().isNotFound());
    }
}
