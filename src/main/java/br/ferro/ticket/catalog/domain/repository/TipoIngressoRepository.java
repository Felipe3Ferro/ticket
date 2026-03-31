package br.ferro.ticket.catalog.domain.repository;

import br.ferro.ticket.catalog.domain.entity.TipoIngresso;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoIngressoRepository extends JpaRepository<TipoIngresso, UUID> {

  List<TipoIngresso> findAllByEventoId(UUID eventoId);

  Optional<TipoIngresso> findByIdAndEventoId(UUID id, UUID eventoId);
}
