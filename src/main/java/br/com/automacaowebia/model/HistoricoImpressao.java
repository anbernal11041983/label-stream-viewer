package br.com.automacaowebia.model;

import java.time.LocalDateTime;

public class HistoricoImpressao {

    private Integer id;
    private String modelo;
    private String sku;
    private Integer quantidade;
    private LocalDateTime dataHora;
    private String impressora;

    public HistoricoImpressao() {
    }

    public HistoricoImpressao(Integer id, String modelo, String sku, Integer quantidade,
                               LocalDateTime dataHora, String impressora) {
        this.id = id;
        this.modelo = modelo;
        this.sku = sku;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
        this.impressora = impressora;
    }

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getImpressora() {
        return impressora;
    }

    public void setImpressora(String impressora) {
        this.impressora = impressora;
    }

    @Override
    public String toString() {
        return "HistoricoImpressao{" +
                "id=" + id +
                ", modelo='" + modelo + '\'' +
                ", sku='" + sku + '\'' +
                ", quantidade=" + quantidade +
                ", dataHora=" + dataHora +
                ", impressora='" + impressora + '\'' +
                '}';
    }
}
