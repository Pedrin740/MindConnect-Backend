package br.com.mindconnect.dao;

import br.com.mindconnect.model.Favorito;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_FAVORITO (chave primária composta: ID_USUARIO + ID_CONTEUDO)
@Repository
public class FavoritoDAO {

    private static final String SELECT_BASE =
            "SELECT f.ID_USUARIO, f.ID_CONTEUDO, f.DATA_FAVORITO, c.TITULO AS TITULO_CONTEUDO, "
                    + "c.ASSUNTO AS ASSUNTO_CONTEUDO "
                    + "FROM GS_FAVORITO f "
                    + "JOIN GS_CONTEUDO c ON c.ID_CONTEUDO = f.ID_CONTEUDO ";

    private final DataSource dataSource;

    public FavoritoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void inserir(Favorito favorito) {
        String sql = "INSERT INTO GS_FAVORITO (ID_USUARIO, ID_CONTEUDO, DATA_FAVORITO) VALUES (?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, favorito.getIdUsuario());
            ps.setLong(2, favorito.getIdConteudo());
            ps.setDate(3, Date.valueOf(favorito.getDataFavorito()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir favorito", e);
        }
    }

    // Favoritos do usuário, do mais recente para o mais antigo
    public List<Favorito> listarPorUsuario(Long idUsuario) {
        String sql = SELECT_BASE + "WHERE f.ID_USUARIO = ? ORDER BY f.DATA_FAVORITO DESC, c.TITULO";
        List<Favorito> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar favoritos", e);
        }
    }

    public Favorito buscar(Long idUsuario, Long idConteudo) {
        String sql = SELECT_BASE + "WHERE f.ID_USUARIO = ? AND f.ID_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            ps.setLong(2, idConteudo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar favorito", e);
        }
    }

    // Quantos favoritos o usuário possui (usado para impedir a remoção do usuário)
    public int contarPorUsuario(Long idUsuario) {
        String sql = "SELECT COUNT(*) FROM GS_FAVORITO WHERE ID_USUARIO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao contar favoritos do usuário", e);
        }
    }

    public int excluir(Long idUsuario, Long idConteudo) {
        String sql = "DELETE FROM GS_FAVORITO WHERE ID_USUARIO = ? AND ID_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            ps.setLong(2, idConteudo);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir favorito", e);
        }
    }

    private Favorito mapear(ResultSet rs) throws SQLException {
        Favorito f = new Favorito();
        f.setIdUsuario(rs.getLong("ID_USUARIO"));
        f.setIdConteudo(rs.getLong("ID_CONTEUDO"));
        f.setDataFavorito(rs.getDate("DATA_FAVORITO").toLocalDate());
        f.setTituloConteudo(rs.getString("TITULO_CONTEUDO"));
        f.setAssuntoConteudo(rs.getString("ASSUNTO_CONTEUDO"));
        return f;
    }
}
