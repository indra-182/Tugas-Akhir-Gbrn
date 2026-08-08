package com.gibran.waroenkbikers.model;

public class Barista {
    private int id;
    private String kodeBarista;
    private String nama;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKodeBarista() {
        return kodeBarista;
    }

    public void setKodeBarista(String kodeBarista) {
        this.kodeBarista = kodeBarista;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    @Override
    public String toString() {
        return kodeBarista + " - " + nama;
    }
}
