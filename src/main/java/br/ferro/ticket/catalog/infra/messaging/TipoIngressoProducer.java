package br.ferro.ticket.catalog.infra.messaging;

import br.ferro.ticket.catalog.app.dto.TipoIngressoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TipoIngressoProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void enviarTipoIngressoCriado(TipoIngressoResponseDTO tipoIngresso) {
    kafkaTemplate
        .send(KafkaTopics.TIPO_INGRESSO_CRIADO, tipoIngresso.id().toString(), tipoIngresso)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "Falha ao publicar tipo ingresso criado no Kafka. ID: {}",
                    tipoIngresso.id(),
                    ex);
              } else {
                log.info(
                    "Tipo ingresso criado publicado com sucesso. ID: {}, tópico: {}, offset: {}",
                    tipoIngresso.id(),
                    KafkaTopics.TIPO_INGRESSO_CRIADO,
                    result.getRecordMetadata().offset());
              }
            });
  }

}
