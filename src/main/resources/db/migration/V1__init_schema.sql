CREATE TABLE evento (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_hora TIMESTAMP NOT NULL,
    local VARCHAR(255) NOT NULL
);

CREATE TABLE tipo_ingresso (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL,
    nome_setor VARCHAR(255) NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    quantidade_disponivel INTEGER NOT NULL
);

ALTER TABLE tipo_ingresso
ADD CONSTRAINT fk_tipo_ingresso_evento
FOREIGN KEY (evento_id) REFERENCES evento(id);
