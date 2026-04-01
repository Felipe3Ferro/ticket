package br.ferro.ticket.catalog.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ResourceNotFoundException;
import br.ferro.ticket.catalog.app.mapper.EventoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.domain.repository.EventoRepository;
import br.ferro.ticket.catalog.infra.messaging.EventoProducer;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class EventoServiceTest {

  @Mock private EventoRepository eventoRepository;

  @Mock private EventoMapper eventoMapper;

  @Mock private EventoProducer eventoProducer;

  @InjectMocks private EventoService eventoService;

  @Test
  @DisplayName("Deve criar um evento, salvar no repositório e notificar o Kafka com sucesso")
  void criarEvento_shouldSaveAndProduceEvent_whenSuccessful() {
    // Arrange
    EventoRequestDTO requestDTO =
        new EventoRequestDTO(
            "Show de Rock",
            "Um grande show de rock",
            LocalDateTime.now().plusDays(10),
            "Estádio Local",
            new ArrayList<>());

    Evento evento = new Evento();
    evento.setId(UUID.randomUUID());
    evento.setNome(requestDTO.nome());

    EventoResponseDTO responseDTO =
        new EventoResponseDTO(
            evento.getId(),
            evento.getNome(),
            requestDTO.descricao(),
            requestDTO.dataHora(),
            requestDTO.local(),
            new ArrayList<TipoIngressoResponseDTO>());

    when(eventoMapper.toEntity(any(EventoRequestDTO.class))).thenReturn(evento);
    when(eventoRepository.save(any(Evento.class))).thenReturn(evento);
    when(eventoMapper.toResponseDTO(any(Evento.class))).thenReturn(responseDTO);

    // Act
    EventoResponseDTO result = eventoService.criarEvento(requestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(evento.getId());
    assertThat(result.nome()).isEqualTo(requestDTO.nome());

    verify(eventoRepository).save(evento);
    verify(eventoProducer).enviarEventoCriado(responseDTO);
  }

  @Test
  @DisplayName("Deve retornar todos os eventos sem queries N+1 quando chamado")
  void listarEventos_shouldReturnAllEvents_whenCalled() {
    // Arrange
    Evento evento = new Evento();
    evento.setId(UUID.randomUUID());
    evento.setNome("Festival de Verão");

    EventoResponseDTO responseDTO =
        new EventoResponseDTO(
            evento.getId(), evento.getNome(), null, LocalDateTime.now().plusDays(30), null, List.of());

    when(eventoRepository.findAllWithTiposIngresso()).thenReturn(List.of(evento));
    when(eventoMapper.toResponseDTO(evento)).thenReturn(responseDTO);

    // Act
    List<EventoResponseDTO> result = eventoService.listarEventos();

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(evento.getId());

    verify(eventoRepository).findAllWithTiposIngresso();
  }

  @Test
  @DisplayName("Deve retornar o evento quando o ID existir")
  void buscarEventoPorId_shouldReturnEvento_whenFound() {
    // Arrange
    UUID id = UUID.randomUUID();
    Evento evento = new Evento();
    evento.setId(id);
    evento.setNome("Carnaval SP");

    EventoResponseDTO responseDTO =
        new EventoResponseDTO(id, evento.getNome(), null, LocalDateTime.now().plusDays(60), null, List.of());

    when(eventoRepository.findByIdWithTiposIngresso(id)).thenReturn(Optional.of(evento));
    when(eventoMapper.toResponseDTO(evento)).thenReturn(responseDTO);

    // Act
    EventoResponseDTO result = eventoService.buscarEventoPorId(id);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);

    verify(eventoRepository).findByIdWithTiposIngresso(id);
  }

  @Test
  @DisplayName("Deve remover evento e publicar no Kafka quando encontrado")
  void removerEvento_shouldDeleteAndPublish_whenFound() {
    // Arrange
    UUID id = UUID.randomUUID();
    Evento evento = new Evento();
    evento.setId(id);

    when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));

    // Act
    eventoService.removerEvento(id);

    // Assert
    verify(eventoRepository).delete(evento);
    verify(eventoProducer).enviarEventoRemovido(id);
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException ao remover quando evento não existir")
  void removerEvento_shouldThrowException_whenNotFound() {
    // Arrange
    UUID id = UUID.randomUUID();

    when(eventoRepository.findById(id)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> eventoService.removerEvento(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(id.toString());

    verify(eventoRepository).findById(id);
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando o ID não existir")
  void buscarEventoPorId_shouldThrowException_whenNotFound() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(eventoRepository.findByIdWithTiposIngresso(id)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> eventoService.buscarEventoPorId(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(id.toString());

    verify(eventoRepository).findByIdWithTiposIngresso(id);
  }
}
