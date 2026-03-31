package br.ferro.ticket.catalog.app.service;

import br.ferro.ticket.catalog.app.dto.TipoIngressoRequestDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ResourceNotFoundException;
import br.ferro.ticket.catalog.app.mapper.TipoIngressoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.domain.entity.TipoIngresso;
import br.ferro.ticket.catalog.domain.repository.EventoRepository;
import br.ferro.ticket.catalog.domain.repository.TipoIngressoRepository;
import br.ferro.ticket.catalog.infra.config.CacheConstants;
import br.ferro.ticket.catalog.infra.messaging.TipoIngressoProducer;
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
public class TipoIngressoService {

  private final TipoIngressoRepository tipoIngressoRepository;
  private final EventoRepository eventoRepository;
  private final TipoIngressoMapper tipoIngressoMapper;
  private final TipoIngressoProducer tipoIngressoProducer;

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = CacheConstants.CACHE_TIPOS_INGRESSO, key = "#eventoId"),
        @CacheEvict(value = CacheConstants.CACHE_EVENTO, key = "#eventoId"),
        @CacheEvict(value = CacheConstants.CACHE_EVENTOS, allEntries = true)
      })
  public TipoIngressoResponseDTO adicionar(UUID eventoId, TipoIngressoRequestDTO requestDTO) {
    Evento evento =
        eventoRepository
            .findById(eventoId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Evento não encontrado com o ID: " + eventoId));

    TipoIngresso tipoIngresso = tipoIngressoMapper.toEntity(requestDTO);
    tipoIngresso.setEvento(evento);

    TipoIngresso salvo = tipoIngressoRepository.save(tipoIngresso);
    TipoIngressoResponseDTO responseDTO = tipoIngressoMapper.toResponseDTO(salvo);

    tipoIngressoProducer.enviarTipoIngressoCriado(responseDTO);

    return responseDTO;
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CacheConstants.CACHE_TIPOS_INGRESSO, key = "#eventoId")
  public List<TipoIngressoResponseDTO> listarPorEvento(UUID eventoId) {
    if (!eventoRepository.existsById(eventoId)) {
      throw new ResourceNotFoundException("Evento não encontrado com o ID: " + eventoId);
    }
    return tipoIngressoRepository.findAllByEventoId(eventoId).stream()
        .map(tipoIngressoMapper::toResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public TipoIngressoResponseDTO buscarPorId(UUID eventoId, UUID id) {
    return tipoIngressoRepository
        .findByIdAndEventoId(id, eventoId)
        .map(tipoIngressoMapper::toResponseDTO)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Tipo de ingresso não encontrado com o ID: " + id));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = CacheConstants.CACHE_TIPOS_INGRESSO, key = "#eventoId"),
        @CacheEvict(value = CacheConstants.CACHE_EVENTO, key = "#eventoId"),
        @CacheEvict(value = CacheConstants.CACHE_EVENTOS, allEntries = true)
      })
  public TipoIngressoResponseDTO atualizar(
      UUID eventoId, UUID id, TipoIngressoRequestDTO requestDTO) {
    TipoIngresso existente =
        tipoIngressoRepository
            .findByIdAndEventoId(id, eventoId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Tipo de ingresso não encontrado com o ID: " + id));

    tipoIngressoMapper.updateEntity(requestDTO, existente);
    TipoIngresso atualizado = tipoIngressoRepository.save(existente);
    TipoIngressoResponseDTO responseDTO = tipoIngressoMapper.toResponseDTO(atualizado);

    tipoIngressoProducer.enviarTipoIngressoAtualizado(responseDTO);

    return responseDTO;
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = CacheConstants.CACHE_TIPOS_INGRESSO, key = "#eventoId"),
        @CacheEvict(value = CacheConstants.CACHE_EVENTO, key = "#eventoId"),
        @CacheEvict(value = CacheConstants.CACHE_EVENTOS, allEntries = true)
      })
  public void remover(UUID eventoId, UUID id) {
    TipoIngresso existente =
        tipoIngressoRepository
            .findByIdAndEventoId(id, eventoId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Tipo de ingresso não encontrado com o ID: " + id));

    tipoIngressoRepository.delete(existente);
  }
}
