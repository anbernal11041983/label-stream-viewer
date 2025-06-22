package br.com.automacaowebia.service;

import br.com.automacaowebia.config.AppProperties;
import br.com.automacaowebia.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginService {

    private static final Logger logger = LogManager.getLogger(LoginService.class);

    /**
     * Autentica o usuário no banco de dados.
     * @param user Objeto do tipo User contendo usuário e senha.
     * @return true se autenticado, false caso contrário.
     */
    public boolean autenticar(User user) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        logger.info("Tentando autenticar usuário: {}", user.getUsername());

        try (Connection conn = AppProperties.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());

            try (ResultSet rs = stmt.executeQuery()) {
                boolean autenticado = rs.next();
                if (autenticado) {
                    logger.info("Autenticação bem-sucedida para o usuário: {}", user.getUsername());
                } else {
                    logger.warn("Falha na autenticação para o usuário: {}", user.getUsername());
                }
                return autenticado;
            }
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário: {}", user.getUsername(), e);
            return false;
        }
    }
}
