package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.Printer;
import br.com.automacaowebia.printer.ContinuousPrinter;
import br.com.automacaowebia.printer.LaserDriver;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PrinterService {

    private static final Logger logger = LogManager.getLogger(PrinterService.class);
    private final LaserDriver laserDriver = new LaserDriver();
    private final Map<String, ContinuousPrinter> jobs = new ConcurrentHashMap<>();
    private final HistoricoImpressaoService historicoSrv = new HistoricoImpressaoService();

    public ObservableList<Printer> listarTodos() {
        ObservableList<Printer> lista = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, ip, porta, modelo FROM printer ORDER BY id";

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

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

    public void teste(Printer printer) throws IOException {
        laserDriver.testPrint(printer, "O teste com o aplicativo deu certo - JULIO");
        logger.info("Teste de impressão enviado a {}:{}", printer.getIp(), printer.getPorta());
    }

    public void imprimir(Printer pr, String template, int quantidadeInpressao,
            Consumer<String> logCallback, int espacamento, Map<String, String> vars) throws IOException {
        laserDriver.printBatch(pr, template, quantidadeInpressao, logCallback, espacamento, vars);
        logger.info("Impressão enviado a {}:{}", pr.getIp(), pr.getPorta());

        if (logCallback != null) {
            logCallback.accept("✔ Teste de impressão enviado a " + pr.getIp() + ":" + pr.getPorta());
        }
    }

    private void inserir(Printer printer) {
        String sql = "INSERT INTO printer (nome, ip, porta, modelo) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    private static String key(Printer pr) {
        return pr.getIp() + ":" + pr.getPorta();
    }

    public void iniciarImpressao(Printer pr,
            String template,
            Integer quantidade, // opcional
            int espacamentoMs,
            Map<String, String> vars,
            Consumer<String> cb) {

        final String k = key(pr);

        ContinuousPrinter existing = jobs.get(k);
        if (existing != null && existing.isRunning()) {
            throw new IllegalStateException("Já existe um job em execução para " + k);
        }

        ContinuousPrinter job = (quantidade != null && quantidade > 0)
                ? new ContinuousPrinter(pr, template, espacamentoMs, vars, cb, quantidade)
                : new ContinuousPrinter(pr, template, espacamentoMs, vars, cb);

        jobs.put(k, job);

        if (quantidade != null && quantidade > 0) {
            job.start(quantidade);
            if (cb != null) {
                cb.accept("▶ Iniciando lote de " + quantidade + " peça(s) em " + k);
            }

        } else {
            job.start();
            if (cb != null) {
                cb.accept("▶ Iniciando impressão contínua em " + k);
            }
        }
    }

    public void pararImpressao(Printer pr, Consumer<String> cb,
            String modeloParaHistorico, String skuParaHistorico) {
        final String k = key(pr);
        ContinuousPrinter job = jobs.get(k);
        if (job == null) {
            if (cb != null) {
                cb.accept("ℹ Nenhum job conhecido para " + k);
            }
            return;
        }

        if (job.isRunning()) {
            job.stop();
            try {
                while (job.isRunning()) {
                    Thread.sleep(150);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        int produzidas = job.getProducedCount();
        if (produzidas > 0 && skuParaHistorico != null && modeloParaHistorico !=null) {
            historicoSrv.salvarHistorico(
                    modeloParaHistorico, skuParaHistorico, produzidas, k
            );
        }
        jobs.remove(k);

        if (cb != null) {
            cb.accept("🛑 Impressão finalizada em " + k + " | Produzidas: " + produzidas);
        }
    }

    public boolean isExecutando(Printer pr) {
        ContinuousPrinter job = jobs.get(key(pr));
        return job != null && job.isRunning();
    }
}
