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

@Repository
public class ServicoAcolhimentoDAO {

    private static final String SELECT_BASE =
            "SELECT s.ID_SERVICO, s.ID_CATEGORIA_SERVICO, c.NOME AS NOME_CATEGORIA, "
                    + "s.NOME, s.ENDERECO, s.BAIRRO, s.MUNICIPIO, s.REGIAO, "
                    + "s.TELEFONE, s.HORARIO_FUNCIONAMENTO, s.DESCRICAO_SERVICOS, "
                    + "s.FONTE_INFORMACAO, s.DATA_VERIFICACAO, "
                    + "s.LATITUDE, s.LONGITUDE "
                    + "FROM GS_SERVICO_ACOLHIMENTO s "
                    + "JOIN GS_CATEGORIA_SERVICO c "
                    + "ON c.ID_CATEGORIA_SERVICO = s.ID_CATEGORIA_SERVICO ";

    private final DataSource dataSource;

    public ServicoAcolhimentoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(ServicoAcolhimento servico) {

        String sql = "INSERT INTO GS_SERVICO_ACOLHIMENTO "
                + "(ID_CATEGORIA_SERVICO, NOME, ENDERECO, BAIRRO, MUNICIPIO, REGIAO, "
                + "TELEFONE, HORARIO_FUNCIONAMENTO, DESCRICAO_SERVICOS, "
                + "FONTE_INFORMACAO, DATA_VERIFICACAO) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql,
                     new String[]{"ID_SERVICO"}
             )) {

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

    public List<ServicoAcolhimento> listar(
            String nome,
            String municipio,
            String regiao,
            Long idCategoria,
            String nomeCategoria
    ) {

        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append("WHERE 1 = 1 ");

        List<Object> parametros = new ArrayList<>();

        if (nome != null && !nome.isBlank()) {
            sql.append("AND UPPER(s.NOME) LIKE ? ");
            parametros.add("%" + nome.trim().toUpperCase() + "%");
        }

        if (municipio != null && !municipio.isBlank()) {
            sql.append("AND UPPER(TRIM(s.MUNICIPIO)) = UPPER(TRIM(?)) ");
            parametros.add(municipio.trim());
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

    public List<String> listarMunicipios() {

        String sql = "SELECT DISTINCT TRIM(MUNICIPIO) AS MUNICIPIO "
                + "FROM GS_SERVICO_ACOLHIMENTO "
                + "WHERE MUNICIPIO IS NOT NULL "
                + "AND TRIM(MUNICIPIO) IS NOT NULL "
                + "ORDER BY TRIM(MUNICIPIO)";

        List<String> municipios = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                municipios.add(rs.getString("MUNICIPIO"));
            }

            return municipios;

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao listar municípios", e);
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

    public boolean existeDuplicado(
            String nome,
            String endereco,
            Long idIgnorar
    ) {

        String sql = "SELECT COUNT(*) FROM GS_SERVICO_ACOLHIMENTO "
                + "WHERE UPPER(NOME) = UPPER(?) "
                + "AND UPPER(ENDERECO) = UPPER(?) "
                + "AND ID_SERVICO <> ?";

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

    public int contarPorCategoria(Long idCategoria) {

        String sql = "SELECT COUNT(*) FROM GS_SERVICO_ACOLHIMENTO "
                + "WHERE ID_CATEGORIA_SERVICO = ?";

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

        String sql = "UPDATE GS_SERVICO_ACOLHIMENTO SET "
                + "ID_CATEGORIA_SERVICO = ?, "
                + "NOME = ?, "
                + "ENDERECO = ?, "
                + "BAIRRO = ?, "
                + "MUNICIPIO = ?, "
                + "REGIAO = ?, "
                + "TELEFONE = ?, "
                + "HORARIO_FUNCIONAMENTO = ?, "
                + "DESCRICAO_SERVICOS = ?, "
                + "FONTE_INFORMACAO = ?, "
                + "DATA_VERIFICACAO = ? "
                + "WHERE ID_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            preencher(ps, servico);
            ps.setLong(12, servico.getIdServico());

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao atualizar serviço", e);
        }
    }

    public int excluir(Long id) {

        String sql = "DELETE FROM GS_SERVICO_ACOLHIMENTO "
                + "WHERE ID_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao excluir serviço", e);
        }
    }

    private void preencher(
            PreparedStatement ps,
            ServicoAcolhimento servico
    ) throws SQLException {

        ps.setLong(1, servico.getIdCategoriaServico());
        ps.setString(2, servico.getNome());
        ps.setString(3, servico.getEndereco());
        ps.setString(4, servico.getBairro());
        ps.setString(5, servico.getMunicipio());
        ps.setString(6, servico.getRegiao());
        ps.setString(7, servico.getTelefone());
        ps.setString(8, servico.getHorarioFuncionamento());
        ps.setString(9, servico.getDescricaoServicos());
        ps.setString(10, servico.getFonteInformacao());

        if (servico.getDataVerificacao() != null) {
            ps.setDate(11, Date.valueOf(servico.getDataVerificacao()));
        } else {
            ps.setNull(11, Types.DATE);
        }
    }

    private ServicoAcolhimento mapear(ResultSet rs) throws SQLException {

        ServicoAcolhimento servico = new ServicoAcolhimento();

        servico.setIdServico(rs.getLong("ID_SERVICO"));
        servico.setIdCategoriaServico(rs.getLong("ID_CATEGORIA_SERVICO"));
        servico.setNomeCategoria(rs.getString("NOME_CATEGORIA"));
        servico.setNome(rs.getString("NOME"));
        servico.setEndereco(rs.getString("ENDERECO"));
        servico.setBairro(rs.getString("BAIRRO"));
        servico.setMunicipio(rs.getString("MUNICIPIO"));
        servico.setRegiao(rs.getString("REGIAO"));
        servico.setTelefone(rs.getString("TELEFONE"));
        servico.setHorarioFuncionamento(rs.getString("HORARIO_FUNCIONAMENTO"));
        servico.setDescricaoServicos(rs.getString("DESCRICAO_SERVICOS"));
        servico.setFonteInformacao(rs.getString("FONTE_INFORMACAO"));
        servico.setLatitude(rs.getBigDecimal("LATITUDE"));
        servico.setLongitude(rs.getBigDecimal("LONGITUDE"));

        Date verificacao = rs.getDate("DATA_VERIFICACAO");

        if (verificacao != null) {
            servico.setDataVerificacao(verificacao.toLocalDate());
        }

        return servico;
    }
}
