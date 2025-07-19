package br.com.automacaowebia.session;

import br.com.automacaowebia.model.Dispositivo;
import br.com.automacaowebia.model.User;

public class Session {

    private static Session instance;
    private User user;
    private Dispositivo dispositivoAtual;
    private String fingerprintAtual;

    private Session() {
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void cleanSession() {
        user = null;
        instance = null;
    }

    public boolean isLogged() {
        return user != null;
    }

    public Dispositivo getDispositivoAtual() {
        return dispositivoAtual;
    }

    public void setDispositivoAtual(Dispositivo d) {
        dispositivoAtual = d;
    }

    public String getFingerprintAtual() {
        return fingerprintAtual;
    }

    public void setFingerprintAtual(String fp) {
        fingerprintAtual = fp;
    }
}
