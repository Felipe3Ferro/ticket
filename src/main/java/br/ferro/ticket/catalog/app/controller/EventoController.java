package br.ferro.ticket.catalog.app.controller;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.exception.ErroResponse;
import br.ferro.ticket.catalog.app.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(
    name = "Eventos",
    description = "Gerenciamento do catálogo de eventos — shows, festivais e jogos")
public class EventoController {

  private final EventoService eventoService;

  @Operation(
      summary = "Criar novo evento",
      description =
          "Cria um evento com seus respectivos tipos de ingresso. "
              + "Após a persistência, um evento de domínio é publicado no Kafka.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Evento criado com sucesso",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = EventoResponseDTO.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos na requisição",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErroResponse.class)))
  })
  @PostMapping
  public ResponseEntity<EventoResponseDTO> criarEvento(
      @Valid @RequestBody EventoRequestDTO eventoRequestDTO) {
    EventoResponseDTO eventoCriado = eventoService.criarEvento(eventoRequestDTO);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(eventoCriado.id())
            .toUri();
    return ResponseEntity.created(location).body(eventoCriado);
  }

  @Operation(
      summary = "Listar todos os eventos",
      description = "Retorna a lista completa de eventos cadastrados no catálogo.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Lista retornada com sucesso",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = EventoResponseDTO.class))))
  })
  @GetMapping
  public ResponseEntity<List<EventoResponseDTO>> listarEventos() {
    List<EventoResponseDTO> eventos = eventoService.listarEventos();
    return ResponseEntity.ok(eventos);
  }

  @Operation(
      summary = "Buscar evento por ID",
      description = "Retorna os detalhes de um evento específico a partir do seu UUID.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Evento encontrado",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = EventoResponseDTO.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Evento não encontrado",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErroResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<EventoResponseDTO> buscarEventoPorId(
      @Parameter(
              description = "UUID do evento",
              required = true,
              example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id) {
    EventoResponseDTO evento = eventoService.buscarEventoPorId(id);
    return ResponseEntity.ok(evento);
  }
}
