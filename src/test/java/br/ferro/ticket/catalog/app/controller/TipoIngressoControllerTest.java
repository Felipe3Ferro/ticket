package br.ferro.ticket.catalog.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.ferro.ticket.catalog.app.dto.TipoIngressoRequestDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ResourceNotFoundException;
import br.ferro.ticket.catalog.app.service.TipoIngressoService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TipoIngressoController.class)
class TipoIngressoControllerTest {

  private static final String BASE_URL = "/api/v1/eventos/{eventoId}/tipos-ingresso";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private TipoIngressoService tipoIngressoService;

  @Test
  @DisplayName("Deve retornar 201 Created quando adicionar tipo de ingresso com dados válidos")
  void adicionar_shouldReturnCreated_whenRequestIsValid() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    TipoIngressoRequestDTO requestDTO =
        new TipoIngressoRequestDTO("VIP", new BigDecimal("150.00"), 100);
    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(UUID.randomUUID(), "VIP", new BigDecimal("150.00"), 100);

    when(tipoIngressoService.adicionar(eq(eventoId), any(TipoIngressoRequestDTO.class)))
        .thenReturn(responseDTO);

    // Act & Assert
    mockMvc
        .perform(
            post(BASE_URL, eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(responseDTO.id().toString())))
        .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
        .andExpect(jsonPath("$.nomeSetor").value("VIP"));
  }

  @Test
  @DisplayName("Deve retornar 400 Bad Request quando adicionar com dados inválidos")
  void adicionar_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    TipoIngressoRequestDTO invalidRequest =
        new TipoIngressoRequestDTO("", new BigDecimal("-10.00"), null);

    // Act & Assert
    mockMvc
        .perform(
            post(BASE_URL, eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Erro de validação"))
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  @DisplayName("Deve retornar 404 quando evento não existir ao adicionar")
  void adicionar_shouldReturnNotFound_whenEventoNotFound() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    TipoIngressoRequestDTO requestDTO =
        new TipoIngressoRequestDTO("Pista", new BigDecimal("50.00"), 500);

    when(tipoIngressoService.adicionar(eq(eventoId), any(TipoIngressoRequestDTO.class)))
        .thenThrow(new ResourceNotFoundException("Evento não encontrado com o ID: " + eventoId));

    // Act & Assert
    mockMvc
        .perform(
            post(BASE_URL, eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Deve retornar 200 com lista de tipos de ingresso quando evento existir")
  void listar_shouldReturnOk_whenEventoExists() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(UUID.randomUUID(), "Pista", new BigDecimal("80.00"), 200);

    when(tipoIngressoService.listarPorEvento(eventoId)).thenReturn(List.of(responseDTO));

    // Act & Assert
    mockMvc
        .perform(get(BASE_URL, eventoId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].nomeSetor").value("Pista"));
  }

  @Test
  @DisplayName("Deve retornar 200 com tipo de ingresso quando ID for válido")
  void buscarPorId_shouldReturnOk_whenFound() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(id, "Camarote", new BigDecimal("300.00"), 50);

    when(tipoIngressoService.buscarPorId(eventoId, id)).thenReturn(responseDTO);

    // Act & Assert
    mockMvc
        .perform(get(BASE_URL + "/{id}", eventoId, id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.nomeSetor").value("Camarote"));
  }

  @Test
  @DisplayName("Deve retornar 404 quando tipo de ingresso não for encontrado pelo ID")
  void buscarPorId_shouldReturnNotFound_whenNotFound() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    when(tipoIngressoService.buscarPorId(eventoId, id))
        .thenThrow(new ResourceNotFoundException("Tipo de ingresso não encontrado com o ID: " + id));

    // Act & Assert
    mockMvc
        .perform(get(BASE_URL + "/{id}", eventoId, id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Deve retornar 200 com tipo atualizado quando dados forem válidos")
  void atualizar_shouldReturnOk_whenRequestIsValid() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    TipoIngressoRequestDTO requestDTO =
        new TipoIngressoRequestDTO("VIP Plus", new BigDecimal("200.00"), 80);
    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(id, "VIP Plus", new BigDecimal("200.00"), 80);

    when(tipoIngressoService.atualizar(eq(eventoId), eq(id), any(TipoIngressoRequestDTO.class)))
        .thenReturn(responseDTO);

    // Act & Assert
    mockMvc
        .perform(
            put(BASE_URL + "/{id}", eventoId, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nomeSetor").value("VIP Plus"));
  }

  @Test
  @DisplayName("Deve retornar 404 ao atualizar quando tipo de ingresso não existir")
  void atualizar_shouldReturnNotFound_whenNotFound() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    TipoIngressoRequestDTO requestDTO =
        new TipoIngressoRequestDTO("VIP", new BigDecimal("150.00"), 100);

    when(tipoIngressoService.atualizar(eq(eventoId), eq(id), any(TipoIngressoRequestDTO.class)))
        .thenThrow(new ResourceNotFoundException("Tipo de ingresso não encontrado com o ID: " + id));

    // Act & Assert
    mockMvc
        .perform(
            put(BASE_URL + "/{id}", eventoId, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Deve retornar 204 No Content ao remover tipo de ingresso existente")
  void remover_shouldReturnNoContent_whenFound() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    // Act & Assert
    mockMvc
        .perform(delete(BASE_URL + "/{id}", eventoId, id))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Deve retornar 404 ao remover quando tipo de ingresso não existir")
  void remover_shouldReturnNotFound_whenNotFound() throws Exception {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    doThrow(new ResourceNotFoundException("Tipo de ingresso não encontrado com o ID: " + id))
        .when(tipoIngressoService)
        .remover(eventoId, id);

    // Act & Assert
    mockMvc
        .perform(delete(BASE_URL + "/{id}", eventoId, id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
