package br.ferro.ticket.catalog.app.service;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.mapper.EventoMapper;
import br.ferro.ticket.catalog.domain.entity.Evento;
import br.ferro.ticket.catalog.infra.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;

    @Transactional
    public EventoResponseDTO criarEvento(EventoRequestDTO eventoRequestDTO) {
        Evento evento = eventoMapper.toEntity(eventoRequestDTO);
        Evento eventoSalvo = eventoRepository.save(evento);
        return eventoMapper.toResponseDTO(eventoSalvo);
    }

    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listarEventos() {
        return eventoRepository.findAll().stream()
                .map(eventoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
