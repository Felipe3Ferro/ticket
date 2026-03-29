package br.ferro.ticket.catalog.app.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Estrutura padrão de resposta de erro da API")
public record ErroResponse(
    @Schema(description = "Momento em que o erro ocorreu", example = "2026-03-29T14:32:00")
        LocalDateTime timestamp,
    @Schema(description = "Código HTTP do erro", example = "400") Integer status,
    @Schema(description = "Mensagem resumida do erro", example = "Erro de validação")
        String message,
    @Schema(
            description = "Lista de erros detalhados por campo",
            example = "[\"nome: não deve estar em branco\"]")
        List<String> errors) {}
