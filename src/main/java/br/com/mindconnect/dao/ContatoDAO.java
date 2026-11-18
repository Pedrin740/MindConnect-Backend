package br.com.mindconnect.dao;

import br.com.mindconnect.model.Contato;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

// Acesso JDBC à tabela GS_CONTATO.
@Repository
public class ContatoDAO {

    private final DataSource dataSource;

    public ContatoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(Contato contato) {
        String sql = "INSERT INTO GS_CONTATO (NOME, EMAIL, MOTIVO, MENSAGEM) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CONTATO"})) {

            ps.setString(1, contato.getNome());
            ps.setString(2, contato.getEmail());
            ps.setString(3, contato.getMotivo());
            ps.setString(4, contato.getMensagem());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getLong(1);
                }
            }

            return null;

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao registrar mensagem de contato", e);
        }
    }

    public Contato buscarPorId(Long id) {
        String sql = "SELECT ID_CONTATO, NOME, EMAIL, MOTIVO, DATA_ENVIO, STATUS "
                + "FROM GS_CONTATO WHERE ID_CONTATO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Contato contato = new Contato();

                contato.setIdContato(rs.getLong("ID_CONTATO"));
                contato.setNome(rs.getString("NOME"));
                contato.setEmail(rs.getString("EMAIL"));
                contato.setMotivo(rs.getString("MOTIVO"));

                Timestamp dataEnvio = rs.getTimestamp("DATA_ENVIO");

                contato.setDataEnvio(
                        dataEnvio != null ? dataEnvio.toLocalDateTime() : null
                );

                contato.setStatus(rs.getString("STATUS"));

                return contato;
            }

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao consultar mensagem de contato", e);
        }
    }
}
