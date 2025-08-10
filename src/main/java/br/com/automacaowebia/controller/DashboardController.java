package br.com.automacaowebia.controller;

import br.com.automacaowebia.config.AppInfo;
import br.com.automacaowebia.config.PermissaoModulo;
import br.com.automacaowebia.dto.ProdutoLabelData;
import br.com.automacaowebia.model.*;
import br.com.automacaowebia.service.HistoricoImpressaoService;
import br.com.automacaowebia.service.ImpressaoZPLService;
import br.com.automacaowebia.service.TemplateZPLService;
import br.com.automacaowebia.service.PrinterService;
import br.com.automacaowebia.model.Printer;
import br.com.automacaowebia.parser.ZplParser;
import br.com.automacaowebia.service.BlueprintService;
import br.com.automacaowebia.service.DispositivoService;
import br.com.automacaowebia.service.ProdutoLabelDataService;
import br.com.automacaowebia.session.Session;
import br.com.automacaowebia.util.PrintExecutor;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
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
    private Button monitor_btn;
    @FXML
    private Button dashboard_btn;
    @FXML
    private AnchorPane impressao_zpl;
    @FXML
    private AnchorPane dasboard_pane;
    @FXML
    private AnchorPane monitor_pane;
    @FXML
    private Label user;
    @FXML
    private TextField template_nome, txtTemplateImpressora;
    @FXML
    private TableView<TemplateZPL> lista_template;
    @FXML
    private ComboBox<String> comboTemplate;
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

    @FXML
    private TextArea txtLog;
    @FXML
    private TextField txtTemplate;
    @FXML
    private ComboBox<Printer> cmbImpressora;
    private FontAwesomeIconView icoStatus;
    @FXML
    private ProgressBar barStatus;

    @FXML
    private Spinner<Integer> qtdSpinner;
    @FXML
    private Spinner<Integer> tmpSpinner;

    @FXML
    private Button dispositivos_btn;
    @FXML
    private AnchorPane dispositivo_pane;
    @FXML
    private TableView<Dispositivo> lista_dispositivo;
    @FXML
    private TableColumn<Dispositivo, String> col_disp_mac;
    @FXML
    private TableColumn<Dispositivo, String> col_disp_status;
    @FXML
    private TableColumn<Dispositivo, Void> col_disp_acao;
    @FXML
    private TextField disp_mac;
    @FXML
    private ComboBox<String> disp_status;
    @FXML
    private TableView<VarItem> tblVars;
    @FXML
    private TableColumn<VarItem, String> colKey;
    @FXML
    private TableColumn<VarItem, String> colValue;

    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;

    @FXML
    private Button btnPrint;

    @FXML
    private TextField txtKey;
    @FXML
    private TextField txtValue;

    @FXML
    private TextArea txtLogPrint;

    @FXML
    private Button blueprint_btn;
    @FXML
    private AnchorPane blueprint_pane;

    @FXML
    private TableView<TemplateBlueprint> tblBlueprint;
    @FXML
    private TableColumn<TemplateBlueprint, String> col_bp_marcador;
    @FXML
    private TableColumn<TemplateBlueprint, String> col_bp_campo;
    @FXML
    private TableColumn<TemplateBlueprint, Integer> col_bp_ordem;
    @FXML
    private TableColumn<TemplateBlueprint, Boolean> col_bp_ativo;
    @FXML
    private TableColumn<TemplateBlueprint, Void> col_bp_acao;

    @FXML
    private ComboBox<String> cb_bp_template;

    /* ─── impressao_zpl ──────────────────────────────────────────────── */
    @FXML
    private ComboBox<Printer> comboPrinter;

    @FXML
    private Label lblNomeProduto;
    @FXML
    private Label lblNomeReduzido;
    @FXML
    private Label lblSku;
    @FXML
    private Label lblDataProducao;
    @FXML
    private Label lblDataValidade;

    @FXML
    private TextField txtIntervalo;      // segundos entre impressões
    @FXML
    private ProgressBar barPrint;

    private double x;
    private double y;
    private final TemplateZPLService templateService = new TemplateZPLService();
    private final ImpressaoZPLService impressaoZPLService = new ImpressaoZPLService();
    private final HistoricoImpressaoService historicoImpressaoService = new HistoricoImpressaoService();
    private final ProdutoLabelDataService produtoLabelDataService = new ProdutoLabelDataService();
    private String conteudoTemplate; // Para guardar o conteúdo carregado
    private final PrinterService printerService = new PrinterService(); // >>> NOVO
    private Printer selecionadoPrinter;
    private final BooleanProperty botaoDesabilitado = new SimpleBooleanProperty(false);
    private final Map<Button, PermissaoModulo> modulos = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(DashboardController.class);
    private final DispositivoService dispositivoService = new DispositivoService();
    private Dispositivo selecionadoDispositivo;
    private final BlueprintService blueprintService = new BlueprintService();
    private TemplateBlueprint selecionadoBlueprint;
    private TemplateZPL selecionadoTemplate;

    private ObservableList<VarItem> varsData = FXCollections.observableArrayList();
    @FXML
    private Button btnLoadTemplate;
    @FXML
    private Button btn_salvar_template;
    @FXML
    private Button bill_clear;
    @FXML
    private Button btn_salvar_printer;
    @FXML
    private Button btn_clear_printer;
    @FXML
    private Button btn_test_printer;

    @FXML
    private Button btn_salvar_disp;
    @FXML
    private Button btn_clear_disp;

    @FXML
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

    @FXML
    public void activateAnchorPane() {

        configurarPermissoes();

        for (Map.Entry<Button, PermissaoModulo> entry : modulos.entrySet()) {
            Button btn = entry.getKey();
            PermissaoModulo modulo = entry.getValue();
            btn.setOnMouseClicked(event -> showPaneComPermissao(modulo));
        }
    }

    private void showPaneComPermissao(PermissaoModulo modulo) {
        // Esconde todos
        limparUI();
        dasboard_pane.setVisible(false);
        template_pane.setVisible(false);
        impressao_zpl.setVisible(false);
        printer_pane.setVisible(false);
        monitor_pane.setVisible(false);
        dispositivo_pane.setVisible(false);
        blueprint_pane.setVisible(false);

        // Estilo
        String corOn = "-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.7), rgba(255,106,239,0.7))";
        String corOff = "-fx-background-color:linear-gradient(to bottom right , rgba(121,172,255,0.2), rgba(255,106,239,0.2))";
        dashboard_btn.setStyle(corOff);
        template_btn.setStyle(corOff);
        impressao_btn.setStyle(corOff);
        printers_btn.setStyle(corOff);
        monitor_btn.setStyle(corOff);
        dispositivos_btn.setStyle(corOff);
        blueprint_btn.setStyle(corOff);

        // Checa permissão
        User usuario = Session.getInstance().getUser();
        String perfil = usuario != null ? usuario.getPerfil() : "";

        if (modulo.perfisPermitidos.contains(perfil)) {
            if (modulo.pane != null) {
                modulo.pane.setVisible(true);
            }
            if (modulo.btn != null) {
                modulo.btn.setStyle(corOn);
            }
            // Se quiser lógica extra, coloque aqui
            if (modulo.btn == impressao_btn) {
                carregarComboTemplate();
            }
            if (modulo.btn == dashboard_btn) {
                carredarDadosDash();
            } else if (modulo.btn == monitor_btn) {
                carregaImpressoraMonitor();
            } else if (modulo.btn == blueprint_btn) {
                carregarComboBlueprint();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Você não tem permissão para acessar este módulo.").showAndWait();
        }
    }

    public void setUsername() {
        var usuario = Session.getInstance().getUser();
        if (usuario != null) {
            user.setText(usuario.getNome() + " (" + usuario.getPerfil() + ")");
        } else {
            user.setText("Desconhecido");
        }
    }

    @FXML
    public void activateDashboard() {
        dasboard_pane.setVisible(true);
        template_pane.setVisible(false);
        impressao_zpl.setVisible(false);
        printer_pane.setVisible(false);
        monitor_pane.setVisible(false);
        dispositivo_pane.setVisible(false);
        blueprint_pane.setVisible(false);
    }

    @FXML
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

    @FXML
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

    @FXML
    public void salvarTemplate() {

        String nome = template_nome.getText();
        String templateImpressora = normalizarNomeArquivo(txtTemplateImpressora.getText());

        logger.info("Tentando salvar/atualizar template '{}'.", nome);

        if (nome == null || nome.isBlank()) {
            showWarn("O nome do template é obrigatório.");
            return;
        }
        if (templateImpressora.isBlank()) {
            showWarn("O nome do template da impressora é obrigatório.");
            return;
        }

        // Se o usuário não carregou novo .txt mas está em modo edição, reaproveite o conteúdo já salvo
        if ((conteudoTemplate == null || conteudoTemplate.isBlank()) && selecionadoTemplate != null) {
            conteudoTemplate = selecionadoTemplate.getConteudo();
        }
        if (conteudoTemplate == null || conteudoTemplate.isBlank()) {
            showWarn("Nenhum arquivo foi carregado.");
            return;
        }

        boolean sucesso;

        if (selecionadoTemplate == null) {     // -------- INSERT ---------
            TemplateZPL novo = new TemplateZPL(nome, "TXT", conteudoTemplate, templateImpressora);
            Long idGerado = templateService.insertTemplate(novo);

            sucesso = (idGerado != null);
            if (sucesso) {
                logger.info("Template '{}' inserido com id {}.", nome, idGerado);

                // grava dados de produto
                ProdutoLabelData dados = ZplParser.parseFromString(conteudoTemplate);
                templateService.salvarDadosProduto(idGerado, dados);
            }

        } else {
            TemplateZPL tpl = selecionadoTemplate;
            tpl.setNome(nome);
            tpl.setTemplateImpressora(templateImpressora);
            tpl.setConteudo(conteudoTemplate);

            sucesso = templateService.updateTemplate(tpl);
            if (sucesso) {
                logger.info("Template '{}' (id={}) atualizado.", nome, tpl.getId());
            }
        }

        /* ---------- feedback UI ---------- */
        if (sucesso) {
            limparCampoTemplate();
            selecionarNenhumTemplate();          // limpa seleção na tabela
            carregarListaTemplate();
            showInfo("Template salvo com sucesso!");
        } else {
            logger.error("Falha ao gravar template '{}'.", nome);
            showError("Erro ao salvar template.");
        }
    }

    private static String normalizarNomeArquivo(String txt) {
        if (txt == null) {
            return "";
        }
        return txt.trim()
                .replaceAll("(?i)\\.ncfm$", "") // remove se já existir no fim
                .toUpperCase() + ".ncfm";        // acrescenta minúsculo
    }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    private void selecionarNenhumTemplate() {
        lista_template.getSelectionModel().clearSelection();
        selecionadoTemplate = null;
    }

    @FXML
    public void limparCampoTemplate() {
        template_nome.setText("");
        txtTemplateImpressora.setText("");
        inv_num.setText("");
        lista_template.getSelectionModel().clearSelection();
        selecionadoTemplate = null;
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

    public void carregarComboTemplate() {
        ObservableList<String> templates = templateService.getTemplateList()
                .stream()
                .map(TemplateZPL::getNome)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        comboTemplate.setItems(templates);

        // limpa seleção anterior e limpa os campos
        comboTemplate.getSelectionModel().clearSelection();
        preencherInfoProduto(null);
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
            carregaImpressoraMonitor();

        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alerta");
            alert.setHeaderText(null);
            alert.setContentText("Porta deve ser numérica.");
            alert.showAndWait();
        }
    }

    @FXML
    private void testPrinter(ActionEvent e) {

        /* --------- 1. validação de campos --------- */
        String nome = printer_nome.getText().trim();
        String ip = printer_ip.getText().trim();
        String modelo = printer_modelo.getText().trim();
        String portaTx = printer_porta.getText().trim();

        if (nome.isEmpty() || ip.isEmpty()) {
            showError("Nome e IP são obrigatórios.");
            return;
        }

        int porta;
        try {
            porta = Integer.parseInt(portaTx);
            if (porta <= 0) {
                showError("Porta inválida (deve ser > 0).");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Porta deve ser numérica.");
            return;
        }

        /* --------- 2. monta objeto Printer --------- */
        Printer p = (selecionadoPrinter == null) ? new Printer() : selecionadoPrinter;
        p.setNome(nome);
        p.setIp(ip);
        p.setPorta(porta);
        p.setModelo(modelo);

        /* --------- 3. task assíncrona --------- */
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                logger.info("Iniciando teste na impressora {}:{}", p.getIp(), p.getPorta());
                printerService.teste(p); // pode lançar IOException ou ConnectException
                return null;
            }
        };

        task.setOnSucceeded(ev -> {
            logger.info("Teste concluído com sucesso na impressora {}:{}", p.getIp(), p.getPorta());
            showInfo("Teste de impressão enviado com sucesso!");
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            if (ex instanceof ConnectException) {
                logger.error("Falha de conexão em {}:{} – {}", p.getIp(), p.getPorta(), ex.getMessage());
                showError("Não foi possível conectar à impressora em "
                        + p.getIp() + ":" + p.getPorta());
            } else {
                logger.error("Erro de I/O durante teste na impressora {}:{}",
                        p.getIp(), p.getPorta(), ex);
                showError("Erro de comunicação: " + ex.getMessage());
            }
        });

        /* --------- 4. dispara em thread tradicional (compatível com todos JDKs) --------- */
        Thread th = new Thread(task);
        th.setDaemon(true); // não bloqueia fechamento do app
        th.start();
    }

    @FXML
    public void imprimir() {
        String template = normalizarNomeArquivo(txtTemplate.getText());
        Printer prSelecionada = cmbImpressora.getSelectionModel().getSelectedItem();
        Integer qtdImpressao = qtdSpinner.getValue();
        Integer espacamento = tmpSpinner.getValue();

        Map<String, String> mapVars = varsData.stream()
                .collect(Collectors.toMap(
                        VarItem::getKey,
                        VarItem::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new));

        if (template.isEmpty() || prSelecionada == null || qtdImpressao == null || qtdImpressao <= 0) {
            appendLog("⚠ Por favor, preencha todos os campos obrigatórios e informe uma quantidade válida (> 0).");
            return;
        }

        if (mapVars.isEmpty()) {                  // ⚠ nenhuma variável definida
            appendLog("⚠ Adicione ao menos uma variável antes de imprimir.");
            return;
        }

        // Busca atualizada pelo ID, se quiser garantir info atualizada
        Printer pr = printerService.buscarPorId(prSelecionada.getId());
        if (pr == null) {
            appendLog("⚠ Impressora não encontrada na base.");
            return;
        }

        barStatus.setVisible(true);

        Task<Void> job = new Task<>() {
            @Override
            protected Void call() throws Exception {

                printerService.imprimir(pr, template, qtdImpressao, line -> appendLog(line), espacamento, mapVars);
                return null;
            }
        };

        job.setOnSucceeded(e -> {
            barStatus.setVisible(false);
            appendLog("✔ Impressão concluída com sucesso.");
        });

        job.setOnFailed(e -> {
            barStatus.setVisible(false);
            Throwable ex = job.getException();
            appendLog("✖ Falha: " + ex.getMessage());
        });

        new Thread(job).start();
    }

    @FXML
    public void onStart() {
        String template = normalizarNomeArquivo(txtTemplate.getText());
        Printer prSelecionada = cmbImpressora.getSelectionModel().getSelectedItem();
        Integer qtdImpressao = qtdSpinner.getValue();         // pode ser null/0 → contínuo
        Integer espacamento = tmpSpinner.getValue();

        Map<String, String> mapVars = varsData.stream()
                .collect(Collectors.toMap(
                        VarItem::getKey,
                        VarItem::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new));

        // -------- validações mínimas --------
        if (template.isEmpty() || prSelecionada == null) {
            appendLog("⚠ Selecione template e impressora.");
            return;
        }
        if (mapVars.isEmpty()) {
            appendLog("⚠ Adicione ao menos uma variável antes de imprimir.");
            return;
        }
        if (espacamento == null || espacamento < 1) {
            appendLog("⚠ Informe um espaçamento (ms) válido.");
            return;
        }

        // quantidade é OPCIONAL: <=0 ou null ⇒ contínuo
        Integer qty = (qtdImpressao != null && qtdImpressao > 0) ? qtdImpressao : null;

        // impressora “fresh” do banco
        Printer pr = printerService.buscarPorId(prSelecionada.getId());
        if (pr == null) {
            appendLog("⚠ Impressora não encontrada na base.");
            return;
        }

        try {
            printerService.iniciarImpressao(
                    pr,
                    template,
                    qty,
                    espacamento,
                    mapVars,
                    line -> appendLog(line)
            );
        } catch (IllegalStateException dup) {
            appendLog("ℹ Já existe um job em execução para " + pr.getIp() + ":" + pr.getPorta());
            return;
        }

        barStatus.setVisible(true);
        if (btnStart != null) {
            btnStart.setDisable(true);
        }
        if (btnStop != null) {
            btnStop.setDisable(false);
        }
    }

    @FXML
    public void onStop() {
        Printer prSelecionada = cmbImpressora.getSelectionModel().getSelectedItem();
        if (prSelecionada == null) {
            appendLog("ℹ Selecione a impressora para parar.");
            return;
        }
        Printer pr = printerService.buscarPorId(prSelecionada.getId());
        if (pr == null) {
            appendLog("⚠ Impressora não encontrada na base.");
            return;
        }

        printerService.pararImpressao(pr, line -> appendLog(line));

        barStatus.setVisible(false);
        if (btnStart != null) {
            btnStart.setDisable(false);
        }
        if (btnStop != null) {
            btnStop.setDisable(true);
        }
    }

    public BooleanProperty botaoDesabilitadoProperty() {
        return botaoDesabilitado;
    }

    public void atualizarEstadoBotao() {
        boolean disable
                = (icoStatus.getFill().equals(Color.RED))
                || txtTemplate.getText().isEmpty()
                || cmbImpressora.getSelectionModel().getSelectedItem() == null;
        botaoDesabilitado.set(disable);
    }

    public void appendLog(String line) {
        Platform.runLater(() -> {
            String msg = LocalTime.now() + " " + line + "\n";

            if (txtLog != null) {              // aba Monitor
                txtLog.appendText(msg);
                txtLog.setScrollTop(Double.MAX_VALUE);
            }
            if (txtLogPrint != null) {         // aba Impressão
                txtLogPrint.appendText(msg);
                txtLogPrint.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    public void fechar(ActionEvent e) {
        //root.getScene().getWindow().hide();
    }

    public void carregaImpressoraMonitor() {
        cmbImpressora.getItems().clear();
        for (Printer p : printerService.listarTodos()) {
            cmbImpressora.getItems().add(p);
        }

        if (!cmbImpressora.getItems().isEmpty()) {
            cmbImpressora.getSelectionModel().selectFirst();
        }

        // Configura a forma de exibir
        cmbImpressora.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Printer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%s)", item.getNome(), item.getIp()));
                }
            }
        });

        cmbImpressora.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Printer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%s)", item.getNome(), item.getIp()));
                }
            }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Informação");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
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

    private void configurarSpinnerQtd() {
        // Define ValueFactory
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 0, 10);
        qtdSpinner.setValueFactory(valueFactory);

        // Permite digitar manualmente
        qtdSpinner.setEditable(true);

        // Aplica TextFormatter para filtrar somente dígitos
        TextFormatter<Integer> intFormatter = new TextFormatter<>(
                new javafx.util.converter.IntegerStringConverter(),
                1,
                change -> change.getControlNewText().matches("\\d*") ? change : null
        );
        qtdSpinner.getEditor().setTextFormatter(intFormatter);
    }

    private void configurarSpinnerTmp() {
        // Define ValueFactory
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000, 1, 1);
        tmpSpinner.setValueFactory(valueFactory);

        // Permite digitar manualmente
        tmpSpinner.setEditable(true);

        // Aplica TextFormatter para filtrar somente dígitos
        TextFormatter<Integer> intFormatter = new TextFormatter<>(
                new javafx.util.converter.IntegerStringConverter(),
                1,
                change -> change.getControlNewText().matches("\\d*") ? change : null
        );
        tmpSpinner.getEditor().setTextFormatter(intFormatter);
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

    public void onMinimize() {
        Stage stage = (Stage) template_btn.getScene().getWindow();
        stage.setIconified(true); // Minimiza a janela
    }

    public void onMaximize() {
        Stage stage = (Stage) template_btn.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized()); // Alterna entre maximizado e restaurado
    }

    @FXML
    private void salvarDispositivo() {
        String mac = disp_mac.getText().trim();
        String status = disp_status.getValue();

        if (mac.isEmpty() || status == null) {
            showError("MAC e Status são obrigatórios.");
            return;
        }
        Dispositivo d = (selecionadoDispositivo == null)
                ? new Dispositivo() : selecionadoDispositivo;
        d.setMacAddress(mac);
        d.setStatus(status);

        try {
            dispositivoService.salvar(d);
            limparCamposDispositivo();
            carregarListaDispositivo();
        } catch (Exception ex) {
            showError("Erro ao salvar dispositivo: " + ex.getMessage());
        }
    }

    @FXML
    private void limparCamposDispositivo() {
        disp_mac.clear();
        disp_status.getSelectionModel().clearSelection();
        selecionadoDispositivo = null;
        lista_dispositivo.getSelectionModel().clearSelection();
    }

    @FXML
    private void remVar() {
        VarItem sel = tblVars.getSelectionModel().getSelectedItem();
        if (sel != null) {
            varsData.remove(sel);
        }
    }

    @FXML
    private void addVar() {
        String k = txtKey.getText().trim();
        String v = txtValue.getText().trim();

        if (k.isEmpty() || v.isEmpty()) {
            showError("Preencha chave e valor antes de adicionar.");
            return;
        }

        // se já existir a chave, só atualiza o valor
        for (VarItem vi : varsData) {
            if (vi.getKey().equalsIgnoreCase(k)) {
                vi.setValue(v);
                tblVars.refresh();
                limparCamposVar();
                return;
            }
        }

        varsData.add(new VarItem(k, v));
        limparCamposVar();
    }

    @FXML
    private void onPrint(ActionEvent e) {

        String templateNomeUI = comboTemplate.getSelectionModel().getSelectedItem();
        Printer prSelecionada = comboPrinter.getSelectionModel().getSelectedItem();

        /* ---------- validações numéricas já existentes ---------- */
        int qtdImpressao, espacamento;
        try {
            qtdImpressao = Integer.parseInt(txtQuantidade.getText().trim());
            espacamento = Integer.parseInt(txtIntervalo.getText().trim());
        } catch (NumberFormatException ex) {
            appendLog("⚠ Quantidade e intervalo precisam ser numéricos.");
            return;
        }
        if (qtdImpressao <= 0) {
            appendLog("⚠ Informe uma quantidade > 0.");
            return;
        }
        if (espacamento < 200) {
            appendLog("⚠ Intervalo mínimo é 200 ms.");
            return;
        }

        if (templateNomeUI == null || templateNomeUI.isBlank() || prSelecionada == null) {
            appendLog("⚠ Selecione template e impressora.");
            return;
        }

        TemplateZPL tpl = templateService.getTemplateByNome(templateNomeUI);
        if (tpl == null) {
            appendLog("⚠ Template não encontrado no banco.");
            return;
        }
        String templateImpressora = tpl.getTemplateImpressora();   // ← ex.: HELLO.ncfm
        if (templateImpressora == null || templateImpressora.isBlank()) {
            appendLog("⚠ Campo template_impressora vazio para esse template.");
            return;
        }

        Map<String, String> mapVars = buildMapVars(tpl.getId());
        if (mapVars.isEmpty()) {
            appendLog("⚠ Nenhum blueprint ativo para este template.");
            return;
        }

        /* ---------- impressora atualizada ---------- */
        Printer pr = printerService.buscarPorId(prSelecionada.getId());
        if (pr == null) {
            appendLog("⚠ Impressora não encontrada na base.");
            return;
        }

        /* ---------- UI ---------- */
        barPrint.setVisible(true);
        botaoDesabilitado.set(true);

        /* ---------- cópia final para o Task ---------- */
        final int delayMs = espacamento;
        final int qtd = qtdImpressao;

        Task<Void> job = new Task<>() {
            @Override
            protected Void call() throws Exception {
                printerService.imprimir(
                        pr,
                        templateImpressora, // ← usa o valor certo!
                        qtd,
                        line -> appendLog(line),
                        delayMs,
                        mapVars);
                return null;
            }
        };

        job.setOnSucceeded(ev -> {
            barPrint.setVisible(false);
            botaoDesabilitado.set(false);   // libera o botão
            appendLog("✔ Impressão concluída com sucesso.");
        });

        job.setOnFailed(ev -> {
            barPrint.setVisible(false);
            botaoDesabilitado.set(false);   // libera mesmo em erro
            appendLog("✖ Falha: " + job.getException().getMessage());
        });

        new Thread(job, "print-job").start();
    }

    private Map<String, String> buildMapVars(long templateId) {

        ProdutoLabelData dados = produtoLabelDataService.findByTemplateId(templateId);
        if (dados == null) {
            appendLog("⚠ Nenhum dado de produto para o template.");
            return Map.of();
        }

        List<TemplateBlueprint> ativos = blueprintService
                .listarPorTemplate(templateId)
                .stream()
                .filter(TemplateBlueprint::isAtivo)
                .sorted(Comparator.comparing(
                        bp -> bp.getOrdem() == null ? 0 : bp.getOrdem()))
                .toList();

        Map<String, String> map = new LinkedHashMap<>();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (TemplateBlueprint bp : ativos) {
            String valor = switch (bp.getCampo()) {
                case "codigo_barras" ->
                    dados.codigoBarras();
                case "url_qr" ->
                    dados.urlQrCode();
                case "sif" ->
                    dados.sif();
                case "sku" ->
                    dados.sku();
                case "data_producao" ->
                    dados.dataProducao() == null ? null
                    : dados.dataProducao().format(df);
                case "data_validade" ->
                    dados.dataValidade() == null ? null
                    : dados.dataValidade().format(df);
                case "desc_completa" ->
                    dados.descCompleta();
                case "desc_reduzida" ->
                    dados.descReduzida();
                case "peso" ->
                    dados.pesoKg() == null
                    ? null : dados.pesoKg()
                    .stripTrailingZeros()
                    .toPlainString();
                default ->
                    null;
            };

            if (valor != null && !valor.isBlank()) {
                map.put(bp.getMarcador(), valor);
            }
        }
        return map;
    }

    private void removerBlueprint(TemplateBlueprint bp) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover mapeamento '" + bp.getMarcador() + "'?");
        c.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                blueprintService.deletar(bp.getId());
            }
        });
    }

    private void limparCamposVar() {
        txtKey.clear();
        txtValue.clear();
        txtKey.requestFocus();
    }

    private void removerDispositivo(Dispositivo d) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover dispositivo '" + d.getMacAddress() + "'?");
        c.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    dispositivoService.remover(d);
                    carregarListaDispositivo();
                    limparCamposDispositivo();
                } catch (Exception ex) {
                    showError("Erro ao remover: " + ex.getMessage());
                }
            }
        });
    }

    private void initBlueprintCrud() {

        // ── colunas simples ──
        col_bp_marcador.setCellValueFactory(new PropertyValueFactory<>("marcador"));
        col_bp_campo.setCellValueFactory(new PropertyValueFactory<>("campo"));
        col_bp_ordem.setCellValueFactory(new PropertyValueFactory<>("ordem"));

        // ── checkbox editável, mas SEM persistir automáticamente ──
        col_bp_ativo.setCellValueFactory(cell -> cell.getValue().ativoProperty());
        col_bp_ativo.setCellFactory(CheckBoxTableCell.forTableColumn(col_bp_ativo));
        col_bp_ativo.setEditable(true);
        tblBlueprint.setEditable(true);

        // ── coluna de ações  (SALVAR + EXCLUIR) ──
        configurarColunaAcoesBlueprint();

        tblBlueprint.getItems().clear();       // carrega depois do template escolhido
    }

    private void configurarColunaAcoesBlueprint() {
        col_bp_acao.setCellFactory(tc -> new TableCell<>() {

            private final Button btnSave = criarBotaoSave();
            private final Button btnDel = criarBotaoTrash();
            private final HBox box = new HBox(6, btnSave, btnDel);

            {
                btnSave.setOnAction(e -> {
                    TemplateBlueprint bp = getTableView().getItems().get(getIndex());

                    boolean ok = blueprintService.atualizar(bp);   // grava TODOS os campos

                    Alert a = new Alert(ok ? Alert.AlertType.INFORMATION
                            : Alert.AlertType.ERROR);

                    a.setHeaderText(null);
                    a.setContentText(ok
                            ? "Mapeamento salvo com sucesso!"
                            : "Não foi possível salvar o mapeamento.");
                    a.showAndWait();
                });

                btnDel.setOnAction(e -> removerBlueprint(
                        getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private Button criarBotaoSave() {
        Button b = new Button();
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName("SAVE");
        icon.setFill(Color.WHITE);
        icon.setSize("16");
        b.setGraphic(icon);
        b.getStyleClass().add("save");
        b.setTooltip(new Tooltip("Salvar alterações"));
        return b;
    }

    private void carregarListaDispositivo() {
        try {
            lista_dispositivo.setItems(dispositivoService.listarTodos());
        } catch (Exception ex) {
            showError("Não foi possível carregar dispositivos.");
        }
    }

    private void initDispositivoCrud() {
        col_disp_mac.setCellValueFactory(new PropertyValueFactory<>("macAddress"));
        col_disp_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        col_disp_acao.setCellFactory(tb -> new TableCell<>() {
            private final Button btn = criarBotaoTrash();

            {
                btn.setOnAction(e -> removerDispositivo(
                        getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        carregarListaDispositivo();

        lista_dispositivo.getSelectionModel().selectedItemProperty()
                .addListener((o, oldVal, newVal) -> {
                    if (newVal != null) {
                        selecionadoDispositivo = newVal;
                        disp_mac.setText(newVal.getMacAddress());
                        disp_status.setValue(newVal.getStatus());
                    }
                });
    }

    private void ocultarBotoesNaoPermitidos() {
        User usuario = Session.getInstance().getUser();
        String perfil = usuario != null ? usuario.getPerfil() : "";

        for (PermissaoModulo modulo : modulos.values()) {
            modulo.btn.setVisible(modulo.perfisPermitidos.contains(perfil));
        }
    }

    private void configurarPermissoes() {
        modulos.put(dashboard_btn, new PermissaoModulo(dasboard_pane, dashboard_btn, List.of("ADMIN", "OPERADOR")));
        modulos.put(template_btn, new PermissaoModulo(template_pane, template_btn, List.of("ADMIN", "OPERADOR")));
        modulos.put(impressao_btn, new PermissaoModulo(impressao_zpl, impressao_btn, List.of("ADMIN", "OPERADOR")));
        modulos.put(printers_btn, new PermissaoModulo(printer_pane, printers_btn, List.of("ADMIN", "OPERADOR")));
        modulos.put(monitor_btn, new PermissaoModulo(monitor_pane, monitor_btn, List.of("ADMIN")));
        modulos.put(dispositivos_btn, new PermissaoModulo(dispositivo_pane, dispositivos_btn, List.of("ADMIN")));
        modulos.put(blueprint_btn, new PermissaoModulo(blueprint_pane, blueprint_btn, List.of("ADMIN")));
    }

    private void initVarsTable() {
        colKey.setCellValueFactory(c -> c.getValue().keyProperty());
        colValue.setCellValueFactory(c -> c.getValue().valueProperty());

        // permitir editar **apenas o valor**
        colValue.setCellFactory(TextFieldTableCell.forTableColumn());
        colValue.setOnEditCommit(evt
                -> evt.getRowValue().setValue(evt.getNewValue())
        );

        tblVars.setItems(varsData);
        tblVars.setEditable(true);
    }

    private void initBlueprintTemplateListener() {
        cb_bp_template.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTpl, newTpl) -> {
                    if (newTpl != null) {
                        carregarListaBlueprint(newTpl);   // popula tabela
                        //cb_bp_template.setDisable(true);  // opcional: trava combobox
                    }
                });
    }

    private void carregarListaBlueprint(String nomeTemplate) {
        TemplateZPL tpl = templateService.getTemplateByNome(nomeTemplate);
        tblBlueprint.setItems(
                tpl == null ? FXCollections.observableArrayList()
                        : blueprintService.listarPorTemplate(tpl.getId()));
    }

    private void carregarComboBlueprint() {
        ObservableList<String> templates = templateService.getTemplateList()
                .stream()
                .map(TemplateZPL::getNome)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        cb_bp_template.setItems(templates);
    }

    private void configurarComboPrinter() {
        comboPrinter.setItems(printerService.listarTodos());
        comboPrinter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Printer p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null
                        : p.getNome() + "  (" + p.getIp() + ")");
            }
        });
        comboPrinter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Printer p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null
                        : p.getNome() + "  (" + p.getIp() + ")");
            }
        });
    }

    private void iniciarListenerTemplate() {
        comboTemplate.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> preencherInfoProduto(n));
    }

    private void preencherInfoProduto(String nomeTemplate) {

        // limpa se nada selecionado
        if (nomeTemplate == null) {
            lblNomeProduto.setText("");
            lblNomeReduzido.setText("");
            lblSku.setText("");
            lblDataProducao.setText("");
            lblDataValidade.setText("");
            return;
        }

        TemplateZPL tpl = templateService.getTemplateByNome(nomeTemplate);
        if (tpl == null) {
            return;
        }

        ProdutoLabelData d = produtoLabelDataService.findByTemplateId(tpl.getId());

        lblNomeProduto.setText(d.descCompleta());
        lblNomeReduzido.setText(d.descReduzida());
        lblSku.setText(d.sku());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDataProducao.setText(d.dataProducao() != null ? d.dataProducao().format(fmt) : "");
        lblDataValidade.setText(d.dataValidade() != null ? d.dataValidade().format(fmt) : "");
    }

    private void configuraTxtIntervalo() {
        UnaryOperator<TextFormatter.Change> onlyDigits = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        txtIntervalo.setTextFormatter(new TextFormatter<>(onlyDigits));
        txtIntervalo.setText("200");
    }

    private int getIntervaloMs() {
        int val = Integer.parseInt(txtIntervalo.getText());
        return Math.max(val, 200);
    }

    private void limparUI() {

        /* —— abas Template —— */
        template_nome.clear();
        txtTemplateImpressora.clear();
        inv_num.setText("");

        /* —— aba Impressão —— */
        txtTemplate.clear();
        txtQuantidade.clear();
        txtIntervalo.setText("200");                 // valor mínimo sugerido
        cmbImpressora.getSelectionModel().clearSelection();
        qtdSpinner.getValueFactory().setValue(1);
        tmpSpinner.getValueFactory().setValue(200);
        varsData.clear();
        tblVars.refresh();

        /* —— aba Printers —— */
        limparCamposPrinter(null);                   // já existe

        /* —— aba Dispositivos —— */
        limparCamposDispositivo();                   // já existe

        /* —— aba Blueprint —— */
        tblBlueprint.getItems().clear();
        cb_bp_template.getSelectionModel().clearSelection();

        /* —— LOGs —— */
        txtLog.clear();
        txtLogPrint.clear();
    }

    private void configurarSelecaoTemplate() {
        lista_template.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, novo) -> {
                    if (novo != null) {
                        selecionadoTemplate = novo;

                        template_nome.setText(novo.getNome());
                        txtTemplateImpressora.setText(novo.getTemplateImpressora());
                        conteudoTemplate = novo.getConteudo();

                        inv_num.setText("Conteúdo carregado do banco.");
                    }
                });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Modules.exportAllToAll();

        initVarsTable();
        configurarPermissoes();
        ocultarBotoesNaoPermitidos();
        activateAnchorPane();

        lblVersao.setText("Versão: " + AppInfo.getVersion());
        setUsername();

        if (btnStop != null) {
            btnStop.setDisable(true);
        }
        if (btnStart != null) {
            btnStart.setDisable(false);
        }
        barStatus.setVisible(false);

        // campos numéricos
        configurarSpinnerQtd();
        configurarSpinnerTmp();
        configuraTxtIntervalo();
        carregarComboTemplate();
        carregarComboBlueprint();
        carregarListaTemplate();
        initBlueprintTemplateListener();
        initPrinterCrud();
        initDispositivoCrud();
        initBlueprintCrud();
        carredarDadosDash();
        configurarSelecaoTemplate();

        btnPrint.disableProperty().bind(botaoDesabilitado);
        configurarComboPrinter();
        iniciarListenerTemplate();
        barPrint.setVisible(false);

        activateDashboard();   // por último
    }
}
