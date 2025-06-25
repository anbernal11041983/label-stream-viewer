package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.Printer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class PrinterService {

    private static final Logger logger = LogManager.getLogger(PrinterService.class);

    public ObservableList<Printer> listarTodos() {
        ObservableList<Printer> lista = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, ip, porta, modelo FROM printer ORDER BY id";

        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Printer p = new Printer();
                p.setId(rs.getLong("id"));
                p.setNome(rs.getString("nome"));
                p.setIp(rs.getString("ip"));
                p.setPorta(rs.getInt("porta"));
                p.setModelo(rs.getString("modelo"));
                lista.add(p);
            }

            logger.info("Lista de impressoras carregada. Total: {}", lista.size());

        } catch (SQLException e) {
            logger.error("Erro ao listar impressoras: {}", e.getMessage(), e);
        }

        return lista;
    }

    public void salvar(Printer printer) {
        if (printer.getId() == 0) {
            inserir(printer);
        } else {
            atualizar(printer);
        }
    }

    private void inserir(Printer printer) {
        String sql = "INSERT INTO printer (nome, ip, porta, modelo) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, printer.getNome());
            stmt.setString(2, printer.getIp());
            stmt.setInt(3, printer.getPorta());
            stmt.setString(4, printer.getModelo());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    printer.setId(rs.getLong(1));
                    logger.info("Impressora '{}' inserida com ID {}", printer.getNome(), printer.getId());
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao inserir impressora: {}", e.getMessage(), e);
        }
    }

    private void atualizar(Printer printer) {
        String sql = "UPDATE printer SET nome = ?, ip = ?, porta = ?, modelo = ? WHERE id = ?";

        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, printer.getNome());
            stmt.setString(2, printer.getIp());
            stmt.setInt(3, printer.getPorta());
            stmt.setString(4, printer.getModelo());
            stmt.setLong(5, printer.getId());

            stmt.executeUpdate();

            logger.info("Impressora ID {} atualizada com sucesso.", printer.getId());

        } catch (SQLException e) {
            logger.error("Erro ao atualizar impressora ID {}: {}", printer.getId(), e.getMessage(), e);
        }
    }

    public void remover(Printer printer) {
        String sql = "DELETE FROM printer WHERE id = ?";

        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, printer.getId());
            int count = stmt.executeUpdate();

            if (count > 0) {
                logger.info("Impressora ID {} removida com sucesso.", printer.getId());
            } else {
                logger.warn("Nenhuma impressora encontrada para ID {}.", printer.getId());
            }

        } catch (SQLException e) {
            logger.error("Erro ao remover impressora ID {}: {}", printer.getId(), e.getMessage(), e);
        }
    }

    public Printer buscarPorId(long id) {
        String sql = "SELECT id, nome, ip, porta, modelo FROM printer WHERE id = ?";

        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Printer p = new Printer();
                    p.setId(rs.getLong("id"));
                    p.setNome(rs.getString("nome"));
                    p.setIp(rs.getString("ip"));
                    p.setPorta(rs.getInt("porta"));
                    p.setModelo(rs.getString("modelo"));
                    return p;
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao buscar impressora por ID {}: {}", id, e.getMessage(), e);
        }

        return null;
    }
}
