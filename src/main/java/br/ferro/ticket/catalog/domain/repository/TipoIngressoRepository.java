package br.ferro.ticket.catalog.domain.repository;

import br.ferro.ticket.catalog.domain.entity.TipoIngresso;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoIngressoRepository extends JpaRepository<TipoIngresso, UUID> {}
