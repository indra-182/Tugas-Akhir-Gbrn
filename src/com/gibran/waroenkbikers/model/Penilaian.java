package com.gibran.waroenkbikers.model;

public class Penilaian {
    private int id;
    private int idBarista;
    private int idKriteria;
    private double nilai;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdBarista() {
        return idBarista;
    }

    public void setIdBarista(int idBarista) {
        this.idBarista = idBarista;
    }

    public int getIdKriteria() {
        return idKriteria;
    }

    public void setIdKriteria(int idKriteria) {
        this.idKriteria = idKriteria;
    }

    public double getNilai() {
        return nilai;
    }

    public void setNilai(double nilai) {
        this.nilai = nilai;
    }
}
