package br.ferro.ticket.catalog.infra.repository;

import br.ferro.ticket.catalog.domain.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {
}
