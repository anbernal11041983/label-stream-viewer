package br.com.automacaowebia.service;

import br.com.automacaowebia.config.AppProperties;
import br.com.automacaowebia.model.Billing;
import br.com.automacaowebia.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {

    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public DashboardService() {
        connection = AppProperties.getInstance().connectDB();
    }

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTS";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getInt("id"),
                        resultSet.getString("item_number"),
                        resultSet.getString("item_group"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("price")
                );
                products.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public String getNewInvoiceNumber() {
        String sql = "SELECT MAX(inv_num) AS inv_num FROM sales";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                String result = resultSet.getString("inv_num");
                if (result == null) {
                    return "INV-1";
                } else {
                    int invId = Integer.parseInt(result.substring(4));
                    invId++;
                    return "INV-" + invId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "INV-1";
    }

    public ObservableList<Billing> getBillingList() {
        ObservableList<Billing> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM BILLING";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                list.add(new Billing(
                        resultSet.getString("item_number"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("price"),
                        resultSet.getDouble("total_amount")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getFinalAmount() {
        String sql = "SELECT SUM(total_amount) AS final_amount FROM billing";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getDouble("final_amount");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean insertBilling(Billing billing) {
        String sql = "INSERT INTO BILLING(item_number,quantity,price,total_amount)VALUES(?,?,?,?)";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, billing.getItem_number());
            preparedStatement.setInt(2, billing.getQuantity());
            preparedStatement.setDouble(3, billing.getPrice());
            preparedStatement.setDouble(4, billing.getTotal_amount());
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBilling(Billing billing) {
        String sql = "UPDATE billing SET quantity=?, price=?, total_amount=? WHERE item_number=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, billing.getQuantity());
            preparedStatement.setDouble(2, billing.getPrice());
            preparedStatement.setDouble(3, billing.getTotal_amount());
            preparedStatement.setString(4, billing.getItem_number());
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBilling(String itemNumber) {
        String sql = "DELETE FROM BILLING WHERE item_number=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, itemNumber);
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAllBilling() {
        String sql = "DELETE FROM BILLING";
        try {
            preparedStatement = connection.prepareStatement(sql);
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
