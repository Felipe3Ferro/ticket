package br.ferro.ticket.catalog.domain.repository;

import br.ferro.ticket.catalog.domain.entity.Evento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

  @Query("SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.tiposIngresso")
  List<Evento> findAllWithTiposIngresso();

  @Query("SELECT e FROM Evento e LEFT JOIN FETCH e.tiposIngresso WHERE e.id = :id")
  Optional<Evento> findByIdWithTiposIngresso(@Param("id") UUID id);
}
