package br.ferro.ticket.catalog.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de um evento retornado pela API")
public record EventoResponseDTO(
    @Schema(
            description = "Identificador único do evento",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
    @Schema(description = "Nome do evento", example = "Rock in Rio 2026") String nome,
    @Schema(
            description = "Descrição detalhada do evento",
            example = "O maior festival de música do Brasil")
        String descricao,
    @Schema(description = "Data e hora do evento", example = "2026-09-15T20:00:00")
        LocalDateTime dataHora,
    @Schema(
            description = "Local onde o evento será realizado",
            example = "Parque Olímpico, Rio de Janeiro")
        String local,
    @Schema(description = "Tipos de ingresso disponíveis para o evento")
        List<TipoIngressoResponseDTO> tiposIngresso) {}
