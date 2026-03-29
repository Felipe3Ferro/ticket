package br.ferro.ticket.catalog.app.service;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ResourceNotFoundException;
import br.ferro.ticket.catalog.app.mapper.EventoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.domain.repository.EventoRepository;
import br.ferro.ticket.catalog.infra.messaging.EventoProducer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoService {

  private final EventoRepository eventoRepository;
  private final EventoMapper eventoMapper;
  private final EventoProducer eventoProducer;

  @Transactional
  public EventoResponseDTO criarEvento(EventoRequestDTO eventoRequestDTO) {
    Evento evento = eventoMapper.toEntity(eventoRequestDTO);
    Evento eventoSalvo = eventoRepository.save(evento);

    EventoResponseDTO eventoResponseDTO = eventoMapper.toResponseDTO(eventoSalvo);
    eventoProducer.enviarEventoCriado(eventoResponseDTO);

    return eventoResponseDTO;
  }

  @Transactional(readOnly = true)
  public List<EventoResponseDTO> listarEventos() {
    return eventoRepository.findAll().stream()
        .map(eventoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public EventoResponseDTO buscarEventoPorId(UUID id) {
    return eventoRepository
        .findById(id)
        .map(eventoMapper::toResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com o ID: " + id));
  }
}
