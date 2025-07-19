package br.com.automacaowebia.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VarItem {

    private final SimpleStringProperty key = new SimpleStringProperty("");
    private final SimpleStringProperty value = new SimpleStringProperty("");

    public VarItem() {
    }

    public VarItem(String k, String v) {
        key.set(k);
        value.set(v);
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String k) {
        key.set(k);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String v) {
        value.set(v);
    }

    public StringProperty valueProperty() {
        return value;
    }
}
