package br.com.automacaowebia.model;

import java.time.LocalDateTime;


public class Dispositivo {
    private Integer id;
    private String macAddress;
    private String status;
    private LocalDateTime criadoEm;

    public Dispositivo() {}

    public Dispositivo(Integer id, String macAddress, String status) {
        this.id = id;
        this.macAddress = macAddress;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * @return the criadoEm
     */
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    /**
     * @param criadoEm the criadoEm to set
     */
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
