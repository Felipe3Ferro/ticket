package br.ferro.ticket.catalog.infra.messaging;

import br.ferro.ticket.catalog.app.dto.EventoResponseDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventoProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void enviarEventoCriado(EventoResponseDTO evento) {
    kafkaTemplate
        .send(KafkaTopics.EVENTO_CRIADO, evento.id().toString(), evento)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "Falha ao publicar evento criado no Kafka. ID: {}", evento.id(), ex);
              } else {
                log.info(
                    "Evento criado publicado com sucesso. ID: {}, tópico: {}, offset: {}",
                    evento.id(),
                    KafkaTopics.EVENTO_CRIADO,
                    result.getRecordMetadata().offset());
              }
            });
  }
  
  public void enviarEventoRemovido(UUID id) {
    kafkaTemplate
        .send(KafkaTopics.EVENTO_REMOVIDO, id.toString(), id)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error("Falha ao publicar evento removido no Kafka. ID: {}", id, ex);
              } else {
                log.info(
                    "Evento removido publicado com sucesso. ID: {}, tópico: {}, offset: {}",
                    id,
                    KafkaTopics.EVENTO_REMOVIDO,
                    result.getRecordMetadata().offset());
              }
            });
  }

}
