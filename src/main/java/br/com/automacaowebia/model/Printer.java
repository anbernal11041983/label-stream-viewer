package br.com.automacaowebia.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Printer {

    private final LongProperty            id       = new SimpleLongProperty();
    private final StringProperty          nome     = new SimpleStringProperty();
    private final StringProperty          ip       = new SimpleStringProperty();
    private final IntegerProperty         porta    = new SimpleIntegerProperty();
    private final StringProperty          modelo   = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> criadoEm = new SimpleObjectProperty<>();

    public Printer() { this(null, "", "", 9100, ""); }

    public Printer(Long id, String nome, String ip, int porta, String modelo) {
        setId(id);
        setNome(nome);
        setIp(ip);
        setPorta(porta);
        setModelo(modelo);
        setCriadoEm(LocalDateTime.now());
    }

    // --- getters / setters -------------------------------------------------
    public long getId()                 { return id.get(); }
    public void setId(Long value)       { this.id.set(value == null ? 0 : value); }
    public LongProperty idProperty()    { return id; }

    public String getNome()             { return nome.get(); }
    public void setNome(String value)   { nome.set(value); }
    public StringProperty nomeProperty(){ return nome; }

    public String getIp()               { return ip.get(); }
    public void setIp(String value)     { ip.set(value); }
    public StringProperty ipProperty()  { return ip; }

    public int getPorta()               { return porta.get(); }
    public void setPorta(int value)     { porta.set(value); }
    public IntegerProperty portaProperty(){ return porta; }

    public String getModelo()           { return modelo.get(); }
    public void setModelo(String value) { modelo.set(value); }
    public StringProperty modeloProperty(){ return modelo; }

    public LocalDateTime getCriadoEm()                { return criadoEm.get(); }
    public void setCriadoEm(LocalDateTime value)      { criadoEm.set(value); }
    public ObjectProperty<LocalDateTime> criadoEmProperty(){ return criadoEm; }
}
