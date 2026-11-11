package br.com.mindconnect.dao;

import br.com.mindconnect.model.CategoriaServico;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_CATEGORIA_SERVICO (lado "1" do relacionamento 1:N com os serviços)
@Repository
public class CategoriaServicoDAO {

    private static final String COLUNAS = "ID_CATEGORIA_SERVICO, NOME, DESCRICAO";

    private final DataSource dataSource;

    public CategoriaServicoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(CategoriaServico categoria) {
        String sql = "INSERT INTO GS_CATEGORIA_SERVICO (NOME, DESCRICAO) VALUES (?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CATEGORIA_SERVICO"})) {

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
            throw TradutorErroSql.traduzir("Erro ao inserir categoria de serviço", e);
        }
    }

    public List<CategoriaServico> listar() {
        String sql = "SELECT " + COLUNAS + " FROM GS_CATEGORIA_SERVICO ORDER BY NOME";
        List<CategoriaServico> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar categorias de serviço", e);
        }
    }

    public CategoriaServico buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM GS_CATEGORIA_SERVICO WHERE ID_CATEGORIA_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar categoria de serviço", e);
        }
    }

    // idIgnorar: usado no PUT para não considerar a própria categoria (use 0 no cadastro)
    public boolean existeNome(String nome, Long idIgnorar) {
        String sql = "SELECT COUNT(*) FROM GS_CATEGORIA_SERVICO "
                + "WHERE UPPER(NOME) = UPPER(?) AND ID_CATEGORIA_SERVICO <> ?";

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

    public int atualizar(CategoriaServico categoria) {
        String sql = "UPDATE GS_CATEGORIA_SERVICO SET NOME = ?, DESCRICAO = ? WHERE ID_CATEGORIA_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setLong(3, categoria.getIdCategoriaServico());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar categoria de serviço", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_CATEGORIA_SERVICO WHERE ID_CATEGORIA_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir categoria de serviço", e);
        }
    }

    private CategoriaServico mapear(ResultSet rs) throws SQLException {
        CategoriaServico categoria = new CategoriaServico();
        categoria.setIdCategoriaServico(rs.getLong("ID_CATEGORIA_SERVICO"));
        categoria.setNome(rs.getString("NOME"));
        categoria.setDescricao(rs.getString("DESCRICAO"));
        return categoria;
    }
}
