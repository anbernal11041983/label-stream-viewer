package br.com.automacaowebia.controller;

import br.com.automacaowebia.service.ImpressaoZPLService;
import br.com.automacaowebia.service.ZplCacheService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
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
        // ✅ Remove qualquer binding anterior no botão
        btnConfirmar.disableProperty().unbind();
        barStatus.progressProperty().unbind();

        btnConfirmar.setDisable(true);
        barStatus.setVisible(true);

        int qtd = qtdOriginal;

        String zpl = ZplCacheService.getZpl(cacheKey);
        if (zpl == null) {
            logger.error("ZPL não encontrado no cache!");
            fechar();
            return;
        }

        Task<Void> t = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 1; i <= qtd; i++) {
                    impressaoService.imprimirNaZebra(
                            zpl,
                            cmbImpressora.getValue(),
                            chkSalvarFlash.isSelected()
                    );
                    updateProgress(i, qtd);
                }
                return null;
            }
        };

        barStatus.progressProperty().bind(t.progressProperty());

        t.setOnSucceeded(ev -> {
            logger.info("Impressão concluída com sucesso.");
            barStatus.progressProperty().unbind();
            fechar();
        });

        t.setOnFailed(ev -> {
            logger.error("Erro durante a impressão.", t.getException());
            barStatus.progressProperty().unbind();
            fechar();
        });

        new Thread(t).start();
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
