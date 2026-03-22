package br.ferro.ticket.catalog.infra.repository;

import br.ferro.ticket.catalog.domain.entity.TipoIngresso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipoIngressoRepository extends JpaRepository<TipoIngresso, UUID> {
}
