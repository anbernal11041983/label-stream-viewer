package br.com.automacaowebia.session;

import br.com.automacaowebia.model.User;

public class Session {

    private static Session instance;
    private User user;

    private Session() {}

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
}
