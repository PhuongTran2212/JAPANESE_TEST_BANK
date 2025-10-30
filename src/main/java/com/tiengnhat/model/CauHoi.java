package com.tiengnhat.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class CauHoi {
    private int maCauHoi;
    private String noiDungCauHoi;
    private String loaiCauHoi;
    private String doKho;
    private String khoaDapAnDung; // Có thể là key ('A', 'B') hoặc nội dung nếu là điền khuyết
    private String giaiThichDapAn;
    private String dapAnGoiYTuAI;
    private String duongDanFileAmThanh;
    private String nhan; // Tags
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Đối với câu hỏi trắc nghiệm, sẽ có danh sách các lựa chọn
    private List<LuaChonCauHoi> danhSachLuaChon;

    // Constructors
    public CauHoi() {
        this.danhSachLuaChon = new ArrayList<>();
    }

    public CauHoi(String noiDungCauHoi, String loaiCauHoi, String doKho, String khoaDapAnDung, String giaiThichDapAn, String duongDanFileAmThanh, String nhan) {
        this.noiDungCauHoi = noiDungCauHoi;
        this.loaiCauHoi = loaiCauHoi;
        this.doKho = doKho;
        this.khoaDapAnDung = khoaDapAnDung;
        this.giaiThichDapAn = giaiThichDapAn;
        this.duongDanFileAmThanh = duongDanFileAmThanh;
        this.nhan = nhan;
        this.danhSachLuaChon = new ArrayList<>();
    }

    // Getters and Setters
    public int getMaCauHoi() {
        return maCauHoi;
    }

    public void setMaCauHoi(int maCauHoi) {
        this.maCauHoi = maCauHoi;
    }

    public String getNoiDungCauHoi() {
        return noiDungCauHoi;
    }

    public void setNoiDungCauHoi(String noiDungCauHoi) {
        this.noiDungCauHoi = noiDungCauHoi;
    }

    public String getLoaiCauHoi() {
        return loaiCauHoi;
    }

    public void setLoaiCauHoi(String loaiCauHoi) {
        this.loaiCauHoi = loaiCauHoi;
    }

    public String getDoKho() {
        return doKho;
    }

    public void setDoKho(String doKho) {
        this.doKho = doKho;
    }

    public String getKhoaDapAnDung() {
        return khoaDapAnDung;
    }

    public void setKhoaDapAnDung(String khoaDapAnDung) {
        this.khoaDapAnDung = khoaDapAnDung;
    }

    public String getGiaiThichDapAn() {
        return giaiThichDapAn;
    }

    public void setGiaiThichDapAn(String giaiThichDapAn) {
        this.giaiThichDapAn = giaiThichDapAn;
    }

    public String getDapAnGoiYTuAI() {
        return dapAnGoiYTuAI;
    }

    public void setDapAnGoiYTuAI(String dapAnGoiYTuAI) {
        this.dapAnGoiYTuAI = dapAnGoiYTuAI;
    }

    public String getDuongDanFileAmThanh() {
        return duongDanFileAmThanh;
    }

    public void setDuongDanFileAmThanh(String duongDanFileAmThanh) {
        this.duongDanFileAmThanh = duongDanFileAmThanh;
    }

    public String getNhan() {
        return nhan;
    }

    public void setNhan(String nhan) {
        this.nhan = nhan;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public List<LuaChonCauHoi> getDanhSachLuaChon() {
        return danhSachLuaChon;
    }

    public void setDanhSachLuaChon(List<LuaChonCauHoi> danhSachLuaChon) {
        this.danhSachLuaChon = danhSachLuaChon;
    }

    public void themLuaChon(LuaChonCauHoi luaChon) {
        if (this.danhSachLuaChon == null) {
            this.danhSachLuaChon = new ArrayList<>();
        }
        this.danhSachLuaChon.add(luaChon);
        // Có thể bạn muốn đặt lại MaCauHoi cho LuaChonCauHoi ở đây nếu cần
        // luaChon.setMaCauHoi(this.maCauHoi);
    }


    @Override
    public String toString() {
        return "CauHoi{" +
                "maCauHoi=" + maCauHoi +
                ", noiDungCauHoi='" + noiDungCauHoi.substring(0, Math.min(noiDungCauHoi.length(), 50)) + "..." + '\'' + // Tránh in quá dài
                ", loaiCauHoi='" + loaiCauHoi + '\'' +
                ", doKho='" + doKho + '\'' +
                ", khoaDapAnDung='" + khoaDapAnDung + '\'' +
                ", danhSachLuaChon=" + (danhSachLuaChon != null ? danhSachLuaChon.size() : 0) + " lựa chọn" +
                '}';
    }
}