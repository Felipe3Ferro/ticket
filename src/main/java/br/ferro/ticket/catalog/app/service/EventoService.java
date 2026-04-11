package br.ferro.ticket.catalog.app.service;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ResourceNotFoundException;
import br.ferro.ticket.catalog.app.mapper.EventoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.domain.repository.EventoRepository;
import br.ferro.ticket.catalog.infra.config.CacheConstants;
import br.ferro.ticket.catalog.infra.messaging.EventoProducer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoService {

  private final EventoRepository eventoRepository;
  private final EventoMapper eventoMapper;
  private final EventoProducer eventoProducer;

  @Transactional
  @CacheEvict(value = CacheConstants.CACHE_EVENTOS, allEntries = true)
  public EventoResponseDTO criarEvento(EventoRequestDTO eventoRequestDTO) {
    Evento evento = eventoMapper.toEntity(eventoRequestDTO);
    evento.definirTiposIngresso(evento.getTiposIngresso());
    Evento eventoSalvo = eventoRepository.save(evento);

    EventoResponseDTO eventoResponseDTO = eventoMapper.toResponseDTO(eventoSalvo);
    eventoProducer.enviarEventoCriado(eventoResponseDTO);

    return eventoResponseDTO;
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CacheConstants.CACHE_EVENTOS)
  public List<EventoResponseDTO> listarEventos() {
    return eventoRepository.findAllWithTiposIngresso().stream()
        .map(eventoMapper::toResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CacheConstants.CACHE_EVENTO, key = "#id")
  public EventoResponseDTO buscarEventoPorId(UUID id) {
    return eventoRepository
        .findByIdWithTiposIngresso(id)
        .map(eventoMapper::toResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com o ID: " + id));
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CacheConstants.CACHE_EVENTO, key = "#id"),
      @CacheEvict(value = CacheConstants.CACHE_EVENTOS, allEntries = true)
  })
  public void removerEvento(UUID id) {
    Evento evento = eventoRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com o ID: " + id));

    eventoRepository.delete(evento);
    eventoProducer.enviarEventoRemovido(id);
  }
}
