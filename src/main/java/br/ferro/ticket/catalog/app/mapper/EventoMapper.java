package br.ferro.ticket.catalog.app.mapper;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.domain.entity.Evento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {TipoIngressoMapper.class})
public interface EventoMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "tiposIngresso", source = "tiposIngresso")
  Evento toEntity(EventoRequestDTO eventoRequestDTO);

  EventoResponseDTO toResponseDTO(Evento evento);
}
