package br.ferro.ticket.catalog.app.controller;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoRequestDTO;
import br.ferro.ticket.catalog.app.service.EventoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoController.class)
class EventoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private EventoService eventoService;

        @Test
        @DisplayName("Deve criar um evento e retornar 201 Created quando a requisição for válida")
        void criarEvento_shouldReturnCreated_whenRequestIsValid() throws Exception {
                // Arrange
                EventoRequestDTO requestDTO = new EventoRequestDTO(
                                "Festival de Jazz",
                                "O melhor do jazz contemporâneo",
                                LocalDateTime.now().plusMonths(1),
                                "Clube de Jazz",
                                Collections.emptyList());

                EventoResponseDTO responseDTO = new EventoResponseDTO(
                                UUID.randomUUID(),
                                requestDTO.nome(),
                                requestDTO.descricao(),
                                requestDTO.dataHora(),
                                requestDTO.local(),
                                Collections.emptyList());

                when(eventoService.criarEvento(any(EventoRequestDTO.class))).thenReturn(responseDTO);

                // Act & Assert
                mockMvc.perform(post("/api/v1/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(header().exists("Location"))
                                .andExpect(header().string("Location", containsString(responseDTO.id().toString())))
                                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                                .andExpect(jsonPath("$.nome").value(responseDTO.nome()));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando a requisição for inválida (nome em branco, preço negativo)")
        void criarEvento_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
                // Arrange
                // Invalid: nome is blank and tiposIngresso[0].preco is negative
                EventoRequestDTO invalidRequest = new EventoRequestDTO(
                                "", // Blank name
                                "Descrição",
                                LocalDateTime.now().plusDays(1),
                                "Local",
                                Collections.singletonList(
                                                new TipoIngressoRequestDTO("VIP", new BigDecimal("-10.00"), 100) // Negative
                                                                                                                 // price
                                ));

                // Act & Assert
                mockMvc.perform(post("/api/v1/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Erro de validação"))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors[?(@ =~ /nome:.*must not be blank/)]").exists())
                                .andExpect(jsonPath(
                                                "$.errors[?(@ =~ /tiposIngresso.*preco:.*must be greater than or equal to 0/)]")
                                                .exists());
        }
}
