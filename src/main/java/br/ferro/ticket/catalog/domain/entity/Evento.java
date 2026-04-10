package br.ferro.ticket.catalog.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String nome;

  private String descricao;

  private LocalDateTime dataHora;

  private String local;

  @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<TipoIngresso> tiposIngresso;

  public void definirTiposIngresso(List<TipoIngresso> tipos) {
    this.tiposIngresso.clear();
    if (tipos == null)
      return;
    tipos.forEach(this::adicionarTipoIngresso);
  }

  public void adicionarTipoIngresso(TipoIngresso tipo) {
    tipo.setEvento(this);
    this.tiposIngresso.add(tipo);
  }
}