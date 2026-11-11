package br.com.mindconnect.dao;

import br.com.mindconnect.model.Administrador;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_ADMINISTRADOR
@Repository
public class AdministradorDAO {

    private static final String SELECT_BASE =
            "SELECT a.ID_ADMINISTRADOR, a.ID_USUARIO, a.NIVEL_ACESSO, u.NOME AS NOME_USUARIO, u.EMAIL AS EMAIL_USUARIO "
                    + "FROM GS_ADMINISTRADOR a "
                    + "JOIN GS_USUARIO u ON u.ID_USUARIO = a.ID_USUARIO ";

    private final DataSource dataSource;

    public AdministradorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(Administrador administrador) {
        String sql = "INSERT INTO GS_ADMINISTRADOR (ID_USUARIO, NIVEL_ACESSO) VALUES (?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_ADMINISTRADOR"})) {

            ps.setLong(1, administrador.getIdUsuario());
            ps.setString(2, administrador.getNivelAcesso());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir administrador", e);
        }
    }

    public List<Administrador> listar() {
        String sql = SELECT_BASE + "ORDER BY u.NOME";
        List<Administrador> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar administradores", e);
        }
    }

    public Administrador buscarPorId(Long id) {
        String sql = SELECT_BASE + "WHERE a.ID_ADMINISTRADOR = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar administrador", e);
        }
    }

    public Administrador buscarPorUsuario(Long idUsuario) {
        String sql = SELECT_BASE + "WHERE a.ID_USUARIO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar administrador do usuário", e);
        }
    }

    public int atualizar(Administrador administrador) {
        String sql = "UPDATE GS_ADMINISTRADOR SET NIVEL_ACESSO = ? WHERE ID_ADMINISTRADOR = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, administrador.getNivelAcesso());
            ps.setLong(2, administrador.getIdAdministrador());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar administrador", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_ADMINISTRADOR WHERE ID_ADMINISTRADOR = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir administrador", e);
        }
    }

    private Administrador mapear(ResultSet rs) throws SQLException {
        Administrador a = new Administrador();
        a.setIdAdministrador(rs.getLong("ID_ADMINISTRADOR"));
        a.setIdUsuario(rs.getLong("ID_USUARIO"));
        a.setNivelAcesso(rs.getString("NIVEL_ACESSO"));
        a.setNomeUsuario(rs.getString("NOME_USUARIO"));
        a.setEmailUsuario(rs.getString("EMAIL_USUARIO"));
        return a;
    }
}
