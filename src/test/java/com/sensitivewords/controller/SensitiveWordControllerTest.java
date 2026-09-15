package com.sensitivewords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensitivewords.dto.SensitiveWordRequest;
import com.sensitivewords.dto.SensitiveWordResponse;
import com.sensitivewords.exception.DuplicateSensitiveWordException;
import com.sensitivewords.exception.SensitiveWordNotFoundException;
import com.sensitivewords.service.SensitiveWordService;
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
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        SensitiveWordResponse response = new SensitiveWordResponse(1L, "DROP", LocalDateTime.now(), LocalDateTime.now());
        when(sensitiveWordService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/sensitive-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SensitiveWordRequest("DROP"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.word").value("DROP"));
    }

    @Test
    void create_shouldReturn400_whenWordIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/sensitive-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SensitiveWordRequest(" "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn409_whenWordAlreadyExists() throws Exception {
        when(sensitiveWordService.create(any())).thenThrow(new DuplicateSensitiveWordException("DROP"));

        mockMvc.perform(post("/api/v1/sensitive-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SensitiveWordRequest("DROP"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Sensitive word 'DROP' already exists"));
    }

    @Test
    void getAll_shouldReturn200_withListOfWords() throws Exception {
        when(sensitiveWordService.getAll()).thenReturn(List.of(
                new SensitiveWordResponse(1L, "DROP", LocalDateTime.now(), LocalDateTime.now()),
                new SensitiveWordResponse(2L, "SELECT", LocalDateTime.now(), LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/v1/sensitive-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].word").value("DROP"));
    }

    @Test
    void getById_shouldReturn200_whenWordExists() throws Exception {
        when(sensitiveWordService.getById(1L))
                .thenReturn(new SensitiveWordResponse(1L, "DROP", LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(get("/api/v1/sensitive-words/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("DROP"));
    }

    @Test
    void getById_shouldReturn404_whenWordDoesNotExist() throws Exception {
        when(sensitiveWordService.getById(99L)).thenThrow(new SensitiveWordNotFoundException(99L));

        mockMvc.perform(get("/api/v1/sensitive-words/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        when(sensitiveWordService.update(eq(1L), any()))
                .thenReturn(new SensitiveWordResponse(1L, "DELETE", LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(put("/api/v1/sensitive-words/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SensitiveWordRequest("DELETE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("DELETE"));
    }

    @Test
    void delete_shouldReturn204_whenWordExists() throws Exception {
        doNothing().when(sensitiveWordService).delete(1L);

        mockMvc.perform(delete("/api/v1/sensitive-words/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404_whenWordDoesNotExist() throws Exception {
        doThrow(new SensitiveWordNotFoundException(99L)).when(sensitiveWordService).delete(99L);

        mockMvc.perform(delete("/api/v1/sensitive-words/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
