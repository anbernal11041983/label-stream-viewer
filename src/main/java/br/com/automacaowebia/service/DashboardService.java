package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;

import java.sql.*;

public class DashboardService {

    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public DashboardService() {
        connection = Database.getInstance().connectDB();
    }
}
