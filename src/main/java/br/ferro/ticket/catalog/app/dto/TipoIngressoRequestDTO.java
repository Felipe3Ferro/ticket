package br.ferro.ticket.catalog.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(description = "Dados para criação de um tipo de ingresso")
public record TipoIngressoRequestDTO(
    @Schema(description = "Nome do setor / tipo de ingresso", example = "Pista Premium") @NotBlank
        String nomeSetor,
    @Schema(description = "Preço unitário do ingresso em reais", example = "350.00") @PositiveOrZero
        BigDecimal preco,
    @Schema(description = "Quantidade de ingressos disponíveis neste setor", example = "500")
        @NotNull
        @PositiveOrZero
        Integer quantidadeDisponivel) {}
