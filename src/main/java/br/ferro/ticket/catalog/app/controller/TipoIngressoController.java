package br.ferro.ticket.catalog.app.controller;

import br.ferro.ticket.catalog.app.dto.TipoIngressoRequestDTO;
import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import br.ferro.ticket.catalog.app.service.TipoIngressoService;
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
@RequestMapping("/api/v1/eventos/{eventoId}/tipos-ingresso")
@RequiredArgsConstructor
@Tag(name = "Tipos de Ingresso")
public class TipoIngressoController {

  private final TipoIngressoService tipoIngressoService;

  @Operation(summary = "Adicionar tipo de ingresso a um evento")
  @PostMapping
  public ResponseEntity<TipoIngressoResponseDTO> adicionar(
      @PathVariable UUID eventoId, @Valid @RequestBody TipoIngressoRequestDTO requestDTO) {
    TipoIngressoResponseDTO criado = tipoIngressoService.adicionar(eventoId, requestDTO);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criado.id())
            .toUri();
    return ResponseEntity.created(location).body(criado);
  }

  @Operation(summary = "Listar tipos de ingresso de um evento")
  @GetMapping
  public ResponseEntity<List<TipoIngressoResponseDTO>> listar(@PathVariable UUID eventoId) {
    return ResponseEntity.ok(tipoIngressoService.listarPorEvento(eventoId));
  }

  @Operation(summary = "Buscar tipo de ingresso por ID")
  @GetMapping("/{id}")
  public ResponseEntity<TipoIngressoResponseDTO> buscarPorId(
      @PathVariable UUID eventoId, @PathVariable UUID id) {
    return ResponseEntity.ok(tipoIngressoService.buscarPorId(eventoId, id));
  }

  @Operation(summary = "Remover tipo de ingresso")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable UUID eventoId, @PathVariable UUID id) {
    tipoIngressoService.remover(eventoId, id);
    return ResponseEntity.noContent().build();
  }
}
