package br.com.automacaowebia.config;

import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class PermissaoModulo {
    public AnchorPane pane;
    public Button btn;
    public List<String> perfisPermitidos;

    public PermissaoModulo(AnchorPane pane, Button btn, List<String> perfisPermitidos) {
        this.pane = pane;
        this.btn = btn;
        this.perfisPermitidos = perfisPermitidos;
    }
}