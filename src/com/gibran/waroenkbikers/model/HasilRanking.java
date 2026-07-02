package com.gibran.waroenkbikers.model;

public class HasilRanking {
    private int idBarista;
    private String kodeBarista;
    private String namaBarista;
    private String divisi;
    private double nilaiMagiq;
    private int peringkat;

    public int getIdBarista() {
        return idBarista;
    }

    public void setIdBarista(int idBarista) {
        this.idBarista = idBarista;
    }

    public String getKodeBarista() {
        return kodeBarista;
    }

    public void setKodeBarista(String kodeBarista) {
        this.kodeBarista = kodeBarista;
    }

    public String getNamaBarista() {
        return namaBarista;
    }

    public void setNamaBarista(String namaBarista) {
        this.namaBarista = namaBarista;
    }

    public String getDivisi() {
        return divisi;
    }

    public void setDivisi(String divisi) {
        this.divisi = divisi;
    }

    public double getNilaiMagiq() {
        return nilaiMagiq;
    }

    public void setNilaiMagiq(double nilaiMagiq) {
        this.nilaiMagiq = nilaiMagiq;
    }

    public int getPeringkat() {
        return peringkat;
    }

    public void setPeringkat(int peringkat) {
        this.peringkat = peringkat;
    }
}
