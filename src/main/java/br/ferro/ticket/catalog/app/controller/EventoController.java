package br.ferro.ticket.catalog.app.controller;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos")
public class EventoController {

  private final EventoService eventoService;

  @Operation(summary = "Criar evento")
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

  @Operation(summary = "Listar eventos")
  @GetMapping
  public ResponseEntity<List<EventoResponseDTO>> listarEventos() {
    List<EventoResponseDTO> eventos = eventoService.listarEventos();
    return ResponseEntity.ok(eventos);
  }

  @Operation(summary = "Buscar evento por ID")
  @GetMapping("/{id}")
  public ResponseEntity<EventoResponseDTO> buscarEventoPorId(@PathVariable UUID id) {
    EventoResponseDTO evento = eventoService.buscarEventoPorId(id);
    return ResponseEntity.ok(evento);
  }
}
