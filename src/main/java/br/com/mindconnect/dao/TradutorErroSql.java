package br.com.mindconnect.dao;

import br.com.mindconnect.exception.ErroBancoDadosException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;

import java.sql.SQLException;

// Traduz os erros do Oracle (SQLException) para exceções do sistema.
// Assim nenhum detalhe de SQL chega ao cliente.
final class TradutorErroSql {

    private TradutorErroSql() {
    }

    static RuntimeException traduzir(String contexto, SQLException e) {
        int codigo = e.getErrorCode();

        // ORA-00001: violação de chave única
        if (codigo == 1) {
            return new RegraNegocioException("Já existe um registro com esses dados.");
        }
        // ORA-02292: registro possui filhos (violação de chave estrangeira ao remover)
        if (codigo == 2292) {
            return new RegraNegocioException("O registro não pode ser removido porque possui registros vinculados.");
        }
        // ORA-02291: chave estrangeira inexistente (pai não encontrado)
        if (codigo == 2291) {
            return new RequisicaoInvalidaException("Um dos registros referenciados não existe.");
        }
        return new ErroBancoDadosException(contexto, e);
    }
}
