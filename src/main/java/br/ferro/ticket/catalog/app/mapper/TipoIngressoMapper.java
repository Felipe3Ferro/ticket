package br.ferro.ticket.catalog.app.mapper;

import br.ferro.ticket.catalog.app.dto.TipoIngressoRequestDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.domain.entity.TipoIngresso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TipoIngressoMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "evento", ignore = true)
  TipoIngresso toEntity(TipoIngressoRequestDTO tipoIngressoRequestDTO);

  TipoIngressoResponseDTO toResponseDTO(TipoIngresso tipoIngresso);
}
