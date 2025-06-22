package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.Billing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class BillingService {

    public ObservableList<Billing> getBillingList() {
        ObservableList<Billing> billingList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM BILLING";
        try (Connection conn = Database.getInstance().connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                billingList.add(new Billing(
                        rs.getString("item_number"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getDouble("total_amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return billingList;
    }

    public boolean insertBilling(Billing billing) {
        String sql = "INSERT INTO BILLING(item_number, quantity, price, total_amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, billing.getItem_number());
            stmt.setInt(2, billing.getQuantity());
            stmt.setDouble(3, billing.getPrice());
            stmt.setDouble(4, billing.getTotal_amount());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBilling(String itemNumber) {
        String sql = "DELETE FROM BILLING WHERE item_number = ?";
        try (Connection conn = Database.getInstance().connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getFinalAmount() {
        String sql = "SELECT SUM(total_amount) AS final_amount FROM billing";
        try (Connection conn = Database.getInstance().connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("final_amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
