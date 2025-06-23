package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.HistoricoImpressao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoImpressaoService {

    private static final Logger logger = LogManager.getLogger(HistoricoImpressaoService.class);

    /**
     * Grava um registro de impressão no histórico.
     */
    public void salvarHistorico(String modelo, String sku, int quantidade, String impressora) {
        String sql = "INSERT INTO historico_impressao (modelo, sku, quantidade, data_hora, impressora) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelo);
            stmt.setString(2, sku);
            stmt.setInt(3, quantidade);
            stmt.setString(4, impressora);

            stmt.executeUpdate();

            logger.info("Histórico salvo: Modelo={}, SKU={}, Qtd={}, Impressora={}", modelo, sku, quantidade, impressora);

        } catch (SQLException e) {
            logger.error("Erro ao salvar histórico de impressão. Modelo={}, SKU={}, Qtd={}, Impressora={}. Erro: {}",
                    modelo, sku, quantidade, impressora, e.getMessage(), e);
        }
    }

    /**
     * Retorna o total de etiquetas impressas no dia atual.
     */
    public int getTotalDiario() {
        String sql = "SELECT COALESCE(SUM(quantidade),0) FROM historico_impressao WHERE DATE(data_hora) = CURRENT_DATE";

        try (Connection conn = Database.getInstance().connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt(1);
                logger.info("Total diário de impressões: {}", total);
                return total;
            }

        } catch (SQLException e) {
            logger.error("Erro ao consultar total diário de impressões: {}", e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Retorna o total de etiquetas impressas no mês atual.
     */
    public int getTotalMensal() {
        String sql = """
            SELECT COALESCE(SUM(quantidade),0)
            FROM historico_impressao
            WHERE EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE)
              AND EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
        """;

        try (Connection conn = Database.getInstance().connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt(1);
                logger.info("Total mensal de impressões: {}", total);
                return total;
            }

        } catch (SQLException e) {
            logger.error("Erro ao consultar total mensal de impressões: {}", e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Retorna o total de etiquetas impressas no ano atual.
     */
    public int getTotalAnual() {
        String sql = """
            SELECT COALESCE(SUM(quantidade),0)
            FROM historico_impressao
            WHERE EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE)
        """;

        try (Connection conn = Database.getInstance().connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt(1);
                logger.info("Total anual de impressões: {}", total);
                return total;
            }

        } catch (SQLException e) {
            logger.error("Erro ao consultar total anual de impressões: {}", e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Retorna a lista completa do histórico de impressões.
     */
    public List<HistoricoImpressao> listarTodos() {
        List<HistoricoImpressao> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_impressao ORDER BY data_hora DESC";

        try (Connection conn = Database.getInstance().connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HistoricoImpressao h = new HistoricoImpressao();
                h.setId(rs.getInt("id"));
                h.setModelo(rs.getString("modelo"));
                h.setSku(rs.getString("sku"));
                h.setQuantidade(rs.getInt("quantidade"));
                h.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
                h.setImpressora(rs.getString("impressora"));
                lista.add(h);
            }

            logger.info("Histórico de impressão listado. Total registros: {}", lista.size());

        } catch (SQLException e) {
            logger.error("Erro ao listar histórico de impressão: {}", e.getMessage(), e);
        }

        return lista;
    }
}
