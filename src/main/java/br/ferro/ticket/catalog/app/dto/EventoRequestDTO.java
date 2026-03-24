package br.ferro.ticket.catalog.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public record EventoRequestDTO(
    @NotBlank String nome,
    String descricao,
    @FutureOrPresent LocalDateTime dataHora,
    String local,
    @Valid List<TipoIngressoRequestDTO> tiposIngresso
) {}
