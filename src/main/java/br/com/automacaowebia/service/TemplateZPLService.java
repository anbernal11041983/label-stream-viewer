package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.TemplateZPL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;

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
    public boolean insertTemplate(TemplateZPL template) {
        logger.info("Tentando inserir template: {}", template.getNome());

        if (template.getNome() == null || template.getNome().isBlank()) {
            logger.warn("Nome do template não pode ser vazio.");
            throw new IllegalArgumentException("O nome do template é obrigatório.");
        }

        String sql = "INSERT INTO template_zpl (nome, tipo_arquivo, conteudo) VALUES (?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, template.getNome());
            preparedStatement.setString(2, template.getTipoArquivo());
            preparedStatement.setString(3, template.getConteudo());

            boolean resultado = preparedStatement.executeUpdate() > 0;
            if (resultado) {
                logger.info("Template inserido com sucesso: {}", template.getNome());
            } else {
                logger.warn("Falha ao inserir template: {}", template.getNome());
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Erro ao inserir template: {}", template.getNome(), e);
            return false;
        }
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
}
