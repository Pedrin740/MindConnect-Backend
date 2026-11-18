package br.com.mindconnect.dao;

import br.com.mindconnect.geografia.ServicoGeograficoDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AuditoriaGeograficaDAO {

    private final DataSource dataSource;

    public AuditoriaGeograficaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ServicoGeograficoDto> listarParaAuditoria(int pagina, int limite) {
        String sql = "SELECT ID_SERVICO, NOME, ENDERECO, BAIRRO, MUNICIPIO, CEP, CODIGO_CNES, LATITUDE, LONGITUDE "
                + "FROM GS_SERVICO_ACOLHIMENTO ORDER BY ID_SERVICO "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<ServicoGeograficoDto> resultado = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pagina * limite);
            ps.setInt(2, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearServico(rs));
                }
            }

            return resultado;

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(
                    "Erro ao consultar serviços para auditoria geográfica", e
            );
        }
    }

    public ServicoGeograficoDto buscarPorId(Long idServico) {
        String sql = "SELECT ID_SERVICO, NOME, ENDERECO, BAIRRO, MUNICIPIO, CEP, CODIGO_CNES, LATITUDE, LONGITUDE "
                + "FROM GS_SERVICO_ACOLHIMENTO WHERE ID_SERVICO = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idServico);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearServico(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(
                    "Erro ao consultar serviço para atualização geográfica", e
            );
        }
    }

    public boolean atualizarCoordenadasSeInalteradas(
            Long idServico,
            BigDecimal latitudeAtual,
            BigDecimal longitudeAtual,
            BigDecimal novaLatitude,
            BigDecimal novaLongitude
    ) {
        String sql = """
                UPDATE GS_SERVICO_ACOLHIMENTO
                   SET LATITUDE = ?, LONGITUDE = ?
                 WHERE ID_SERVICO = ?
                   AND ((LATITUDE = ?) OR (LATITUDE IS NULL AND ? IS NULL))
                   AND ((LONGITUDE = ?) OR (LONGITUDE IS NULL AND ? IS NULL))
                """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, novaLatitude);
            ps.setBigDecimal(2, novaLongitude);
            ps.setLong(3, idServico);

            ps.setBigDecimal(4, latitudeAtual);
            ps.setBigDecimal(5, latitudeAtual);

            ps.setBigDecimal(6, longitudeAtual);
            ps.setBigDecimal(7, longitudeAtual);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(
                    "Erro ao atualizar coordenadas do serviço", e
            );
        }
    }

    private ServicoGeograficoDto mapearServico(ResultSet rs) throws SQLException {
        return new ServicoGeograficoDto(
                rs.getLong("ID_SERVICO"),
                rs.getString("NOME"),
                rs.getString("ENDERECO"),
                rs.getString("BAIRRO"),
                rs.getString("MUNICIPIO"),
                rs.getString("CEP"),
                rs.getString("CODIGO_CNES"),
                rs.getBigDecimal("LATITUDE"),
                rs.getBigDecimal("LONGITUDE")
        );
    }

    public List<ServicoGeograficoDto> listarServicosDaCapitalParaComparacaoCnes() {

        String sql = """
            SELECT ID_SERVICO, NOME, ENDERECO, BAIRRO,
                   MUNICIPIO, CEP, CODIGO_CNES, LATITUDE, LONGITUDE
            FROM GS_SERVICO_ACOLHIMENTO
            WHERE CODIGO_IBGE_MUNICIPIO = '355030'
              AND UF = 'SP'
            ORDER BY ID_SERVICO
            """;

        List<ServicoGeograficoDto> resultado = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapearServico(rs));
            }

            return resultado;

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(
                    "Erro ao consultar serviços da capital para comparação com o CNES",
                    e
            );
        }
    }
}
