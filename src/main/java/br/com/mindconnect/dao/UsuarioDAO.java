package br.com.mindconnect.dao;

import br.com.mindconnect.model.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_USUARIO com JDBC puro
@Repository
public class UsuarioDAO {

    private static final String COLUNAS = "ID_USUARIO, NOME, EMAIL, SENHA, DATA_CADASTRO";

    private final DataSource dataSource;

    public UsuarioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(Usuario usuario) {
        String sql = "INSERT INTO GS_USUARIO (NOME, EMAIL, SENHA) VALUES (?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_USUARIO"})) {

            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir usuário", e);
        }
    }

    public List<Usuario> listar() {
        String sql = "SELECT " + COLUNAS + " FROM GS_USUARIO ORDER BY NOME";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar usuários", e);
        }
    }

    public Usuario buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM GS_USUARIO WHERE ID_USUARIO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar usuário", e);
        }
    }

    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM GS_USUARIO WHERE UPPER(EMAIL) = UPPER(?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar usuário por e-mail", e);
        }
    }

    // idIgnorar: usado no PUT para não considerar o próprio usuário (use 0 no cadastro)
    public boolean existeEmail(String email, Long idIgnorar) {
        String sql = "SELECT COUNT(*) FROM GS_USUARIO WHERE UPPER(EMAIL) = UPPER(?) AND ID_USUARIO <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setLong(2, idIgnorar);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao verificar e-mail", e);
        }
    }

    public int atualizar(Usuario usuario) {
        String sql = "UPDATE GS_USUARIO SET NOME = ?, EMAIL = ?, SENHA = ? WHERE ID_USUARIO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.setLong(4, usuario.getIdUsuario());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar usuário", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_USUARIO WHERE ID_USUARIO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir usuário", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("ID_USUARIO"));
        usuario.setNome(rs.getString("NOME"));
        usuario.setEmail(rs.getString("EMAIL"));
        usuario.setSenha(rs.getString("SENHA"));

        Timestamp cadastro = rs.getTimestamp("DATA_CADASTRO");
        if (cadastro != null) {
            usuario.setDataCadastro(cadastro.toLocalDateTime());
        }
        return usuario;
    }
}
