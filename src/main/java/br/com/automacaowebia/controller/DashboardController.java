package br.com.automacaowebia.controller;

import br.com.automacaowebia.config.AppInfo;
import br.com.automacaowebia.model.*;
import br.com.automacaowebia.service.HistoricoImpressaoService;
import br.com.automacaowebia.service.ImpressaoZPLService;
import br.com.automacaowebia.service.TemplateZPLService;
import br.com.automacaowebia.service.ZplCacheService;
import br.com.automacaowebia.service.PrinterService;
import br.com.automacaowebia.model.Printer;
import br.com.automacaowebia.util.PrintExecutor;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.burningwave.core.assembler.StaticComponentContainer.Modules;

public class DashboardController implements Initializable {

    @FXML
    private Button template_btn;
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
    private Label user;
    @FXML
    private TextField template_nome;
    @FXML
    private TableView<TemplateZPL> lista_template;
    @FXML
    private ComboBox<String> comboTemplate;
    @FXML
    private ComboBox<String> comboSku;
    @FXML
    private ImageView imgPreview;
    @FXML
    private Label lblPreviewPlaceholder;
    @FXML
    private TableColumn<TemplateZPL, String> col_template_nome;
    @FXML
    private TableColumn<TemplateZPL, String> col_template_tipo;
    @FXML
    private TableColumn<TemplateZPL, String> col_template_criado;
    @FXML
    private TableColumn<TemplateZPL, Void> col_template_acao;
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
    @FXML
    private Label lblVersao;
    @FXML
    private Label total_impressao_dia_dash;
    @FXML
    private Label total_impressao_mes_dash;
    @FXML
    private Label total_impressao_ano_dash;
    @FXML
    private Label total_jobs;
    @FXML
    private Label inv_num;
    @FXML
    private Label total_modelo_dash;
    @FXML
    private TextField printer_nome;
    @FXML
    private TextField printer_ip;
    @FXML
    private TextField printer_porta;
    @FXML
    private TextField printer_modelo;
    @FXML
    private TableView<Printer> lista_printer;
    @FXML
    private TableColumn<Printer, String> col_printer_nome;
    @FXML
    private TableColumn<Printer, String> col_printer_ip;
    @FXML
    private TableColumn<Printer, Integer> col_printer_porta;
    @FXML
    private TableColumn<Printer, String> col_printer_modelo;
    @FXML
    private TableColumn<Printer, Void> col_printer_acao;
    @FXML
    private Button printers_btn;
    @FXML
    private AnchorPane printer_pane;

    private double x;
    private double y;
    private final TemplateZPLService templateService = new TemplateZPLService();
    private final ImpressaoZPLService impressaoZPLService = new ImpressaoZPLService();
    private final HistoricoImpressaoService historicoImpressaoService = new HistoricoImpressaoService();
    private String conteudoTemplate; // Para guardar o conteúdo carregado
    private final PrinterService printerService = new PrinterService(); // >>> NOVO
    private Printer selecionadoPrinter;

    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    public void onExit() {
        PrintExecutor.POOL.shutdown();
        System.exit(0);
    }

    public void carredarDadosDash() {

        DashResumo resumo = historicoImpressaoService.getResumoDashboard();

        total_jobs.setText(String.valueOf(resumo.getTotalJobs()));
        total_impressao_dia_dash.setText(String.valueOf(resumo.getTotalDia()));
        total_impressao_mes_dash.setText(String.valueOf(resumo.getTotalMes()));
        total_impressao_ano_dash.setText(String.valueOf(resumo.getTotalAno()));
        total_modelo_dash.setText(String.valueOf(resumo.getTotalTemplates()));

    }

