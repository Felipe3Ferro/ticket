package br.ferro.ticket.catalog.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public record EventoRequestDTO(
    @NotBlank String nome,
    String descricao,
    @Schema(description = "Deve ser data/hora presente ou futura") @FutureOrPresent
        LocalDateTime dataHora,
    String local,
    @Valid List<TipoIngressoRequestDTO> tiposIngresso) {}
