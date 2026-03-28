package br.ferro.ticket.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TicketApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() {
		// Este teste verifica se o contexto inteiro da aplicação sobe sem erros
		// (incluindo as configurações de banco).
	}

	@Test
	void testDatabaseConnection() throws SQLException {
		assertNotNull(dataSource, "O DataSource não foi inicializado corretamente pelo Spring.");
		try (Connection connection = dataSource.getConnection()) {
			assertTrue(connection.isValid(2), "Não foi possível validar a conexão com o banco de dados.");
		}
	}
}