    public void activateAnchorPane() {

        String corOn = "-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7), rgba(255,106,239,0.7))";
        String corOff = "-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2), rgba(255,106,239,0.2))";

        dashboard_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(true);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(false);
            printer_pane.setVisible(false);
            dashboard_btn.setStyle(corOn);
            template_btn.setStyle(corOff);
            impressao_btn.setStyle(corOff);
            printers_btn.setStyle(corOff);
            carredarDadosDash();
        });
        template_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(true);
            impressao_zpl.setVisible(false);
            printer_pane.setVisible(false);
            dashboard_btn.setStyle(corOff);
            template_btn.setStyle(corOn);
            impressao_btn.setStyle(corOff);
            printers_btn.setStyle(corOff);
        });
        impressao_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(true);
            printer_pane.setVisible(false);
            dashboard_btn.setStyle(corOff);
            template_btn.setStyle(corOff);
            impressao_btn.setStyle(corOn);
            printers_btn.setStyle(corOff);
            carregarComboTemplate();
        });
        printers_btn.setOnMouseClicked(mouseEvent -> {
            dasboard_pane.setVisible(false);
            template_pane.setVisible(false);
            impressao_zpl.setVisible(false);
            printer_pane.setVisible(true);
            dashboard_btn.setStyle(corOff);
            template_btn.setStyle(corOff);
            impressao_btn.setStyle(corOff);
            printers_btn.setStyle(corOn);
        });

    }

    public void setUsername() {
        user.setText("Admin");
    }

    public void activateDashboard() {
        dasboard_pane.setVisible(true);
        template_pane.setVisible(false);
        impressao_zpl.setVisible(false);
    }

    public void signOut() {
        PrintExecutor.POOL.shutdown();
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
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText(err.getMessage());
            alert.showAndWait();
        }

    }

    public void loadTemplate() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar Template TXT");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos TXT", "*.txt"));

        File arq = fc.showOpenDialog(new Stage());
        if (arq == null) {
            return;
        }

        try {
            conteudoTemplate = Files.readString(arq.toPath(), ISO_8859_1);

            inv_num.setText("Arquivo carregado: " + arq.getName());
            logger.info("Template '{}' carregado com sucesso.", arq.getAbsolutePath());

        } catch (IOException e) {
            logger.error("Erro ao carregar arquivo '{}': {}", arq.getName(), e.getMessage(), e);
            new Alert(Alert.AlertType.ERROR, "Não foi possível ler o arquivo.").showAndWait();
        }
    }

    public void salvarTemplate() {
        String nome = template_nome.getText();

        logger.info("Tentando salvar template com nome '{}'.", nome);

        if (nome == null || nome.isBlank()) {
            logger.warn("Nome do template não preenchido.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("O nome do template é obrigatório.");
            alert.showAndWait();
            return;
        }

        if (conteudoTemplate == null || conteudoTemplate.isBlank()) {
            logger.warn("Nenhum conteúdo de template carregado para salvar.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
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
            limparCampoTemplate();
            carregarListaTemplate();
        } else {
            logger.error("Erro ao salvar template '{}'.", nome);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao salvar template.");
            alert.showAndWait();
            limparCampoTemplate();
        }
    }

    public void limparCampoTemplate() {
        template_nome.setText("");
        inv_num.setText("");
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
                btnExcluir.getStyleClass().add("delete");

                // Ícone no botão (usando FontAwesomeIconView)
                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName("TRASH");
                icon.setSize("16");
                icon.setFill(javafx.scene.paint.Color.WHITE);
                btnExcluir.setTooltip(new Tooltip("Remover"));
                btnExcluir.setGraphic(icon);
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

    private void initPrinterCrud() {
        // colunas
        col_printer_nome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        col_printer_ip.setCellValueFactory(new PropertyValueFactory<>("ip"));
        col_printer_porta.setCellValueFactory(new PropertyValueFactory<>("porta"));
        col_printer_modelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));

        configurarColunaAcoesPrinter();
        carregarListaPrinter();

        // clique na linha ⇒ preencher formulário
        lista_printer.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selecionadoPrinter = newVal;
                printer_nome.setText(newVal.getNome());
                printer_ip.setText(newVal.getIp());
                printer_porta.setText(String.valueOf(newVal.getPorta()));
                printer_modelo.setText(newVal.getModelo());
            }
        });
    }

    @FXML
    private void salvarPrinter(ActionEvent e) {
        try {
            String nome = printer_nome.getText().trim();
            String ip = printer_ip.getText().trim();
            String modelo = printer_modelo.getText().trim();
            int porta = Integer.parseInt(printer_porta.getText().trim());

            if (nome.isEmpty() || ip.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Alerta");
                alert.setHeaderText(null);
                alert.setContentText("Nome e IP são obrigatórios.");
                alert.showAndWait();
                return;
            }
            if (porta <= 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Alerta");
                alert.setHeaderText(null);
                alert.setContentText("Porta inválida.");
                alert.showAndWait();
                return;
            }

            Printer p = (selecionadoPrinter == null) ? new Printer() : selecionadoPrinter;
            p.setNome(nome);
            p.setIp(ip);
            p.setPorta(porta);
            p.setModelo(modelo);

            printerService.salvar(p);
            limparCamposPrinter(null);
            carregarListaPrinter();

        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alerta");
            alert.setHeaderText(null);
            alert.setContentText("Porta deve ser numérica.");
            alert.showAndWait();
        }
    }

    private void carregarListaPrinter() {
        lista_printer.setItems(printerService.listarTodos());
    }

    @FXML
    private void limparCamposPrinter(ActionEvent e) {
        printer_nome.clear();
        printer_ip.clear();
        printer_porta.clear();
        printer_modelo.clear();
        selecionadoPrinter = null;
        lista_printer.getSelectionModel().clearSelection();
    }

    private void configurarColunaAcoesPrinter() {
        col_printer_acao.setCellFactory(param -> new TableCell<>() {
            private final Button btn = criarBotaoTrash();

            {
                btn.setOnAction(ev -> {
                    Printer pr = getTableView().getItems().get(getIndex());
                    removerPrinter(pr);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private Button criarBotaoTrash() {
        Button b = new Button();
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName("TRASH");
        icon.setFill(javafx.scene.paint.Color.WHITE);
        icon.setSize("16");
        b.setGraphic(icon);
        b.getStyleClass().add("delete");
        b.setTooltip(new Tooltip("Remover"));
        return b;
    }

    private void removerPrinter(Printer p) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Remover impressora '" + p.getNome() + "'?");
        c.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                printerService.remover(p);
                carregarListaPrinter();
                limparCamposPrinter(null);
            }
        });
    }

    @FXML
    public void onMinimize() {
        Stage stage = (Stage) template_btn.getScene().getWindow();
        stage.setIconified(true); // Minimiza a janela
    }

    @FXML
    public void onMaximize() {
        Stage stage = (Stage) template_btn.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized()); // Alterna entre maximizado e restaurado
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Modules.exportAllToAll();
        lblVersao.setText("Versão: " + AppInfo.getVersion());
        setupQuantidadeField();
        setUsername();
        activateDashboard();
        setupFields();
        comboUnidade.getSelectionModel().select("inches");
        carregarComboTemplate();
        comboSku.setItems(getListaSkuMock());
        carregarListaTemplate();
        carredarDadosDash();
        initPrinterCrud();
    }
}
