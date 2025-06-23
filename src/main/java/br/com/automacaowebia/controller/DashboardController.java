package br.com.automacaowebia.controller;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.model.*;
import br.com.automacaowebia.service.ImpressaoZPLService;
import br.com.automacaowebia.service.TemplateZPLService;
import br.com.automacaowebia.service.ZplCacheService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.burningwave.core.assembler.StaticComponentContainer.Modules;

public class DashboardController implements Initializable {

    private double x;
    private double y;

    @FXML
    private Button billing_btn;

    @FXML
    private AnchorPane template_pane;

    @FXML
    private Button impressao_btn;

    @FXML
    private Button dashboard_btn;

    @FXML
    private AnchorPane impressao_zpl;

    @FXML
    private AnchorPane dasboard_pane;

    @FXML
    private Button purchase_btn;

    @FXML
    private AnchorPane purchase_pane;

    @FXML
    private Button sales_btn;

    @FXML
    private AnchorPane sales_pane;

    @FXML
    private Label user;

    @FXML
    private Label inv_num;

    private Connection connection;

    private Statement statement;

    private PreparedStatement preparedStatement;

    private ResultSet resultSet;

    @FXML
    private Button bill_add;

    @FXML
    private Button bill_clear;

    @FXML
    private DatePicker bill_date;

    @FXML
    private TextField bill_item;

    @FXML
    private TextField template_nome;

    @FXML
    private TextField bill_phone;

    @FXML
    private TextField bill_price;

    @FXML
    private Button bill_print;

    @FXML
    private ComboBox<?> bill_quantity;

    @FXML
    private Button btn_salvar_template;

    @FXML
    private TextField bill_total_amount;

    @FXML
    private TableView<TemplateZPL> lista_template;

    @FXML
    private TextField billing_table_search;

    @FXML
    private Label final_amount;

    @FXML
    private ProgressBar progressPrint;

    @FXML
    private Button btnPrint;

    @FXML
    private ComboBox<String> comboTemplate;

    @FXML
    private ComboBox<String> comboSku;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Label lblPreviewPlaceholder;

    private String invoiceList[] = {"BX123456", "ZX123456", "AX123456"};

