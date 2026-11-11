package br.com.mindconnect.dao;

import br.com.mindconnect.model.CategoriaConteudo;
import br.com.mindconnect.model.Conteudo;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso às tabelas GS_CONTEUDO e GS_CONTEUDO_CATEGORIA (associação N:N com as categorias).
// Inserir, atualizar e excluir mexem em mais de uma tabela, então usam transação (commit/rollback).
@Repository
public class ConteudoDAO {

    private static final String COLUNAS =
            "c.ID_CONTEUDO, c.TITULO, c.ASSUNTO, c.DESCRICAO, c.MATERIAL, c.FONTE_INFORMACAO, "
                    + "c.DATA_PUBLICACAO, c.STATUS";

    private final DataSource dataSource;

    public ConteudoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(Conteudo conteudo, List<Long> idsCategorias) {
        String sql = "INSERT INTO GS_CONTEUDO (TITULO, ASSUNTO, DESCRICAO, MATERIAL, FONTE_INFORMACAO, "
                + "DATA_PUBLICACAO, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                Long id = null;

                try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CONTEUDO"})) {
                    ps.setString(1, conteudo.getTitulo());
                    ps.setString(2, conteudo.getAssunto());
                    ps.setString(3, conteudo.getDescricao());
                    ps.setString(4, conteudo.getMaterial());
                    ps.setString(5, conteudo.getFonteInformacao());
                    ps.setDate(6, Date.valueOf(conteudo.getDataPublicacao()));
                    ps.setString(7, conteudo.getStatus());
                    ps.executeUpdate();

                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) {
                            id = chaves.getLong(1);
                        }
                    }
                }

                inserirAssociacoes(con, id, idsCategorias);
                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir conteúdo", e);
        }
    }

    // Todos os filtros são opcionais (null = não filtra)
    public List<Conteudo> listar(String busca, String status, Long idCategoria, String nomeCategoria) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUNAS + " FROM GS_CONTEUDO c WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (busca != null && !busca.isBlank()) {
            sql.append("AND (UPPER(c.TITULO) LIKE ? OR UPPER(c.ASSUNTO) LIKE ?) ");
            String termo = "%" + busca.trim().toUpperCase() + "%";
            parametros.add(termo);
            parametros.add(termo);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND c.STATUS = ? ");
            parametros.add(status.trim().toUpperCase());
        }
        if (idCategoria != null) {
            sql.append("AND EXISTS (SELECT 1 FROM GS_CONTEUDO_CATEGORIA cc "
                    + "WHERE cc.ID_CONTEUDO = c.ID_CONTEUDO AND cc.ID_CATEGORIA_CONTEUDO = ?) ");
            parametros.add(idCategoria);
        }
        if (nomeCategoria != null && !nomeCategoria.isBlank()) {
            sql.append("AND EXISTS (SELECT 1 FROM GS_CONTEUDO_CATEGORIA cc "
                    + "JOIN GS_CATEGORIA_CONTEUDO cat ON cat.ID_CATEGORIA_CONTEUDO = cc.ID_CATEGORIA_CONTEUDO "
                    + "WHERE cc.ID_CONTEUDO = c.ID_CONTEUDO AND UPPER(cat.NOME) = UPPER(?)) ");
            parametros.add(nomeCategoria.trim());
        }
        sql.append("ORDER BY c.DATA_PUBLICACAO DESC, c.ID_CONTEUDO DESC");

        List<Conteudo> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

            for (Conteudo conteudo : lista) {
                conteudo.setCategorias(buscarCategorias(con, conteudo.getIdConteudo()));
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar conteúdos", e);
        }
    }

    public Conteudo buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM GS_CONTEUDO c WHERE c.ID_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            Conteudo conteudo = null;

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    conteudo = mapear(rs);
                }
            }

            if (conteudo != null) {
                conteudo.setCategorias(buscarCategorias(con, id));
            }
            return conteudo;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar conteúdo", e);
        }
    }

    public int atualizar(Conteudo conteudo, List<Long> idsCategorias) {
        String sql = "UPDATE GS_CONTEUDO SET TITULO = ?, ASSUNTO = ?, DESCRICAO = ?, MATERIAL = ?, "
                + "FONTE_INFORMACAO = ?, DATA_PUBLICACAO = ?, STATUS = ? WHERE ID_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                int linhas;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, conteudo.getTitulo());
                    ps.setString(2, conteudo.getAssunto());
                    ps.setString(3, conteudo.getDescricao());
                    ps.setString(4, conteudo.getMaterial());
                    ps.setString(5, conteudo.getFonteInformacao());
                    ps.setDate(6, Date.valueOf(conteudo.getDataPublicacao()));
                    ps.setString(7, conteudo.getStatus());
                    ps.setLong(8, conteudo.getIdConteudo());
                    linhas = ps.executeUpdate();
                }

                // troca as associações antigas pelas novas
                excluirAssociacoes(con, conteudo.getIdConteudo());
                inserirAssociacoes(con, conteudo.getIdConteudo(), idsCategorias);

                con.commit();
                return linhas;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar conteúdo", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_CONTEUDO WHERE ID_CONTEUDO = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                excluirAssociacoes(con, id);
                excluirFavoritos(con, id);
                int linhas;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, id);
                    linhas = ps.executeUpdate();
                }

                con.commit();
                return linhas;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir conteúdo", e);
        }
    }

    private void inserirAssociacoes(Connection con, Long idConteudo, List<Long> idsCategorias)
            throws SQLException {
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO GS_CONTEUDO_CATEGORIA (ID_CONTEUDO, ID_CATEGORIA_CONTEUDO) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Long idCategoria : idsCategorias) {
                ps.setLong(1, idConteudo);
                ps.setLong(2, idCategoria);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void excluirAssociacoes(Connection con, Long idConteudo) throws SQLException {
        String sql = "DELETE FROM GS_CONTEUDO_CATEGORIA WHERE ID_CONTEUDO = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idConteudo);
            ps.executeUpdate();
        }
    }

    // Favoritos dependem do conteúdo: saem junto com ele (na mesma transação)
    private void excluirFavoritos(Connection con, Long idConteudo) throws SQLException {
        String sql = "DELETE FROM GS_FAVORITO WHERE ID_CONTEUDO = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idConteudo);
            ps.executeUpdate();
        }
    }

    private List<CategoriaConteudo> buscarCategorias(Connection con, Long idConteudo) throws SQLException {
        String sql = "SELECT cat.ID_CATEGORIA_CONTEUDO, cat.NOME, cat.DESCRICAO "
                + "FROM GS_CONTEUDO_CATEGORIA cc "
                + "JOIN GS_CATEGORIA_CONTEUDO cat ON cat.ID_CATEGORIA_CONTEUDO = cc.ID_CATEGORIA_CONTEUDO "
                + "WHERE cc.ID_CONTEUDO = ? ORDER BY cat.NOME";
        List<CategoriaConteudo> categorias = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idConteudo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoriaConteudo categoria = new CategoriaConteudo();
                    categoria.setIdCategoriaConteudo(rs.getLong("ID_CATEGORIA_CONTEUDO"));
                    categoria.setNome(rs.getString("NOME"));
                    categoria.setDescricao(rs.getString("DESCRICAO"));
                    categorias.add(categoria);
                }
            }
        }
        return categorias;
    }

    private Conteudo mapear(ResultSet rs) throws SQLException {
        Conteudo c = new Conteudo();
        c.setIdConteudo(rs.getLong("ID_CONTEUDO"));
        c.setTitulo(rs.getString("TITULO"));
        c.setAssunto(rs.getString("ASSUNTO"));
        c.setDescricao(rs.getString("DESCRICAO"));
        c.setMaterial(rs.getString("MATERIAL"));
        c.setFonteInformacao(rs.getString("FONTE_INFORMACAO"));
        c.setStatus(rs.getString("STATUS"));

        Date publicacao = rs.getDate("DATA_PUBLICACAO");
        if (publicacao != null) {
            c.setDataPublicacao(publicacao.toLocalDate());
        }
        return c;
    }
}
