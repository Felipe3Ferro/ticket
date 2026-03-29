package br.ferro.ticket.catalog.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record TipoIngressoRequestDTO(
    @NotBlank String nomeSetor,
    @PositiveOrZero BigDecimal preco,
    @NotNull @PositiveOrZero Integer quantidadeDisponivel) {}
