package br.ferro.ticket.catalog.infra.messaging;

import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventoProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "ticket-catalog.evento.criado";

    public void enviarEventoCriado(EventoResponseDTO evento) {
        try {
            log.info("Enviando evento de criação para o tópico: {}", TOPIC);
            kafkaTemplate.send(TOPIC, evento);
            log.info("Evento enviado com sucesso: {}", evento.id());
        } catch (Exception e) {
            log.error("Erro ao enviar evento de criação para o Kafka", e);
        }
    }
}
