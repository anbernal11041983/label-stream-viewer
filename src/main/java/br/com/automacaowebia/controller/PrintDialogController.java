package br.com.automacaowebia.controller;

import br.com.automacaowebia.service.HistoricoImpressaoService;
import br.com.automacaowebia.service.ImpressaoZPLService;
import br.com.automacaowebia.service.ZplCacheService;
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

        cmbImpressora.getItems().setAll("Zebra ZT411", "ZDesigner GK420d");
        cmbImpressora.getSelectionModel().selectFirst();
    }

    /**
     * Inicia a impressão no background
     */
    @FXML
    private void imprimir() {

        btnConfirmar.setDisable(true);
        barStatus.setVisible(true);

        final int qtd = qtdOriginal;
        final String zpl = ZplCacheService.getZpl(cacheKey);
        final String printerIp = cmbImpressora.getValue();
        final String modelo = lblTemplate.getText();
        final String sku = lblSku.getText();

        if (zpl == null) {
            logger.error("ZPL ausente no cache (key={})", cacheKey);
            fechar();
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                impressaoService.imprimirNaZebra(
                        zpl, printerIp, chkSalvarFlash.isSelected(),
                        modelo, sku, qtd);
                updateProgress(1, 1);
                return null;
            }
        };

        barStatus.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> fechar());
        task.setOnFailed(e -> {
            logger.error("Falha no job", task.getException());
            fechar();
        });

        new Thread(task, "print-job").start();
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
