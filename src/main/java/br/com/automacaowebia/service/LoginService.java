package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginService {

    private static final Logger logger = LogManager.getLogger(LoginService.class);

    public User autenticar(User userInput) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userInput.getUsername());
            stmt.setString(2, userInput.getPassword());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setNome(rs.getString("nome"));
                    user.setPerfil(rs.getString("perfil"));

                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
