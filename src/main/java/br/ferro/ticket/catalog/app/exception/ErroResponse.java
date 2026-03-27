package br.ferro.ticket.catalog.app.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
        LocalDateTime timestamp,
        Integer status,
        String message,
        List<String> errors) {
}