package br.com.mindconnect.dao;

import br.com.mindconnect.geografia.ServicoGeograficoDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
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

        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pagina * limite);
            ps.setInt(2, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new ServicoGeograficoDto(
                            rs.getLong("ID_SERVICO"),
                            rs.getString("NOME"),
                            rs.getString("ENDERECO"),
                            rs.getString("BAIRRO"),
                            rs.getString("MUNICIPIO"),
                            rs.getString("CEP"),
                            rs.getString("CODIGO_CNES"),
                            rs.getBigDecimal("LATITUDE"),
                            rs.getBigDecimal("LONGITUDE")
                    ));
                }
            }

            return resultado;

        } catch (SQLException e) {
            throw TradutorErroSql.traduzir("Erro ao consultar serviços para auditoria geográfica", e);
        }
    }
}