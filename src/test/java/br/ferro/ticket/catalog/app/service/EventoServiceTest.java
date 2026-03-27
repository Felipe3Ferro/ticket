package br.ferro.ticket.catalog.app.service;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.app.mapper.EventoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.domain.repository.EventoRepository;
import br.ferro.ticket.catalog.infra.messaging.EventoProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private EventoMapper eventoMapper;

    @Mock
    private EventoProducer eventoProducer;

    @InjectMocks
    private EventoService eventoService;

    @Test
    @DisplayName("Deve criar um evento, salvar no repositório e notificar o Kafka com sucesso")
    void criarEvento_shouldSaveAndProduceEvent_whenSuccessful() {
        // Arrange
        EventoRequestDTO requestDTO = new EventoRequestDTO(
            "Show de Rock",
            "Um grande show de rock",
            LocalDateTime.now().plusDays(10),
            "Estádio Local",
            new ArrayList<>()
        );

        Evento evento = new Evento();
        evento.setId(UUID.randomUUID());
        evento.setNome(requestDTO.nome());

        EventoResponseDTO responseDTO = new EventoResponseDTO(
            evento.getId(),
            evento.getNome(),
            requestDTO.descricao(),
            requestDTO.dataHora(),
            requestDTO.local(),
            new ArrayList<TipoIngressoResponseDTO>()
        );

        when(eventoMapper.toEntity(any(EventoRequestDTO.class))).thenReturn(evento);
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);
        when(eventoMapper.toResponseDTO(any(Evento.class))).thenReturn(responseDTO);

        // Act
        EventoResponseDTO result = eventoService.criarEvento(requestDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(evento.getId());
        assertThat(result.nome()).isEqualTo(requestDTO.nome());

        // Verify interactions
        verify(eventoRepository).save(evento);
        verify(eventoProducer).enviarEventoCriado(responseDTO);
    }
}
