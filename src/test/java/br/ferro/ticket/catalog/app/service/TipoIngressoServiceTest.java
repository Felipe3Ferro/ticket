package br.ferro.ticket.catalog.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.ferro.ticket.catalog.app.dto.TipoIngressoRequestDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ResourceNotFoundException;
import br.ferro.ticket.catalog.app.mapper.TipoIngressoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.domain.entity.TipoIngresso;
import br.ferro.ticket.catalog.domain.repository.EventoRepository;
import br.ferro.ticket.catalog.domain.repository.TipoIngressoRepository;
import br.ferro.ticket.catalog.infra.messaging.TipoIngressoProducer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TipoIngressoServiceTest {

  @Mock private TipoIngressoRepository tipoIngressoRepository;
  @Mock private EventoRepository eventoRepository;
  @Mock private TipoIngressoMapper tipoIngressoMapper;
  @Mock private TipoIngressoProducer tipoIngressoProducer;

  @InjectMocks private TipoIngressoService tipoIngressoService;

  @Test
  @DisplayName("Deve adicionar tipo de ingresso, salvar e publicar no Kafka quando evento existir")
  void adicionar_shouldSaveAndProduceEvent_whenEventoExists() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    Evento evento = new Evento();
    evento.setId(eventoId);

    TipoIngressoRequestDTO requestDTO =
        new TipoIngressoRequestDTO("VIP", new BigDecimal("150.00"), 100);

    TipoIngresso entity = new TipoIngresso();
    entity.setId(UUID.randomUUID());

    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(entity.getId(), "VIP", new BigDecimal("150.00"), 100);

    when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
    when(tipoIngressoMapper.toEntity(requestDTO)).thenReturn(entity);
    when(tipoIngressoRepository.save(entity)).thenReturn(entity);
    when(tipoIngressoMapper.toResponseDTO(entity)).thenReturn(responseDTO);

    // Act
    TipoIngressoResponseDTO result = tipoIngressoService.adicionar(eventoId, requestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.nomeSetor()).isEqualTo("VIP");

    verify(eventoRepository).findById(eventoId);
    verify(tipoIngressoRepository).save(entity);
    verify(tipoIngressoProducer).enviarTipoIngressoCriado(responseDTO);
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando evento não existir ao adicionar")
  void adicionar_shouldThrowException_whenEventoNotFound() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    TipoIngressoRequestDTO requestDTO =
        new TipoIngressoRequestDTO("Pista", new BigDecimal("50.00"), 500);

    when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> tipoIngressoService.adicionar(eventoId, requestDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(eventoId.toString());

    verify(eventoRepository).findById(eventoId);
  }

  @Test
  @DisplayName("Deve retornar lista de tipos de ingresso do evento quando chamado")
  void listarPorEvento_shouldReturnList_whenEventoExists() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    TipoIngresso entity = new TipoIngresso();
    entity.setId(UUID.randomUUID());

    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(entity.getId(), "Pista", new BigDecimal("80.00"), 200);

    when(eventoRepository.existsById(eventoId)).thenReturn(true);
    when(tipoIngressoRepository.findAllByEventoId(eventoId)).thenReturn(List.of(entity));
    when(tipoIngressoMapper.toResponseDTO(entity)).thenReturn(responseDTO);

    // Act
    List<TipoIngressoResponseDTO> result = tipoIngressoService.listarPorEvento(eventoId);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).nomeSetor()).isEqualTo("Pista");

    verify(tipoIngressoRepository).findAllByEventoId(eventoId);
  }

  @Test
  @DisplayName("Deve retornar tipo de ingresso quando ID e eventoId forem válidos")
  void buscarPorId_shouldReturnTipoIngresso_whenFound() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    TipoIngresso entity = new TipoIngresso();
    entity.setId(id);

    TipoIngressoResponseDTO responseDTO =
        new TipoIngressoResponseDTO(id, "Camarote", new BigDecimal("300.00"), 50);

    when(tipoIngressoRepository.findByIdAndEventoId(id, eventoId)).thenReturn(Optional.of(entity));
    when(tipoIngressoMapper.toResponseDTO(entity)).thenReturn(responseDTO);

    // Act
    TipoIngressoResponseDTO result = tipoIngressoService.buscarPorId(eventoId, id);

    // Assert
    assertThat(result.id()).isEqualTo(id);
    verify(tipoIngressoRepository).findByIdAndEventoId(id, eventoId);
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando tipo de ingresso não existir")
  void buscarPorId_shouldThrowException_whenNotFound() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    when(tipoIngressoRepository.findByIdAndEventoId(id, eventoId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> tipoIngressoService.buscarPorId(eventoId, id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  @DisplayName("Deve remover tipo de ingresso quando encontrado")
  void remover_shouldDelete_whenFound() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    TipoIngresso existente = new TipoIngresso();
    existente.setId(id);

    when(tipoIngressoRepository.findByIdAndEventoId(id, eventoId))
        .thenReturn(Optional.of(existente));

    // Act
    tipoIngressoService.remover(eventoId, id);

    // Assert
    verify(tipoIngressoRepository).delete(existente);
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException ao remover quando tipo de ingresso não existir")
  void remover_shouldThrowException_whenNotFound() {
    // Arrange
    UUID eventoId = UUID.randomUUID();
    UUID id = UUID.randomUUID();

    when(tipoIngressoRepository.findByIdAndEventoId(id, eventoId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> tipoIngressoService.remover(eventoId, id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(id.toString());
  }
}
