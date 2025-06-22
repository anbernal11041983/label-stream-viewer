package br.com.automacaowebia.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemplateZPL {

    private Long id;
    private String nome;
    private String tipoArquivo;
    private String conteudo;
    private LocalDateTime criadoEm; // 🔥 Adicionado o campo de data

    public TemplateZPL() {
    }

    public TemplateZPL(Long id, String nome, String tipoArquivo, String conteudo, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.tipoArquivo = tipoArquivo;
        this.conteudo = conteudo;
        this.criadoEm = criadoEm;
    }

    public TemplateZPL(String nome, String tipoArquivo, String conteudo) {
        this.nome = nome;
        this.tipoArquivo = tipoArquivo;
        this.conteudo = conteudo;
    }

    public String getCriadoEmFormatado() {
        if (criadoEm == null) return "";
        return criadoEm.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}