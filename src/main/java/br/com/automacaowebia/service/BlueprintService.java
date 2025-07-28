package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.TemplateBlueprint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class BlueprintService {

    private static final Logger log = LogManager.getLogger(BlueprintService.class);

    /* ===================================================================== *
     *  UTILITÁRIOS                                                          *
     * ===================================================================== */

    /** Abre conexão já configurada com WAL e busy_timeout                   */
    private Connection getConnection() throws SQLException {
        Connection c = Database.getInstance().connectDB();

        try (Statement st = c.createStatement()) {
            // se já estiver em WAL não altera; é rápido
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=5000");
        }
        return c;
    }

    private TemplateBlueprint map(ResultSet rs) throws SQLException {
        return new TemplateBlueprint(
                rs.getLong("id"),
                rs.getLong("template_id"),
                rs.getString("marcador"),
                rs.getString("campo_dto"),
                rs.getObject("ordem", Integer.class), // pode vir null
                rs.getBoolean("ativo"));
    }

    /* ===================================================================== *
     *  CRUD                                                                 *
     * ===================================================================== */

    public boolean salvar(TemplateBlueprint bp) {
        final String sql =
                "INSERT INTO template_blueprint " +
                "(template_id, marcador, campo_dto, ordem, ativo) " +
                "VALUES (?,?,?,?,?)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong   (1, bp.getTemplateId());
            ps.setString (2, bp.getMarcador());
            ps.setString (3, bp.getCampo());
            ps.setObject (4, bp.getOrdem());
            ps.setBoolean(5, bp.isAtivo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("Erro ao salvar blueprint", e);
            return false;
        }
    }

    public boolean atualizar(TemplateBlueprint bp) {
        final String sql = """
                UPDATE template_blueprint
                   SET marcador  = ?,
                       campo_dto = ?,
                       ordem     = ?,
                       ativo     = ?
                 WHERE id = ?""";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString (1, bp.getMarcador());
            ps.setString (2, bp.getCampo());
            ps.setObject (3, bp.getOrdem());
            ps.setBoolean(4, bp.isAtivo());
            ps.setLong   (5, bp.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("Erro ao atualizar blueprint id={}", bp.getId(), e);
            return false;
        }
    }

    /** Altera apenas o flag ATIVO – usado pelo botão “Salvar” da tela         */
    public boolean atualizarAtivo(long id, boolean ativo) {
        final String sql = "UPDATE template_blueprint SET ativo=? WHERE id=?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, ativo);
            ps.setLong   (2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("Erro ao atualizar campo 'ativo' (id={})", id, e);
            return false;
        }
    }

    public boolean deletar(long id) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM template_blueprint WHERE id=?")) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("Erro ao deletar blueprint id={}", id, e);
            return false;
        }
    }

    /* ===================================================================== *
     *  CONSULTAS                                                            *
     * ===================================================================== */

    public ObservableList<TemplateBlueprint> listarPorTemplate(long templateId) {
        ObservableList<TemplateBlueprint> lista = FXCollections.observableArrayList();
        final String sql =
                "SELECT * FROM template_blueprint " +
                "WHERE template_id = ? ORDER BY id";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, templateId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }

        } catch (SQLException e) {
            log.error("Erro ao listar blueprints do template {}", templateId, e);
        }
        return lista;
    }

    public TemplateBlueprint buscar(long id) {
        final String sql = "SELECT * FROM template_blueprint WHERE id=?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            log.error("Erro ao buscar blueprint id={}", id, e);
            return null;
        }
    }

    /* ===================================================================== *
     *  UTILITÁRIO – inserir conjunto padrão                                 *
     * ===================================================================== */

    public void inserirPadrao(long templateId) {
        final String sql = """
                INSERT INTO template_blueprint
                (template_id, marcador, campo_dto, ordem, ativo) VALUES
                (?,?,?,?,1),(?,?,?,?,1),(?,?,?,?,1),(?,?,?,?,1),(?,?,?,?,1),
                (?,?,?,?,1),(?,?,?,?,1),(?,?,?,?,1),(?,?,?,?,1)""";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // preenche os 45 parâmetros (5 colunas × 9 linhas)
            int p = 1;
            for (int linha = 1; linha <= 9; linha++) {
                ps.setLong   (p++, templateId);            // template_id
                ps.setString (p++, switch (linha) {
                    case 1 -> "B1"; case 2 -> "Q1"; case 3 -> "T4";
                    case 4 -> "T3"; case 5 -> "T5"; case 6 -> "T6";
                    case 7 -> "T1"; case 8 -> "T2"; default -> "T7";
                });
                ps.setString (p++, switch (linha) {
                    case 1 -> "codigo_barras"; case 2 -> "url_qr";
                    case 3 -> "sif";            case 4 -> "sku";
                    case 5 -> "data_producao";  case 6 -> "data_validade";
                    case 7 -> "desc_completa";  case 8 -> "desc_reduzida";
                    default -> "peso";
                });
                ps.setInt    (p++, linha);      // ordem
                ps.setBoolean(p++, true);       // ativo
            }
            ps.executeUpdate();
            log.info("Blueprints padrão inseridos para template_id {}", templateId);

        } catch (SQLException e) {
            log.error("Erro ao inserir blueprints padrão", e);
        }
    }
}