package br.ferro.ticket.catalog.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Dados de um tipo de ingresso retornado pela API")
public record TipoIngressoResponseDTO(
    @Schema(
            description = "Identificador único do tipo de ingresso",
            example = "7cb4e6a1-3b2f-4d8e-9c01-5e7f8a9b0c2d")
        UUID id,
    @Schema(description = "Nome do setor / tipo de ingresso", example = "Pista Premium")
        String nomeSetor,
    @Schema(description = "Preço unitário do ingresso em reais", example = "350.00")
        BigDecimal preco,
    @Schema(description = "Quantidade de ingressos disponíveis neste setor", example = "500")
        Integer quantidadeDisponivel) {}