    private String quantityList[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    @FXML
    private TableColumn<TemplateZPL, String> col_template_nome;

    @FXML
    private TableColumn<TemplateZPL, String> col_template_tipo;

    @FXML
    private TableColumn<TemplateZPL, String> col_template_criado;

    @FXML
    private TableColumn<TemplateZPL, Void> col_template_acao;

    @FXML
    private TableColumn<?, ?> col_bill_price;

    @FXML
    private TableColumn<?, ?> col_bill_total_amt;

    @FXML
    private Button cust_btn_add;

    @FXML
    private Button cust_btn_delete;

    @FXML
    private Button cust_btn_edit;

    @FXML
    private TableColumn<?, ?> cust_col_id;

    @FXML
    private TableColumn<?, ?> cust_col_name;

    @FXML
    private TableColumn<?, ?> cust_col_phone;

    @FXML
    private TextField cust_field_name;

    @FXML
    private TextField cust_field_phone;

    @FXML
    private TextField customer_search;

    @FXML
    private TableView<Customer> customer_table;

    @FXML
    private TableColumn<?, ?> sales_col_cust_name;

    @FXML
    private TableColumn<?, ?> sales_col_date_of_sales;

    @FXML
    private TableColumn<?, ?> sales_col_id;

    @FXML
    private TableColumn<?, ?> sales_col_inv_num;

    @FXML
    private TableColumn<?, ?> sales_col_quantity;

    @FXML
    private TableColumn<?, ?> sales_col_total_amount;

    @FXML
    private TableColumn<?, ?> sales_col_price;

    @FXML
    private TableColumn<?, ?> sales_col_item_num;

    @FXML
    private TableView<Sales> sales_table;

    @FXML
    private Label sales_total_amount;

    @FXML
    private Button purchase_btn_add;

    @FXML
    private Button purchase_btn_print;

    @FXML
    private Label purchase_total_amount;

    @FXML
    private TableColumn<?, ?> purchase_col_date_of_purchase;

    @FXML
    private TableColumn<?, ?> purchase_col_id;

    @FXML
    private TableColumn<?, ?> purchase_col_invoice;

    @FXML
    private TableColumn<?, ?> purchase_col_shop_details;

    @FXML
    private TableColumn<?, ?> purchase_col_total_amount;

    @FXML
    private TableColumn<?, ?> purchase_col_total_items;

    @FXML
    private TableView<Purchase> purchase_table;

    @FXML
    private Label dash_total_items_sold_this_month;

    @FXML
    private Label dash_total_purchase;

    @FXML
    private Label dash_total_sales_items_this_month_name;

    @FXML
    private Label dash_total_sales_this_month;

    @FXML
    private Label dash_total_sales_this_month_name;

    @FXML
    private Label dash_total_sold;

    @FXML
    private Label dash_total_stocks;

    @FXML
    private Button signout_btn;

    @FXML
    private TextField txtQuantidade;

    @FXML
    private TextField txtWidth;

    @FXML
    private TextField txtHeight;

    @FXML
    private ComboBox<String> comboUnidade;

    List<Product> productsList;

    private final TemplateZPLService templateService = new TemplateZPLService();
    private final ImpressaoZPLService impressaoZPLService = new ImpressaoZPLService();
    private String conteudoTemplate; // Para guardar o conteúdo carregado
    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    public void onExit() {
        System.exit(0);
    }

    public void activateAnchorPane() {
        dashboard_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(true);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(false);
            sales_pane.setVisible(false);
            purchase_pane.setVisible(false);
            dashboard_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7),  rgba(255,106,239,0.7))");
            billing_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            impressao_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            sales_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            purchase_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
        });
        billing_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(true);
            impressao_zpl.setVisible(false);
            sales_pane.setVisible(false);
            purchase_pane.setVisible(false);
            dashboard_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            billing_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7),  rgba(255,106,239,0.7))");
            impressao_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            sales_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            purchase_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
        });
        impressao_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(true);
            sales_pane.setVisible(false);
            purchase_pane.setVisible(false);
            dashboard_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            billing_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            impressao_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7),  rgba(255,106,239,0.7))");
            sales_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            purchase_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
        });
        sales_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(false);
            sales_pane.setVisible(true);
            purchase_pane.setVisible(false);
            dashboard_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            billing_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            impressao_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            sales_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7),  rgba(255,106,239,0.7))");
            purchase_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
        });
        purchase_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(false);
            sales_pane.setVisible(false);
            purchase_pane.setVisible(true);
            dashboard_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            billing_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            impressao_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            sales_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2),  rgba(255,106,239,0.2))");
            purchase_btn.setStyle("-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7),  rgba(255,106,239,0.7))");
        });

    }

    public void setUsername() {
        user.setText("Admin");
    }

    public void activateDashboard() {
        dasboard_pane.setVisible(true);
        template_pane.setVisible(false);
        impressao_zpl.setVisible(false);
        sales_pane.setVisible(false);
        purchase_pane.setVisible(false);
    }

    public List<Product> getItemsList() {
        productsList = new ArrayList<>();
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM PRODUCTS";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            Product product;
            while (resultSet.next()) {
                product = new Product(Integer.parseInt(resultSet.getString("id")), resultSet.getString("item_number"), resultSet.getString("item_group"), Integer.parseInt(resultSet.getString("quantity")), Double.parseDouble(resultSet.getString("price")));
                productsList.add(product);
            }
        } catch (Exception err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeight(500);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText(err.getMessage());
            alert.showAndWait();
        }
        return productsList;
    }

    public void setInvoiceNum() {
        connection = Database.getInstance().connectDB();
        String sql = "SELECT MAX(inv_num) AS inv_num FROM sales";

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String result = resultSet.getString("inv_num");
                if (result == null) {
                    Invoice.billingInvoiceNumber = "INV-1";
                    inv_num.setText(Invoice.billingInvoiceNumber);
                } else {
                    int invId = Integer.parseInt(result.substring(4));
                    invId++;
                    Invoice.billingInvoiceNumber = "INV-" + invId;
                    inv_num.setText(Invoice.billingInvoiceNumber);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void checkForPriceandQuantity() {
        if (!bill_price.getText().isBlank() && !bill_quantity.getSelectionModel().isEmpty()) {
            bill_total_amount.setText(String.valueOf(Integer.parseInt(bill_price.getText()) * Integer.parseInt(bill_quantity.getValue().toString())));
        } else {
            bill_total_amount.setText("0");
        }
    }

    public void getPriceOfTheItem() {
        try {
            Product product = productsList.stream().filter(prod -> prod.getItemNumber().equals(bill_item.getText())).findAny().get();
            System.out.println("Price " + product.getPrice());
            bill_price.setText(String.valueOf((int) product.getPrice()));
        } catch (Exception err) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText("Exception Item Number : " + err.getMessage());
            alert.showAndWait();
        }
    }

    public void onInputTextChanged() {
        bill_price.setOnKeyReleased(event -> checkForPriceandQuantity());
        bill_price.setOnKeyPressed(event -> checkForPriceandQuantity());
        bill_price.setOnKeyTyped(event -> checkForPriceandQuantity());
        bill_quantity.setOnAction(actionEvent -> checkForPriceandQuantity());
        bill_item.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode().equals(KeyCode.ENTER)) {
                getPriceOfTheItem();
            }
        });
    }

    public ObservableList<Billing> listBilligData() {
        ObservableList<Billing> billingList = FXCollections.observableArrayList();
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM BILLING";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            Billing billingData;
            while (resultSet.next()) {
                billingData = new Billing(resultSet.getString("item_number"), Integer.parseInt(resultSet.getString("quantity")), Double.parseDouble(resultSet.getString("price")), Double.parseDouble(resultSet.getString("total_amount")));
                billingList.addAll(billingData);
            }

        } catch (Exception err) {
            err.printStackTrace();
        }
        return billingList;
    }

    public void calculateFinalAmount() {
        connection = Database.getInstance().connectDB();
        String sql = "SELECT SUM(total_amount) AS final_amount FROM billing";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                final_amount.setText(resultSet.getString("final_amount"));
            }

        } catch (Exception err) {
            err.printStackTrace();
        }

    }

    public void billClearCustomerData() {
        template_nome.setText("");
        bill_phone.setText("");
    }

    public void limparCampoTemplate() {
        template_nome.setText("");
    }

    public boolean saveCustomerDetails() {
        if (bill_phone.getText().isBlank() || template_nome.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText("Kindly Fill Customer Name and Phone number.");
            alert.showAndWait();
            return false;
        }
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM CUSTOMERS WHERE phonenumber=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, bill_phone.getText());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                alert.setHeaderText(null);
                alert.setContentText("Customer Data is already present in customer table. Proceeding further to save invoice.");
                alert.showAndWait();
                return true;
            } else {
                String customerSql = "INSERT INTO CUSTOMERS(name,phonenumber) VALUES(?,?)";
                preparedStatement = connection.prepareStatement(customerSql);
                preparedStatement.setString(1, template_nome.getText());
                preparedStatement.setString(2, bill_phone.getText());
                int result = preparedStatement.executeUpdate();
                if (result > 0) {
                    showCustomerData();
                    return true;
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Customer Data not saved. Please fill name and phone number correctly.");
                    alert.showAndWait();
                    return false;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    public void customerClearData() {
        cust_field_name.setText("");
        cust_field_phone.setText("");
    }

    public ObservableList<Customer> listCustomerData() {
        ObservableList<Customer> customersList = FXCollections.observableArrayList();
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM Customers";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            Customer customer;
            while (resultSet.next()) {
                customer = new Customer(Integer.parseInt(resultSet.getString("id")), resultSet.getString("name"), resultSet.getString("phone_number"));
                customersList.addAll(customer);
            }

        } catch (Exception err) {
            err.printStackTrace();
        }
        return customersList;
    }

    public void showCustomerData() {
        ObservableList<Customer> customerList = listCustomerData();
        cust_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        cust_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        cust_col_phone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        customer_table.setItems(customerList);
    }

    public boolean checkForCustomerAvailability() {
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM CUSTOMERS WHERE phone_number=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cust_field_phone.getText());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                alert.setHeaderText(null);
                alert.setContentText("Customer already present in the customer table.");
                alert.showAndWait();
                return false;
            } else {
                return true;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        return false;
    }

    public void addCustomerData() {
        if (!checkForCustomerAvailability()) {
            return;
        }
        connection = Database.getInstance().connectDB();
        String sql = "INSERT INTO CUSTOMERS(name,phonenumber)VALUES(?,?)";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cust_field_name.getText());
            preparedStatement.setString(2, cust_field_phone.getText());
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                showCustomerData();
                customerClearData();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill the mandatory data such as name and phone number.");
                alert.showAndWait();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void selectCustomerTableData() {
        int num = customer_table.getSelectionModel().getSelectedIndex();
        Customer customerData = customer_table.getSelectionModel().getSelectedItem();
        if (num - 1 < -1) {
            return;
        }

        cust_field_name.setText(customerData.getName());
        cust_field_phone.setText(customerData.getPhoneNumber());
    }

    public void updateCustomerData() {
        if (cust_field_phone.getText().isBlank() || cust_field_name.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill the mandatory data such as name, phone number .");
            alert.showAndWait();
            return;
        }
        connection = Database.getInstance().connectDB();
        String sql = "UPDATE CUSTOMERS SET name=? WHERE phonenumber=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cust_field_name.getText());
            preparedStatement.setString(2, cust_field_phone.getText());
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                showCustomerData();
                customerClearData();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill the mandatory data such as name, phone number .");
                alert.showAndWait();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void deleteCustomerData() {
        if (customer_table.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText("Please select customer for deletion.");
            alert.showAndWait();
            return;
        }
        connection = Database.getInstance().connectDB();
        String sql = "DELETE FROM CUSTOMERS WHERE phonenumber=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, customer_table.getSelectionModel().getSelectedItem().getPhoneNumber());
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                showCustomerData();
                customerClearData();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                alert.setHeaderText(null);
                alert.setContentText("No data present in the customer table.");
                alert.showAndWait();
            }
        } catch (Exception err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeight(500);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText(err.getMessage());
            alert.showAndWait();
        }
    }

    public void printCustomersDetails() {
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM customers";
        try {
            JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getClassLoader().getResourceAsStream("jasper-reports/customers.jrxml"));
            JRDesignQuery updateQuery = new JRDesignQuery();
            updateQuery.setText(sql);
            jasperDesign.setQuery(updateQuery);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, connection);
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void getTotalSalesAmount() {

    }

    public ObservableList<Sales> listSalesData() {
        ObservableList<Sales> salesList = FXCollections.observableArrayList();
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM SALES s INNER JOIN CUSTOMERS c ON s.cust_id=c.id";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            Sales sale;
            while (resultSet.next()) {
                sale = new Sales(Integer.parseInt(resultSet.getString("id")), resultSet.getString("inv_num"), Integer.parseInt(resultSet.getString("cust_id")), resultSet.getString("name"), Double.parseDouble(resultSet.getString("price")), Integer.parseInt(resultSet.getString("quantity")), Double.parseDouble(resultSet.getString("total_amount")), resultSet.getString("date"), resultSet.getString("item_number"));
                salesList.addAll(sale);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return salesList;
    }

    public void showSalesData() {
        ObservableList<Sales> salesList = listSalesData();
        sales_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        sales_col_inv_num.setCellValueFactory(new PropertyValueFactory<>("inv_num"));
        sales_col_cust_name.setCellValueFactory(new PropertyValueFactory<>("custName"));
        sales_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        sales_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sales_col_total_amount.setCellValueFactory(new PropertyValueFactory<>("total_amount"));
        sales_col_date_of_sales.setCellValueFactory(new PropertyValueFactory<>("date"));
        sales_col_item_num.setCellValueFactory(new PropertyValueFactory<>("item_num"));
        sales_table.setItems(salesList);

        getTotalSalesAmount();
    }

    public void printSalesDetails() {
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM sales s INNER JOIN customers c ON s.cust_id=c.id";
        try {
            JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getClassLoader().getResourceAsStream("jasper-reports/sales_report.jrxml"));
            JRDesignQuery updateQuery = new JRDesignQuery();
            updateQuery.setText(sql);
            jasperDesign.setQuery(updateQuery);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, connection);
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void getTotalPurchaseAmount() {

    }

    public void printPurchaseDetails() {
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM purchase";
        try {
            JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getClassLoader().getResourceAsStream("jasper-reports/purchase_report.jrxml"));
            JRDesignQuery updateQuery = new JRDesignQuery();
            updateQuery.setText(sql);
            jasperDesign.setQuery(updateQuery);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, connection);
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public ObservableList<Purchase> listPurchaseData() {
        ObservableList<Purchase> purchaseList = FXCollections.observableArrayList();
        connection = Database.getInstance().connectDB();
        String sql = "SELECT * FROM purchase";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            Purchase purchase;
            while (resultSet.next()) {
                purchase = new Purchase(Integer.parseInt(resultSet.getString("id")), resultSet.getString("invoice"), resultSet.getString("shop_and_address"), Integer.parseInt(resultSet.getString("total_items")), Double.parseDouble(resultSet.getString("total_amount")), resultSet.getString("date_of_purchase"));
                purchaseList.addAll(purchase);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return purchaseList;
    }

    public void showPurchaseData() {
        ObservableList<Purchase> purchaseList = listPurchaseData();
        purchase_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        purchase_col_invoice.setCellValueFactory(new PropertyValueFactory<>("invoice"));
        purchase_col_shop_details.setCellValueFactory(new PropertyValueFactory<>("shopDetails"));
        purchase_col_total_items.setCellValueFactory(new PropertyValueFactory<>("totalItems"));
        purchase_col_total_amount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        purchase_col_date_of_purchase.setCellValueFactory(new PropertyValueFactory<>("dateOfPurchase"));
        purchase_table.setItems(purchaseList);
        getTotalPurchaseAmount();
    }

    public void getTotalPurchase() {

    }

    public void getTotalSales() {

    }

    public void getTotalStocks() {
        int totalPurchase = Integer.parseInt(dash_total_purchase.getText());
        int total_sold = Integer.parseInt(dash_total_sold.getText());
        int totalStockLeft = totalPurchase - total_sold;
        dash_total_stocks.setText(String.valueOf(totalStockLeft));
    }

    public void getSalesDetailsOfThisMonth() {

    }

    public void getItemSoldThisMonth() {

    }

    public void showDashboardData() {
        getTotalPurchase();
        getTotalSales();
        getTotalStocks();
        getSalesDetailsOfThisMonth();
        getItemSoldThisMonth();
    }

    public void signOut() {
        signout_btn.getScene().getWindow().hide();
        try {
            //Parent root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("/br/com/automacaowebia/login-view.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            root.setOnMousePressed((event) -> {
                x = event.getSceneX();
                y = event.getSceneY();
            });
            root.setOnMouseDragged((event) -> {
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);
            });

            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (Exception err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeight(500);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText(err.getMessage());
            alert.showAndWait();
        }

    }

    public void loadTemplate() {
        logger.info("Abrindo seletor de arquivos para carregar template.");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Template TXT");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos TXT", "*.txt")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            try {
                conteudoTemplate = java.nio.file.Files.readString(selectedFile.toPath());
                //template_nome.setText(selectedFile.getName().replace(".txt", ""));

                logger.info("Template '{}' carregado com sucesso.", selectedFile.getAbsolutePath());

            } catch (Exception e) {
                logger.error("Erro ao carregar arquivo: {}", e.getMessage(), e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeight(500);
                alert.setTitle("Alerta");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao carregar o arquivo.");
                alert.showAndWait();
            }
        } else {
            logger.warn("Nenhum arquivo selecionado.");
        }
    }

    public void salvarTemplate() {
        String nome = template_nome.getText();

        logger.info("Tentando salvar template com nome '{}'.", nome);

        if (nome == null || nome.isBlank()) {
            logger.warn("Nome do template não preenchido.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeight(500);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("O nome do template é obrigatório.");
            alert.showAndWait();
            return;
        }

        if (conteudoTemplate == null || conteudoTemplate.isBlank()) {
            logger.warn("Nenhum conteúdo de template carregado para salvar.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeight(500);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("Nenhum arquivo foi carregado");
            alert.showAndWait();
            return;
        }

        TemplateZPL template = new TemplateZPL(nome, "TXT", conteudoTemplate);

        boolean sucesso = templateService.insertTemplate(template);
        if (sucesso) {
            logger.info("Template '{}' salvo com sucesso.", nome);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeight(500);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText("Template salvo com sucesso.");
            alert.showAndWait();
            limparCampoTemplate();
            carregarListaTemplate();
        } else {
            logger.error("Erro ao salvar template '{}'.", nome);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeight(500);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao salvar template.");
            alert.showAndWait();
            limparCampoTemplate();
        }
    }

    public void carregarListaTemplate() {
        // Configuração das colunas padrão
        col_template_nome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        col_template_tipo.setCellValueFactory(new PropertyValueFactory<>("tipoArquivo"));
        col_template_criado.setCellValueFactory(cellData -> {
            var criado = cellData.getValue().getCriadoEm();
            String dataFormatada = (criado != null)
                    ? criado.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";
            return new SimpleStringProperty(dataFormatada);
        });

        // Adicionar coluna de ações (botão excluir)
        col_template_acao.setCellFactory(param -> new TableCell<TemplateZPL, Void>() {
            private final Button btnExcluir = new Button();

            {
                btnExcluir.getStyleClass().add("delete"); // aplica classe CSS
                btnExcluir.setPrefWidth(80);
                btnExcluir.setPrefHeight(28);

                btnExcluir.getStyleClass().add("delete");

                // Ícone no botão (usando FontAwesomeIconView)
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("TRASH");
                icon.setSize("14");
                icon.setFill(javafx.scene.paint.Color.WHITE);

                btnExcluir.setGraphic(icon);
                btnExcluir.setText("Excluir");
                btnExcluir.setOnAction(event -> {
                    TemplateZPL template = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmação");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Deseja realmente excluir o template '" + template.getNome() + "'?");

                    var result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        boolean sucesso = templateService.deleteTemplate(template.getId());
                        if (sucesso) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText(null);
                            alert.setContentText("Template excluído com sucesso.");
                            alert.showAndWait();
                            carregarListaTemplate(); // Atualiza lista
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText(null);
                            alert.setContentText("Erro ao excluir template.");
                            alert.showAndWait();
                        }
                    }
                });

                // Estilo opcional do botão
                btnExcluir.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnExcluir);
                }
            }
        });

        // Carregar os dados da tabela
        ObservableList<TemplateZPL> lista = templateService.getTemplateList();
        lista_template.setItems(lista);
    }

    public ObservableList<String> getListaSkuMock() {
        ObservableList<String> lista = FXCollections.observableArrayList();
        for (int i = 1; i <= 100; i++) {
            lista.add(String.format("SKU-%04d", i));
        }
        return lista;
    }

    public void carregarComboTemplate() {
        ObservableList<String> templates = templateService.getTemplateList()
                .stream()
                .map(TemplateZPL::getNome)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        comboTemplate.setItems(templates);
    }

    public String getConteudoTemplatePorNome(String nome) {
        TemplateZPL template = templateService.getTemplateList()
                .stream()
                .filter(t -> t.getNome().equals(nome))
                .findFirst()
                .orElse(null);

        return (template != null) ? template.getConteudo() : null;
    }

    public void setupQuantidadeField() {
        txtQuantidade.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtQuantidade.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void setupFields() {
        txtQuantidade.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtQuantidade.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        txtWidth.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtWidth.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });

        txtHeight.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtHeight.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });
    }

    @FXML
    public void refreshTemplates() {
        logger.info("Iniciando geração de preview da etiqueta.");

        // 1. Coleta dos valores digitados / selecionados pelo usuário
        String template = comboTemplate.getValue();
        String sku = comboSku.getValue();
        String qtdTxt = txtQuantidade.getText();
        String widthTxt = txtWidth.getText();
        String heightTxt = txtHeight.getText();
        String unidade = comboUnidade.getValue();

        // 2. Validação dos campos obrigatórios
        List<String> faltando = new ArrayList<>();

        if (template == null || template.isBlank()) {
            faltando.add("Template");
        }
        if (sku == null || sku.isBlank()) {
            faltando.add("SKU");
        }
        if (qtdTxt == null || qtdTxt.isBlank()) {
            faltando.add("Quantidade");
        }
        if (widthTxt == null || widthTxt.isBlank()) {
            faltando.add("Largura");
        }
        if (heightTxt == null || heightTxt.isBlank()) {
            faltando.add("Altura");
        }
        if (unidade == null || unidade.isBlank()) {
            faltando.add("Unidade de medida");
        }

        if (!faltando.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos obrigatórios");
            alert.setHeaderText("Preencha os campos abaixo antes de continuar:");
            alert.setContentText(String.join(", ", faltando));
            alert.showAndWait();
            logger.warn("Campos obrigatórios não preenchidos: {}", faltando);
            return;
        }

        // 3. Conversão segura
        int quantidade;
        double largura, altura;
        try {
            quantidade = Integer.parseInt(qtdTxt);
            largura = Double.parseDouble(widthTxt.replace(',', '.'));
            altura = Double.parseDouble(heightTxt.replace(',', '.'));
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Valor inválido");
            alert.setHeaderText(null);
            alert.setContentText("Quantidade deve ser numérica inteira e tamanho deve ser numérico.");
            alert.showAndWait();
            logger.error("Erro de conversão numérica.", ex);
            return;
        }

        // 4. Gerar chave do cache
        String cacheKey = gerarCacheKey(template, sku, largura, altura, unidade);
        logger.info("CacheKey gerada: {}", cacheKey);

        try {
            // 5. Montagem do ZPL
            String zplBruto = getConteudoTemplatePorNome(template);
            String zplFinal = impressaoZPLService.personalizarZpl(zplBruto, sku);

            // 6. Gerar preview da etiqueta
            Image preview = impressaoZPLService.gerarPreview(zplFinal, largura, altura, unidade, "image/png");

            if (preview != null) {
                imgPreview.setImage(preview);
                lblPreviewPlaceholder.setVisible(false);

                // 7. Salvar no cache (tanto o preview quanto o ZPL final)
                ZplCacheService.salvarPreview(cacheKey, preview);
                ZplCacheService.salvarZpl(cacheKey, zplFinal);

                logger.info("Preview e ZPL armazenados no cache com sucesso.");
            } else {
                logger.error("Falha ao gerar preview da etiqueta.");
                lblPreviewPlaceholder.setText("Erro ao gerar preview.");
                lblPreviewPlaceholder.setVisible(true);
            }

        } catch (Exception e) {
            logger.error("Erro durante a geração do preview da etiqueta.", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Ocorreu um erro ao gerar o preview da etiqueta.");
            alert.showAndWait();
        }
    }

    public String gerarCacheKey(String template, String sku, double largura, double altura, String unidade) {
        String chave = String.format("%s|%s|%.2f|%.2f|%s",
                template.trim(),
                sku.trim(),
                largura,
                altura,
                unidade.trim().toLowerCase());

        logger.info("Gerando cacheKey: {}", chave);
        return chave;
    }

    @FXML
    public void abrirDialogoImpressao() {
        try {
            // Validar se os campos estão preenchidos
            String template = comboTemplate.getValue();
            String sku = comboSku.getValue();
            String qtdTxt = txtQuantidade.getText();
            String widthTxt = txtWidth.getText();
            String heightTxt = txtHeight.getText();
            String unidade = comboUnidade.getValue();

            if (template == null || sku == null || qtdTxt.isBlank()
                    || widthTxt.isBlank() || heightTxt.isBlank() || unidade == null) {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atenção");
                alert.setHeaderText("Campos obrigatórios não preenchidos.");
                alert.setContentText("Preencha Template, SKU, Quantidade e Tamanho.");
                alert.showAndWait();
                return;
            }

            // Conversão segura
            int quantidade = Integer.parseInt(qtdTxt);
            double largura = Double.parseDouble(widthTxt.replace(",", "."));
            double altura = Double.parseDouble(heightTxt.replace(",", "."));

            String cacheKey = gerarCacheKey(template, sku, largura, altura, unidade);

            String zpl = ZplCacheService.getZpl(cacheKey);
            Image preview = ZplCacheService.getPreview(cacheKey);

            if (zpl == null || preview == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Gere o preview antes de imprimir.");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/automacaowebia/print-dialog.fxml"));
            Parent root = loader.load();

            PrintDialogController controller = loader.getController();
            controller.initData(template, sku, largura, altura, unidade, quantidade, preview, cacheKey);

            Stage stage = new Stage();
            stage.setTitle("Impressão de Etiqueta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // 🔒 Bloqueia tela principal
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            logger.error("Erro ao abrir o diálogo de impressão", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao abrir o diálogo de impressão.");
            alert.showAndWait();
        }
    }

    @FXML
    public void onMinimize() {
        Stage stage = (Stage) billing_btn.getScene().getWindow();
        stage.setIconified(true); // Minimiza a janela
    }

    @FXML
    public void onMaximize() {
        Stage stage = (Stage) billing_btn.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized()); // Alterna entre maximizado e restaurado
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Exports all modules to other modules
        Modules.exportAllToAll();

        setupQuantidadeField();
        setUsername();
        activateDashboard();
        setupFields();
        comboUnidade.getSelectionModel().select("cm");

        carregarComboTemplate();

        comboSku.setItems(getListaSkuMock());

//      DASHBOARD PANE
        showDashboardData();

//      CUSTOMER PANE
        showCustomerData();

//      SALES PANE
        showSalesData();

//      Purchase Pane
        showPurchaseData();

//      Lista de tempates
        carregarListaTemplate();
    }
}
