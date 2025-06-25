package br.com.automacaowebia.controller;

import br.com.automacaowebia.service.HistoricoImpressaoService;
import br.com.automacaowebia.service.ImpressaoZPLService;
import br.com.automacaowebia.service.ZplCacheService;
import br.com.automacaowebia.model.Printer;
import br.com.automacaowebia.service.PrinterService;
import br.com.automacaowebia.util.PrintExecutor;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintDialogController {

    @FXML
    private ImageView imgMiniPreview;
    @FXML
    private Label lblTemplate;
    @FXML
    private Label lblSku;
    @FXML
    private Label lblQtd;
    @FXML
    private Label lblLarg;
    @FXML
    private Label lblAlt;
    @FXML
    private Label lblUnid;
    @FXML
    private ComboBox<String> cmbImpressora;
    @FXML
    private CheckBox chkSalvarFlash;
    @FXML
    private ProgressBar barStatus;
    @FXML
    private Button btnConfirmar;

    private String cacheKey;
    private int qtdOriginal;
    private final ImpressaoZPLService impressaoService = new ImpressaoZPLService();
    private final PrinterService printerService = new PrinterService();

    private static final Logger logger = LogManager.getLogger(PrintDialogController.class);

    /**
     * Método chamado ao abrir o modal para preencher os dados.
     */
    public void initData(String tpl, String sku,
            double larg, double alt, String unid,
            int qtd, Image preview, String cacheKey) {

        this.cacheKey = cacheKey;
        this.qtdOriginal = qtd;

        imgMiniPreview.setImage(preview);
        lblTemplate.setText(tpl);
        lblSku.setText(sku);
        lblQtd.setText(String.valueOf(qtd));
        lblLarg.setText(String.valueOf(larg));
        lblAlt.setText(String.valueOf(alt));
        lblUnid.setText(unid);

        cmbImpressora.getItems().clear();
        for (Printer p : printerService.listarTodos()) {
            // Ex.:  ZT-411 (192.168.1.90)
            cmbImpressora.getItems()
                    .add(String.format("%s (%s)", p.getNome(), p.getIp()));
        }

        if (!cmbImpressora.getItems().isEmpty()) {
            cmbImpressora.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void imprimir() {

        // verifica se o usuário escolheu alguma impressora
        String impressoraSelecionada = cmbImpressora.getValue();
        if (impressoraSelecionada == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Escolha uma impressora antes de confirmar.").showAndWait();
            return;
        }

        String printerIp = impressoraSelecionada
                .replaceAll(".*\\((.*)\\)", "$1")
                .trim();

        btnConfirmar.disableProperty().unbind();
        btnConfirmar.setDisable(true);
        barStatus.setVisible(true);

        final int qtd = qtdOriginal;
        final String zpl = ZplCacheService.getZpl(cacheKey);
        final String modelo = lblTemplate.getText();
        final String sku = lblSku.getText();

        if (zpl == null) {
            logger.error("ZPL ausente no cache (key={})", cacheKey);
            fechar();
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                impressaoService.imprimirNaZebra(
                        zpl,
                        printerIp,
                        chkSalvarFlash.isSelected(),
                        modelo,
                        sku,
                        qtd);
                updateProgress(1, 1);
                return null;
            }
        };

        barStatus.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> fechar());
        task.setOnFailed(e -> {
            logger.error("Falha no job", task.getException());
            // volta estado visual
            Throwable ex = task.getException();  // exception real lançada pelo Task
            logger.error("Falha no job", ex);

            // restaura o estado da UI
            barStatus.setVisible(false);
            btnConfirmar.setDisable(false);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro na impressão");
            alert.setHeaderText(null);

            String msg = (ex != null && ex.getMessage() != null)
                    ? ex.getMessage()
                    : "Não foi possível enviar a etiqueta para a impressora.";
            alert.setContentText(msg);
            alert.showAndWait();
        });

        PrintExecutor.POOL.submit(task);
    }

    /**
     * Fecha o modal
     */
    @FXML
    private void fechar() {
        Stage s = (Stage) btnConfirmar.getScene().getWindow();
        s.close();
    }
}
