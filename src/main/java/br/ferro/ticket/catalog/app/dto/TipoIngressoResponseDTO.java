package br.ferro.ticket.catalog.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TipoIngressoResponseDTO(
    UUID id,
    String nomeSetor,
    BigDecimal preco,
    Integer quantidadeDisponivel
) {}
