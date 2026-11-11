package br.com.mindconnect.dao;

import br.com.mindconnect.model.ServicoAcolhimento;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// Acesso à tabela GS_SERVICO_ACOLHIMENTO (lado "N" do relacionamento 1:N com a categoria)
@Repository
public class ServicoAcolhimentoDAO {

    private static final String SELECT_BASE =
            "SELECT s.ID_SERVICO, s.ID_CATEGORIA_SERVICO, c.NOME AS NOME_CATEGORIA, s.NOME, s.ENDERECO, "
                    + "s.BAIRRO, s.REGIAO, s.TELEFONE, s.HORARIO_FUNCIONAMENTO, s.DESCRICAO_SERVICOS, "
                    + "s.FONTE_INFORMACAO, s.DATA_VERIFICACAO "
                    + "FROM GS_SERVICO_ACOLHIMENTO s "
                    + "JOIN GS_CATEGORIA_SERVICO c ON c.ID_CATEGORIA_SERVICO = s.ID_CATEGORIA_SERVICO ";

    private final DataSource dataSource;

    public ServicoAcolhimentoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(ServicoAcolhimento servico) {
        String sql = "INSERT INTO GS_SERVICO_ACOLHIMENTO (ID_CATEGORIA_SERVICO, NOME, ENDERECO, BAIRRO, REGIAO, "
                + "TELEFONE, HORARIO_FUNCIONAMENTO, DESCRICAO_SERVICOS, FONTE_INFORMACAO, DATA_VERIFICACAO) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_SERVICO"})) {

            preencher(ps, servico);
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao inserir serviço", e);
        }
    }

    // Todos os filtros são opcionais (null = não filtra)
    public List<ServicoAcolhimento> listar(String nome, String regiao, Long idCategoria, String nomeCategoria) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (nome != null && !nome.isBlank()) {
            sql.append("AND UPPER(s.NOME) LIKE ? ");
            parametros.add("%" + nome.trim().toUpperCase() + "%");
        }
        if (regiao != null && !regiao.isBlank()) {
            sql.append("AND UPPER(s.REGIAO) = UPPER(?) ");
            parametros.add(regiao.trim());
        }
        if (idCategoria != null) {
            sql.append("AND s.ID_CATEGORIA_SERVICO = ? ");
            parametros.add(idCategoria);
        }
        if (nomeCategoria != null && !nomeCategoria.isBlank()) {
            sql.append("AND UPPER(c.NOME) = UPPER(?) ");
            parametros.add(nomeCategoria.trim());
        }
        sql.append("ORDER BY s.NOME");

        List<ServicoAcolhimento> lista = new ArrayList<>();

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
            return lista;
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar serviços", e);
        }
    }

    public ServicoAcolhimento buscarPorId(Long id) {
        String sql = SELECT_BASE + "WHERE s.ID_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao buscar serviço", e);
        }
    }

    // Regra de negócio: não pode haver dois serviços com o mesmo nome e endereço
    // idIgnorar: usado no PUT para não considerar o próprio serviço (use 0 no cadastro)
    public boolean existeDuplicado(String nome, String endereco, Long idIgnorar) {
        String sql = "SELECT COUNT(*) FROM GS_SERVICO_ACOLHIMENTO "
                + "WHERE UPPER(NOME) = UPPER(?) AND UPPER(ENDERECO) = UPPER(?) AND ID_SERVICO <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, endereco);
            ps.setLong(3, idIgnorar);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao verificar serviço duplicado", e);
        }
    }

    // Quantos serviços pertencem à categoria (usado para impedir a remoção da categoria)
    public int contarPorCategoria(Long idCategoria) {
        String sql = "SELECT COUNT(*) FROM GS_SERVICO_ACOLHIMENTO WHERE ID_CATEGORIA_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao contar serviços da categoria", e);
        }
    }

    public int atualizar(ServicoAcolhimento servico) {
        String sql = "UPDATE GS_SERVICO_ACOLHIMENTO SET ID_CATEGORIA_SERVICO = ?, NOME = ?, ENDERECO = ?, "
                + "BAIRRO = ?, REGIAO = ?, TELEFONE = ?, HORARIO_FUNCIONAMENTO = ?, DESCRICAO_SERVICOS = ?, "
                + "FONTE_INFORMACAO = ?, DATA_VERIFICACAO = ? WHERE ID_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            preencher(ps, servico);
            ps.setLong(11, servico.getIdServico());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar serviço", e);
        }
    }

    public int excluir(Long id) {
        String sql = "DELETE FROM GS_SERVICO_ACOLHIMENTO WHERE ID_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir serviço", e);
        }
    }

    // Preenche os 10 parâmetros comuns ao INSERT e ao UPDATE
    private void preencher(PreparedStatement ps, ServicoAcolhimento s) throws SQLException {
        ps.setLong(1, s.getIdCategoriaServico());
        ps.setString(2, s.getNome());
        ps.setString(3, s.getEndereco());
        ps.setString(4, s.getBairro());
        ps.setString(5, s.getRegiao());
        ps.setString(6, s.getTelefone());
        ps.setString(7, s.getHorarioFuncionamento());
        ps.setString(8, s.getDescricaoServicos());
        ps.setString(9, s.getFonteInformacao());

        if (s.getDataVerificacao() != null) {
            ps.setDate(10, Date.valueOf(s.getDataVerificacao()));
        } else {
            ps.setNull(10, Types.DATE);
        }
    }

    private ServicoAcolhimento mapear(ResultSet rs) throws SQLException {
        ServicoAcolhimento s = new ServicoAcolhimento();
        s.setIdServico(rs.getLong("ID_SERVICO"));
        s.setIdCategoriaServico(rs.getLong("ID_CATEGORIA_SERVICO"));
        s.setNomeCategoria(rs.getString("NOME_CATEGORIA"));
        s.setNome(rs.getString("NOME"));
        s.setEndereco(rs.getString("ENDERECO"));
        s.setBairro(rs.getString("BAIRRO"));
        s.setRegiao(rs.getString("REGIAO"));
        s.setTelefone(rs.getString("TELEFONE"));
        s.setHorarioFuncionamento(rs.getString("HORARIO_FUNCIONAMENTO"));
        s.setDescricaoServicos(rs.getString("DESCRICAO_SERVICOS"));
        s.setFonteInformacao(rs.getString("FONTE_INFORMACAO"));

        Date verificacao = rs.getDate("DATA_VERIFICACAO");
        if (verificacao != null) {
            s.setDataVerificacao(verificacao.toLocalDate());
        }
        return s;
    }
}
