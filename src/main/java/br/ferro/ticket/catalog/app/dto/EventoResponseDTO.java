package br.ferro.ticket.catalog.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventoResponseDTO(
    UUID id,
    String nome,
    String descricao,
    LocalDateTime dataHora,
    String local,
    List<TipoIngressoResponseDTO> tiposIngresso
) {}
