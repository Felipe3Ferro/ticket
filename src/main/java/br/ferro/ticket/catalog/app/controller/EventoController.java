package br.ferro.ticket.catalog.app.controller;

import br.ferro.ticket.catalog.app.dto.EventoRequestDTO;
import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import br.ferro.ticket.catalog.app.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    public ResponseEntity<EventoResponseDTO> criarEvento(@RequestBody EventoRequestDTO eventoRequestDTO) {
        EventoResponseDTO eventoCriado = eventoService.criarEvento(eventoRequestDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(eventoCriado.id())
                .toUri();
        return ResponseEntity.created(location).body(eventoCriado);
    }

    @GetMapping
    public ResponseEntity<List<EventoResponseDTO>> listarEventos() {
        List<EventoResponseDTO> eventos = eventoService.listarEventos();
        return ResponseEntity.ok(eventos);
    }
}
