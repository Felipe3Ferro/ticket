package br.ferro.ticket.catalog.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Dados para criação de um novo evento")
public record EventoRequestDTO(
    @Schema(description = "Nome do evento", example = "Rock in Rio 2026") @NotBlank String nome,
    @Schema(
            description = "Descrição detalhada do evento",
            example = "O maior festival de música do Brasil")
        String descricao,
    @Schema(
            description = "Data e hora do evento (não pode ser no passado)",
            example = "2026-09-15T20:00:00")
        @FutureOrPresent
        LocalDateTime dataHora,
    @Schema(
            description = "Local onde o evento será realizado",
            example = "Parque Olímpico, Rio de Janeiro")
        String local,
    @Schema(description = "Tipos de ingresso disponíveis para o evento") @Valid
        List<TipoIngressoRequestDTO> tiposIngresso) {}
