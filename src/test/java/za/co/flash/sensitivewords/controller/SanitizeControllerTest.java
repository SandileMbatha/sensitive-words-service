package za.co.flash.sensitivewords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.flash.sensitivewords.dto.SanitizeRequest;
import za.co.flash.sensitivewords.service.SanitizeService;
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
    void givenMessageContainingASensitiveWord_whenSanitize_then200IsReturnedWithTheSanitizedMessage() throws Exception {
        // given
        when(sanitizeService.sanitizeMessage("SELECT * FROM sensitiveWords"))
                .thenReturn("****** * FROM sensitiveWords");

        // when
        var result = mockMvc.perform(post("/api/v1/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SanitizeRequest("SELECT * FROM sensitiveWords"))));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.originalMessage").value("SELECT * FROM sensitiveWords"))
                .andExpect(jsonPath("$.sanitizedMessage").value("****** * FROM sensitiveWords"));
    }

    @Test
    void givenBlankMessage_whenSanitize_then400IsReturned() throws Exception {
        // given
        SanitizeRequest blankRequest = new SanitizeRequest(" ");

        // when
        var result = mockMvc.perform(post("/api/v1/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }
}
