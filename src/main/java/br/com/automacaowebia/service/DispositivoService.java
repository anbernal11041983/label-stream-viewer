package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.Dispositivo;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DispositivoService {

    private static final String INSERT =
        "INSERT INTO dispositivo(mac_address,status) VALUES(?,?)";
    private static final String UPDATE =
        "UPDATE dispositivo SET mac_address=?, status=? WHERE id=?";
    private static final String DELETE =
        "DELETE FROM dispositivo WHERE id=?";
    private static final String SELECT_ALL =
        "SELECT * FROM dispositivo";

    public void salvar(Dispositivo d) throws SQLException {
        try (Connection c = Database.getInstance().connectDB()) {
            if (d.getId() == null) {            // novo
                try (PreparedStatement ps = c.prepareStatement(INSERT)) {
                    ps.setString(1, d.getMacAddress());
                    ps.setString(2, d.getStatus());
                    ps.executeUpdate();
                }
            } else {                            // update
                try (PreparedStatement ps = c.prepareStatement(UPDATE)) {
                    ps.setString(1, d.getMacAddress());
                    ps.setString(2, d.getStatus());
                    ps.setInt(3, d.getId());
                    ps.executeUpdate();
                }
            }
        }
    }

    public void remover(Dispositivo d) throws SQLException {
        try (Connection c = Database.getInstance().connectDB();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setInt(1, d.getId());
            ps.executeUpdate();
        }
    }

    public ObservableList<Dispositivo> listarTodos() throws SQLException {
        ObservableList<Dispositivo> lista = FXCollections.observableArrayList();
        try (Connection c = Database.getInstance().connectDB();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                Dispositivo d = new Dispositivo();
                d.setId(rs.getInt("id"));
                d.setMacAddress(rs.getString("mac_address"));
                d.setStatus(rs.getString("status"));
                // criadoEm opcional
                lista.add(d);
            }
        }
        return lista;
    }
}
