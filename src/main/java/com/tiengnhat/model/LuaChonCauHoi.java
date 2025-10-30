package com.tiengnhat.model;

public class LuaChonCauHoi {
    private int maLuaChon;
    private int maCauHoi; // Khóa ngoại
    private String kyTuLuaChon; // 'A', 'B', 'C', 'D'
    private String noiDungLuaChon;
    private boolean laDapAnDung;

    // Constructors
    public LuaChonCauHoi() {
    }

    public LuaChonCauHoi(int maCauHoi, String kyTuLuaChon, String noiDungLuaChon, boolean laDapAnDung) {
        this.maCauHoi = maCauHoi;
        this.kyTuLuaChon = kyTuLuaChon;
        this.noiDungLuaChon = noiDungLuaChon;
        this.laDapAnDung = laDapAnDung;
    }
     public LuaChonCauHoi(String kyTuLuaChon, String noiDungLuaChon, boolean laDapAnDung) {
        this.kyTuLuaChon = kyTuLuaChon;
        this.noiDungLuaChon = noiDungLuaChon;
        this.laDapAnDung = laDapAnDung;
    }


    // Getters and Setters
    public int getMaLuaChon() {
        return maLuaChon;
    }

    public void setMaLuaChon(int maLuaChon) {
        this.maLuaChon = maLuaChon;
    }

    public int getMaCauHoi() {
        return maCauHoi;
    }

    public void setMaCauHoi(int maCauHoi) {
        this.maCauHoi = maCauHoi;
    }

    public String getKyTuLuaChon() {
        return kyTuLuaChon;
    }

    public void setKyTuLuaChon(String kyTuLuaChon) {
        this.kyTuLuaChon = kyTuLuaChon;
    }

    public String getNoiDungLuaChon() {
        return noiDungLuaChon;
    }

    public void setNoiDungLuaChon(String noiDungLuaChon) {
        this.noiDungLuaChon = noiDungLuaChon;
    }

    public boolean isLaDapAnDung() { // Getter cho boolean thường là 'isPropertyName'
        return laDapAnDung;
    }

    public void setLaDapAnDung(boolean laDapAnDung) {
        this.laDapAnDung = laDapAnDung;
    }

    @Override
    public String toString() {
        return "LuaChonCauHoi{" +
                "maLuaChon=" + maLuaChon +
                ", maCauHoi=" + maCauHoi +
                ", kyTuLuaChon='" + kyTuLuaChon + '\'' +
                ", noiDungLuaChon='" + noiDungLuaChon.substring(0, Math.min(noiDungLuaChon.length(), 30)) + "..." + '\'' +
                ", laDapAnDung=" + laDapAnDung +
                '}';
    }
}