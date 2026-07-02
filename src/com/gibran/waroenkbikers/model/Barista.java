package com.gibran.waroenkbikers.model;

public class Barista {
    private int id;
    private String kodeBarista;
    private String nama;
    private String divisi;
    private String jabatan;
    private String tanggalMasuk;
    private String status;

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

    public String getDivisi() {
        return divisi;
    }

    public void setDivisi(String divisi) {
        this.divisi = divisi;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    public String getTanggalMasuk() {
        return tanggalMasuk;
    }

    public void setTanggalMasuk(String tanggalMasuk) {
        this.tanggalMasuk = tanggalMasuk;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return kodeBarista + " - " + nama;
    }
}
