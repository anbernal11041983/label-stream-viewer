package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.dto.ProdutoLabelData;
import br.com.automacaowebia.model.TemplateZPL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class TemplateZPLService {

    private static final Logger logger = LogManager.getLogger(TemplateZPLService.class);

    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public TemplateZPLService() {
        connection = Database.getInstance().connectDB();
    }

    /**
     * Insere um template ZPL no banco.
     */
    public Long insertTemplate(TemplateZPL template) {
        final String SQL = "INSERT INTO template_zpl (nome, tipo_arquivo, conteudo,template_impressora) VALUES (?,?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);

            ps.setString(1, template.getNome());
            ps.setString(2, template.getTipoArquivo());
            ps.setString(3, template.getConteudo());
            ps.setString(4, template.getTemplateImpressora());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long idGerado = rs.getLong(1);
                    connection.commit();
                    logger.info("Template salvo (id={})", idGerado);
                    return idGerado;
                }
            }
            connection.rollback();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ignore) {
            }
            logger.error("Erro ao inserir template", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * Atualiza um template existente.
     */
    public boolean updateTemplate(TemplateZPL template) {
        logger.info("Tentando atualizar template ID {}: {}", template.getId(), template.getNome());

        String sql = "UPDATE template_zpl SET nome=?, tipo_arquivo=?, conteudo=? WHERE id=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, template.getNome());
            preparedStatement.setString(2, template.getTipoArquivo());
            preparedStatement.setString(3, template.getConteudo());
            preparedStatement.setLong(4, template.getId());

            boolean resultado = preparedStatement.executeUpdate() > 0;
            if (resultado) {
                logger.info("Template atualizado com sucesso. ID: {}", template.getId());
            } else {
                logger.warn("Falha ao atualizar template. ID: {}", template.getId());
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Erro ao atualizar template. ID: {}", template.getId(), e);
            return false;
        }
    }

    /**
     * Remove um template pelo ID.
     */
    public boolean deleteTemplate(Long id) {
        logger.info("Tentando deletar template com ID: {}", id);

        String sql = "DELETE FROM template_zpl WHERE id=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);

            boolean resultado = preparedStatement.executeUpdate() > 0;
            if (resultado) {
                logger.info("Template deletado com sucesso. ID: {}", id);
            } else {
                logger.warn("Falha ao deletar template. ID: {}", id);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Erro ao deletar template. ID: {}", id, e);
            return false;
        }
    }

    /**
     * Lista todos os templates cadastrados.
     */
    public ObservableList<TemplateZPL> getTemplateList() {
        logger.info("Buscando lista de templates.");

        ObservableList<TemplateZPL> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM template_zpl ORDER BY id DESC";

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                TemplateZPL template = new TemplateZPL(
                        resultSet.getLong("id"),
                        resultSet.getString("nome"),
                        resultSet.getString("tipo_arquivo"),
                        resultSet.getString("conteudo"),
                        resultSet.getString("template_impressora"),
                        resultSet.getTimestamp("criado_em").toLocalDateTime()
                );
                list.add(template);
            }

            logger.info("Total de templates encontrados: {}", list.size());
        } catch (Exception e) {
            logger.error("Erro ao buscar lista de templates.", e);
        }
        return list;
    }

    /**
     * Busca um template específico pelo ID.
     */
    public TemplateZPL getTemplateById(Long id) {
        logger.info("Buscando template com ID: {}", id);

        String sql = "SELECT * FROM template_zpl WHERE id=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                TemplateZPL template = new TemplateZPL(
                        resultSet.getLong("id"),
                        resultSet.getString("nome"),
                        resultSet.getString("tipo_arquivo"),
                        resultSet.getString("conteudo"),
                        resultSet.getString("template_impressora"),
                        resultSet.getTimestamp("criado_em").toLocalDateTime()
                );
                logger.info("Template encontrado: {}", template.getNome());
                return template;
            } else {
                logger.warn("Nenhum template encontrado com ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar template com ID: {}", id, e);
        }
        return null;
    }

    public int getTotalTemplates() {
        logger.info("Consultando total de templates cadastrados.");
        String sql = "SELECT COUNT(*) AS total FROM template_zpl";
        int total = 0;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                total = resultSet.getInt("total");
                logger.info("Total de templates encontrados: {}", total);
            }
        } catch (Exception e) {
            logger.error("Erro ao consultar total de templates.", e);
        }

        return total;
    }

    public TemplateZPL getTemplateByNome(String nome) {
        String sql = "SELECT * FROM template_zpl WHERE nome = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, nome);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new TemplateZPL(
                        resultSet.getLong("id"),
                        resultSet.getString("nome"),
                        resultSet.getString("tipo_arquivo"),
                        resultSet.getString("conteudo"),
                        resultSet.getString("template_impressora"),
                        resultSet.getTimestamp("criado_em").toLocalDateTime()
                );
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar template pelo nome.", e);
        }
        return null;
    }

    public void salvarDadosProduto(Long templateId, ProdutoLabelData dados) {

        final String SQL_PRODUTO = """
        INSERT INTO produto_label_data (
            template_id, codigo_barras, url_qr, sif, sku,
            data_producao, data_validade, desc_completa, desc_reduzida, peso_kg
        ) VALUES (?,?,?,?,?,?,?,?,?,?)
        """;

        /* use o nome de coluna que realmente existe – ajuste se for “campo” */
        final String SQL_BLUEPRINT = """
        INSERT INTO template_blueprint (template_id, marcador, campo_dto, ordem, ativo) VALUES
        (?, 'B1', 'codigo_barras', 1, 1),
        (?, 'Q1', 'url_qr',        2, 1),
        (?, 'T4', 'sif',           3, 1),
        (?, 'T3', 'sku',           4, 1),
        (?, 'T5', 'data_producao', 5, 1),
        (?, 'T6', 'data_validade', 6, 1),
        (?, 'T1', 'desc_completa', 7, 1),
        (?, 'T2', 'desc_reduzida', 8, 1),
        (?, 'T7', 'peso',          9, 1)
        """;

        try (Connection conn = Database.getInstance().connectDB()) {
            conn.setAutoCommit(false);                // ───► 1 única transacção

            /* ---------- INSERE produto_label_data ---------- */
            try (PreparedStatement ps = conn.prepareStatement(SQL_PRODUTO)) {
                ps.setLong(1, templateId);
                ps.setString(2, dados.codigoBarras());
                ps.setString(3, dados.urlQrCode());
                ps.setString(4, dados.sif());
                ps.setString(5, dados.sku());
                ps.setObject(6, dados.dataProducao());
                ps.setObject(7, dados.dataValidade());
                ps.setString(8, dados.descCompleta());
                ps.setString(9, dados.descReduzida());
                ps.setBigDecimal(10, dados.pesoKg());
                ps.executeUpdate();
            }

            /* ---------- INSERE blueprints‑padrão ---------- */
            try (PreparedStatement ps = conn.prepareStatement(SQL_BLUEPRINT)) {
                for (int i = 1; i <= 9; i++) {        // 9 “?” na query
                    ps.setLong(i, templateId);
                }
                ps.executeUpdate();
            }

            conn.commit();                            // tudo OK
            logger.info("Produto e blueprints inseridos para template_id {}", templateId);

        } catch (Exception e) {
            logger.error("Falha ao salvar produto/blueprints (template_id={})", templateId, e);
            /* se a ligação ainda existir, desfazemos a transacção */
            try {
                Database.getInstance().connectDB().rollback();
            } catch (Exception ignore) {
            }
        }
    }
}
