package br.com.mindconnect.dao;

import br.com.mindconnect.model.CategoriaConteudo;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_CATEGORIA_CONTEUDO
@Repository
public class CategoriaConteudoDAO {

    private static final String COLUNAS = "ID_CATEGORIA_CONTEUDO, NOME, DESCRICAO";

    private final DataSource dataSource;

    public CategoriaConteudoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(CategoriaConteudo categoria) {
        String sql = "INSERT INTO GS_CATEGORIA_CONTEUDO (NOME, DESCRICAO) VALUES (?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CATEGORIA_CONTEUDO"})) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir categoria de conteúdo", e);
        }
    }

    public List<CategoriaConteudo> listar() {
        String sql = "SELECT " + COLUNAS + " FROM GS_CATEGORIA_CONTEUDO ORDER BY NOME";
        List<CategoriaConteudo> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar categorias de conteúdo", e);
        }
    }

    public CategoriaConteudo buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM GS_CATEGORIA_CONTEUDO WHERE ID_CATEGORIA_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar categoria de conteúdo", e);
        }
    }

    // idIgnorar: usado no PUT para não considerar a própria categoria (use 0 no cadastro)
    public boolean existeNome(String nome, Long idIgnorar) {
        String sql = "SELECT COUNT(*) FROM GS_CATEGORIA_CONTEUDO "
                + "WHERE UPPER(NOME) = UPPER(?) AND ID_CATEGORIA_CONTEUDO <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setLong(2, idIgnorar);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao verificar nome da categoria", e);
        }
    }

    // Quantos conteúdos estão associados à categoria (usado para impedir a remoção)
    public int contarConteudos(Long idCategoria) {
        String sql = "SELECT COUNT(*) FROM GS_CONTEUDO_CATEGORIA WHERE ID_CATEGORIA_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao contar conteúdos da categoria", e);
        }
    }

    public int atualizar(CategoriaConteudo categoria) {
        String sql = "UPDATE GS_CATEGORIA_CONTEUDO SET NOME = ?, DESCRICAO = ? WHERE ID_CATEGORIA_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setLong(3, categoria.getIdCategoriaConteudo());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar categoria de conteúdo", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_CATEGORIA_CONTEUDO WHERE ID_CATEGORIA_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir categoria de conteúdo", e);
        }
    }

    private CategoriaConteudo mapear(ResultSet rs) throws SQLException {
        CategoriaConteudo categoria = new CategoriaConteudo();
        categoria.setIdCategoriaConteudo(rs.getLong("ID_CATEGORIA_CONTEUDO"));
        categoria.setNome(rs.getString("NOME"));
        categoria.setDescricao(rs.getString("DESCRICAO"));
        return categoria;
    }
}
