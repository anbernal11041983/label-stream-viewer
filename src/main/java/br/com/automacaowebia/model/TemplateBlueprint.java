package br.com.automacaowebia.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class TemplateBlueprint {

    private Long id;
    private Long templateId;
    private String templateNome;   // apenas para exibição em tela
    private String marcador;       // T1, B1, Q1…
    private String campo;          // sku, peso…
    private Integer ordem;

    private final BooleanProperty ativoProperty = new SimpleBooleanProperty(this, "ativo");

    public TemplateBlueprint() {
    }

    public TemplateBlueprint(Long id, Long templateId, String templateNome,
            String marcador, String campo,
            Integer ordem, boolean ativo) {
        this.id = id;
        this.templateId = templateId;
        this.templateNome = templateNome;
        this.marcador = marcador;
        this.campo = campo;
        this.ordem = ordem;
        this.ativoProperty.set(ativo);
    }

    /**
     * Construtor sem o {@code templateNome}.
     */
    public TemplateBlueprint(Long id, Long templateId,
            String marcador, String campo,
            Integer ordem, boolean ativo) {
        this(id, templateId, null, marcador, campo, ordem, ativo);
    }

    /**
     * Construtor para INSERT (id autogerado).
     */
    public TemplateBlueprint(Long templateId,
            String marcador, String campo,
            Integer ordem, boolean ativo) {
        this(null, templateId, null, marcador, campo, ordem, ativo);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateNome() {
        return templateNome;
    }

    public void setTemplateNome(String templateNome) {
        this.templateNome = templateNome;
    }

    public String getMarcador() {
        return marcador;
    }

    public void setMarcador(String marcador) {
        this.marcador = marcador;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public BooleanProperty ativoProperty() {
        return ativoProperty;
    }

    public boolean isAtivo() {
        return ativoProperty.get();
    }

    public void setAtivo(boolean ativo) {
        this.ativoProperty.set(ativo);
    }

    @Override
    public String toString() {
        return marcador + " → " + campo + (isAtivo() ? "" : " (desativado)");
    }
}
