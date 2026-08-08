package com.gibran.waroenkbikers.model;

public class Kriteria {
    private int id;
    private String kode;
    private String nama;
    private double bobot;
    private int urutanPrioritas;
    private String keterangan;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public double getBobot() {
        return bobot;
    }

    public void setBobot(double bobot) {
        this.bobot = bobot;
    }

    public int getUrutanPrioritas() {
        return urutanPrioritas;
    }

    public void setUrutanPrioritas(int urutanPrioritas) {
        this.urutanPrioritas = urutanPrioritas;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    @Override
    public String toString() {
        return kode + " - " + nama;
    }
}
