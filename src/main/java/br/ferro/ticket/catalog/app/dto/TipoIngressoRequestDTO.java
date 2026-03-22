package br.ferro.ticket.catalog.app.dto;

import java.math.BigDecimal;

public record TipoIngressoRequestDTO(
    String nomeSetor,
    BigDecimal preco,
    Integer quantidadeDisponivel
) {}
