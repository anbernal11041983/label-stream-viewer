package br.com.automacaowebia.controller;

import br.com.automacaowebia.model.Printer;
import br.com.automacaowebia.service.PrinterService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrinterController {

    private static final Logger logger = LogManager.getLogger(PrinterController.class);

    // ====== FXML - campos de formulário
    @FXML private TextField printer_nome;
    @FXML private TextField printer_ip;
    @FXML private TextField printer_porta;
    @FXML private TextField printer_modelo;

    // ====== FXML - tabela
    @FXML private TableView<Printer> lista_printer;
    @FXML private TableColumn<Printer, String> col_printer_nome;
    @FXML private TableColumn<Printer, String> col_printer_ip;
    @FXML private TableColumn<Printer, Integer> col_printer_porta;
    @FXML private TableColumn<Printer, String> col_printer_modelo;
    @FXML private TableColumn<Printer, Void> col_printer_acao;

    private final PrinterService printerService = new PrinterService();
    private Printer selecionado;

    @FXML
    public void initialize() {
        logger.info("Inicializando PrinterController.");

        col_printer_nome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        col_printer_ip.setCellValueFactory(new PropertyValueFactory<>("ip"));
        col_printer_porta.setCellValueFactory(new PropertyValueFactory<>("porta"));
        col_printer_modelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));

        configurarColunaAcoes();
        carregarLista();

        lista_printer.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selecionado = newVal;
                preencherFormulario(selecionado);
            }
        });
    }

    private void carregarLista() {
        ObservableList<Printer> lista = printerService.listarTodos();
        lista_printer.setItems(lista);
    }

    private void preencherFormulario(Printer p) {
        printer_nome.setText(p.getNome());
        printer_ip.setText(p.getIp());
        printer_porta.setText(String.valueOf(p.getPorta()));
        printer_modelo.setText(p.getModelo());
    }

    @FXML
    private void salvarPrinter(ActionEvent e) {
        try {
            String nome = printer_nome.getText().trim();
            String ip = printer_ip.getText().trim();
            String modelo = printer_modelo.getText().trim();
            int porta = Integer.parseInt(printer_porta.getText().trim());

            if (nome.isEmpty() || ip.isEmpty()) {
                alert("Nome e IP são obrigatórios.");
                return;
            }

            if (porta <= 0) {
                alert("Porta inválida.");
                return;
            }

            Printer p = (selecionado == null) ? new Printer() : selecionado;

            p.setNome(nome);
            p.setIp(ip);
            p.setPorta(porta);
            p.setModelo(modelo);

            printerService.salvar(p);
            logger.info("Impressora salva: {}", p.getNome());

            limparCampos(null);
            carregarLista();

        } catch (NumberFormatException ex) {
            alert("Porta deve ser um número válido.");
        }
    }

    @FXML
    private void limparCampos(ActionEvent e) {
        printer_nome.clear();
        printer_ip.clear();
        printer_porta.clear();
        printer_modelo.clear();
        selecionado = null;
        lista_printer.getSelectionModel().clearSelection();
    }

    private void configurarColunaAcoes() {
        Callback<TableColumn<Printer, Void>, TableCell<Printer, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnExcluir = new Button();

            {
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("TRASH");
                icon.setSize("16");
                icon.setFill(javafx.scene.paint.Color.WHITE);
                btnExcluir.setGraphic(icon);
                btnExcluir.getStyleClass().add("delete");
                btnExcluir.setTooltip(new Tooltip("Remover"));

                btnExcluir.setOnAction(event -> {
                    Printer printer = getTableView().getItems().get(getIndex());
                    removerPrinter(printer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnExcluir);
            }
        };

        col_printer_acao.setCellFactory(cellFactory);
    }

    private void removerPrinter(Printer printer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Deseja remover esta impressora?");
        confirm.setContentText("Impressora: " + printer.getNome());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                printerService.remover(printer);
                carregarLista();
                limparCampos(null);
            }
        });
    }

    private void alert(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
