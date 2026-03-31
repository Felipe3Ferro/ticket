package br.ferro.ticket.catalog.infra.messaging;

public final class KafkaTopics {

  public static final String EVENTO_CRIADO = "tickethigh.catalog.evento.criado";

  public static final String TIPO_INGRESSO_CRIADO = "tickethigh.catalog.tipo-ingresso.criado";
  public static final String TIPO_INGRESSO_ATUALIZADO =
      "tickethigh.catalog.tipo-ingresso.atualizado";

  private KafkaTopics() {}
}
