import dao.OperacaoBancoDAO;
import model.ResultadoOperacao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Teste de integração dos objetos do banco acessados pelo Java.
 * Executar depois de sql/testes.sql, que deixa o banco em estado conhecido.
 */
public class TesteOperacaoBanco {

    private static int falhas = 0;

    private static void check(boolean condicao, String descricao) {
        if (condicao) {
            System.out.println("OK - " + descricao);
        } else {
            System.err.println("FALHA - " + descricao);
            falhas++;
        }
    }

    public static void main(String[] args) {
        OperacaoBancoDAO dao = new OperacaoBancoDAO();

        BigDecimal preco = dao.precoServico(1);
        check(preco != null && preco.compareTo(new BigDecimal("30.00")) == 0,
                "fn_preco_servico retorna 30.00 para o servico 1");

        List<String[]> faturamento = dao.faturamentoPorBarbeiro(null);
        check(!faturamento.isEmpty(),
                "fn_faturamento_por_barbeiro retorna linhas quando o ID e nulo");

        ResultadoOperacao resultado = dao.finalizarAtendimento(999999, "PIX");
        check(!resultado.sucesso() && resultado.mensagem() != null
                        && resultado.mensagem().contains("nao encontrado"),
                "procedure devolve ERRO com mensagem para agendamento inexistente");

        ResultadoOperacao duplicado = dao.finalizarAtendimento(1, "PIX");
        check(!duplicado.sucesso() && duplicado.mensagem() != null
                        && duplicado.mensagem().contains("REALIZADO"),
                "procedure devolve ERRO com mensagem para agendamento ja finalizado");

        ResultadoOperacao formaInvalida = dao.finalizarAtendimento(2, "CHEQUE");
        check(!formaInvalida.sucesso() && formaInvalida.mensagem() != null
                        && formaInvalida.mensagem().contains("Forma de pagamento invalida"),
                "procedure devolve ERRO com mensagem para forma de pagamento invalida");

        List<String[]> auditoria = dao.listarAuditoriaPagamentos();
        check(!auditoria.isEmpty(),
                "trigger de auditoria gravou registro consultavel pelo Java");

        if (falhas > 0) {
            throw new AssertionError(falhas + " verificacao(oes) falharam");
        }
        System.out.println("PASS: 6 verificacoes de banco");
    }
}
