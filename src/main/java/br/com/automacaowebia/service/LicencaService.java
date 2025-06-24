package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.Dispositivo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class LicencaService {

    private static final Logger logger = LogManager.getLogger(LicencaService.class);
    private final Connection connection;

    public LicencaService() {
        this.connection = Database.getInstance().connectDB();
    }

    public Dispositivo buscarDispositivo(String macAddress) {
        logger.info("Buscando dispositivo com MAC: {}", macAddress);

        String sql = "SELECT id, mac_address, status FROM dispositivo WHERE mac_address = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, macAddress);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dispositivo disp = new Dispositivo();
                disp.setId(rs.getInt("id"));
                disp.setMacAddress(rs.getString("mac_address"));
                disp.setStatus(rs.getString("status"));
                logger.info("Dispositivo encontrado: {}", disp.getStatus());
                return disp;
            } else {
                logger.info("Dispositivo não encontrado no banco.");
                return null;
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar dispositivo: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void cadastrarDispositivo(String macAddress) {
        logger.info("Cadastrando novo dispositivo MAC: {} com status BLOQUEADO", macAddress);

        String sql = "INSERT INTO dispositivo (mac_address, status) VALUES (?, 'BLOQUEADO')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, macAddress);
            stmt.executeUpdate();
            logger.info("Dispositivo cadastrado com sucesso.");
        } catch (SQLException e) {
            logger.error("Erro ao cadastrar dispositivo: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
