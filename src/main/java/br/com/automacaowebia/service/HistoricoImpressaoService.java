package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.DashResumo;
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
        String sql = "INSERT INTO historico_impressao (modelo, sku, quantidade, data_hora, impressora) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    public DashResumo getResumoDashboard() {
        String sql = "SELECT * FROM dash_impressao_agg";

        try (Connection conn = Database.getInstance().connectDB(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                DashResumo resumo = new DashResumo(
                        rs.getLong("total_templates"),
                        rs.getLong("total_labels"),
                        rs.getLong("total_jobs"),
                        rs.getLong("total_dia"),
                        rs.getLong("total_mes"),
                        rs.getLong("total_ano")
                );
                logger.info("Resumo do dashboard carregado: {}", resumo);
                return resumo;
            }

        } catch (SQLException e) {
            logger.error("Erro ao obter resumo do dashboard: {}", e.getMessage(), e);
        }

        return new DashResumo(0, 0, 0, 0, 0, 0);
    }

    /**
     * Lista completa do histórico de impressões.
     */
    public List<HistoricoImpressao> listarTodos() {
        List<HistoricoImpressao> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_impressao ORDER BY data_hora DESC";

        try (Connection conn = Database.getInstance().connectDB(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

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
