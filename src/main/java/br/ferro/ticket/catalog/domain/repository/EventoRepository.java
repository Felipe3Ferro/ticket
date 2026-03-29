package br.ferro.ticket.catalog.domain.repository;

import br.ferro.ticket.catalog.domain.entity.Evento;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, UUID> {}
