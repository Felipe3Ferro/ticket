package br.ferro.ticket.catalog.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventoRequestDTO(
    String nome,
    String descricao,
    LocalDateTime dataHora,
    String local,
    List<TipoIngressoRequestDTO> tiposIngresso
) {}
