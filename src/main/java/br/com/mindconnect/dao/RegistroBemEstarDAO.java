package br.com.mindconnect.dao;

import br.com.mindconnect.model.RegistroBemEstar;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_REGISTRO_BEM_ESTAR (lado "N" do relacionamento 1:N com o usuário)
@Repository
public class RegistroBemEstarDAO {

    private static final String COLUNAS =
            "ID_REGISTRO, ID_USUARIO, DATA_REGISTRO, HUMOR, NIVEL_HUMOR, SENTIMENTOS, OBSERVACOES";

    private final DataSource dataSource;

    public RegistroBemEstarDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(RegistroBemEstar registro) {
        String sql = "INSERT INTO GS_REGISTRO_BEM_ESTAR "
                + "(ID_USUARIO, DATA_REGISTRO, HUMOR, NIVEL_HUMOR, SENTIMENTOS, OBSERVACOES) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_REGISTRO"})) {

            ps.setLong(1, registro.getIdUsuario());
            ps.setDate(2, Date.valueOf(registro.getDataRegistro()));
            ps.setString(3, registro.getHumor());
            ps.setInt(4, registro.getNivelHumor());
            ps.setString(5, registro.getSentimentos());
            ps.setString(6, registro.getObservacoes());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir registro de bem-estar", e);
        }
    }

    // idUsuario null = lista de todos os usuários; com valor = histórico daquele usuário
    // (do mais recente para o mais antigo)
    public List<RegistroBemEstar> listar(Long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM GS_REGISTRO_BEM_ESTAR "
                + (idUsuario != null ? "WHERE ID_USUARIO = ? " : "")
                + "ORDER BY DATA_REGISTRO DESC, ID_REGISTRO DESC";
        List<RegistroBemEstar> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (idUsuario != null) {
                ps.setLong(1, idUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar registros de bem-estar", e);
        }
    }

    public RegistroBemEstar buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM GS_REGISTRO_BEM_ESTAR WHERE ID_REGISTRO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar registro de bem-estar", e);
        }
    }

    // Regra de negócio: um registro por usuário por dia
    // idIgnorar: usado no PUT para não considerar o próprio registro (use 0 no cadastro)
    public boolean existeNoDia(Long idUsuario, LocalDate data, Long idIgnorar) {
        String sql = "SELECT COUNT(*) FROM GS_REGISTRO_BEM_ESTAR "
                + "WHERE ID_USUARIO = ? AND TRUNC(DATA_REGISTRO) = ? AND ID_REGISTRO <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            ps.setDate(2, Date.valueOf(data));
            ps.setLong(3, idIgnorar);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao verificar registro do dia", e);
        }
    }

    // Quantos registros o usuário possui (usado para impedir a remoção do usuário)
    public int contarPorUsuario(Long idUsuario) {
        String sql = "SELECT COUNT(*) FROM GS_REGISTRO_BEM_ESTAR WHERE ID_USUARIO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao contar registros do usuário", e);
        }
    }

    public int atualizar(RegistroBemEstar registro) {
        String sql = "UPDATE GS_REGISTRO_BEM_ESTAR SET DATA_REGISTRO = ?, HUMOR = ?, NIVEL_HUMOR = ?, "
                + "SENTIMENTOS = ?, OBSERVACOES = ? WHERE ID_REGISTRO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(registro.getDataRegistro()));
            ps.setString(2, registro.getHumor());
            ps.setInt(3, registro.getNivelHumor());
            ps.setString(4, registro.getSentimentos());
            ps.setString(5, registro.getObservacoes());
            ps.setLong(6, registro.getIdRegistro());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar registro de bem-estar", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_REGISTRO_BEM_ESTAR WHERE ID_REGISTRO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir registro de bem-estar", e);
        }
    }

    private RegistroBemEstar mapear(ResultSet rs) throws SQLException {
        RegistroBemEstar r = new RegistroBemEstar();
        r.setIdRegistro(rs.getLong("ID_REGISTRO"));
        r.setIdUsuario(rs.getLong("ID_USUARIO"));
        r.setDataRegistro(rs.getDate("DATA_REGISTRO").toLocalDate());
        r.setHumor(rs.getString("HUMOR"));
        r.setNivelHumor(rs.getInt("NIVEL_HUMOR"));
        r.setSentimentos(rs.getString("SENTIMENTOS"));
        r.setObservacoes(rs.getString("OBSERVACOES"));
        return r;
    }
}
