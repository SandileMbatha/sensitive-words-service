package com.sensitivewords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensitivewords.dto.SanitizeRequest;
import com.sensitivewords.service.SanitizeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SanitizeController.class)
class SanitizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SanitizeService sanitizeService;

    @Test
    void sanitize_shouldReturn200_withSanitizedMessage() throws Exception {
        when(sanitizeService.sanitize("SELECT * FROM sensitiveWords"))
                .thenReturn("****** * FROM sensitiveWords");

        mockMvc.perform(post("/api/v1/sanitize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SanitizeRequest("SELECT * FROM sensitiveWords"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalMessage").value("SELECT * FROM sensitiveWords"))
                .andExpect(jsonPath("$.sanitizedMessage").value("****** * FROM sensitiveWords"));
    }

    @Test
    void sanitize_shouldReturn400_whenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/sanitize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SanitizeRequest(" "))))
                .andExpect(status().isBadRequest());
    }
}
