package com.tiengnhat.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class DeThi {
    private int maDeThi;
    private String tenDeThi;
    private String moTa;
    private boolean choPhepTronCauHoi;
    private Integer tongSoCauHoi; // Sử dụng Integer để cho phép giá trị null nếu cần
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    // private int maNguoiTao; // Nếu bạn kích hoạt cột này

    // Danh sách các câu hỏi có trong đề thi này
    // Đây có thể là List<CauHoi> hoặc List<CauHoiTrongDeThi> tùy cách bạn muốn quản lý
    private List<CauHoi> danhSachCauHoiTrongDe; // Để tiện truy cập thông tin câu hỏi
    private List<CauHoiTrongDeThi> chiTietCauHoiTrongDe; // Để lấy thông tin thứ tự

    // Constructors
    public DeThi() {
        this.danhSachCauHoiTrongDe = new ArrayList<>();
        this.chiTietCauHoiTrongDe = new ArrayList<>();
    }

    public DeThi(String tenDeThi, String moTa, boolean choPhepTronCauHoi, Integer tongSoCauHoi) {
        this.tenDeThi = tenDeThi;
        this.moTa = moTa;
        this.choPhepTronCauHoi = choPhepTronCauHoi;
        this.tongSoCauHoi = tongSoCauHoi;
        this.danhSachCauHoiTrongDe = new ArrayList<>();
        this.chiTietCauHoiTrongDe = new ArrayList<>();
    }

    // Getters and Setters
    public int getMaDeThi() {
        return maDeThi;
    }

    public void setMaDeThi(int maDeThi) {
        this.maDeThi = maDeThi;
    }

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isChoPhepTronCauHoi() {
        return choPhepTronCauHoi;
    }

    public void setChoPhepTronCauHoi(boolean choPhepTronCauHoi) {
        this.choPhepTronCauHoi = choPhepTronCauHoi;
    }

    public Integer getTongSoCauHoi() {
        return tongSoCauHoi;
    }

    public void setTongSoCauHoi(Integer tongSoCauHoi) {
        this.tongSoCauHoi = tongSoCauHoi;
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

    public List<CauHoi> getDanhSachCauHoiTrongDe() {
        return danhSachCauHoiTrongDe;
    }

    public void setDanhSachCauHoiTrongDe(List<CauHoi> danhSachCauHoiTrongDe) {
        this.danhSachCauHoiTrongDe = danhSachCauHoiTrongDe;
    }

    public List<CauHoiTrongDeThi> getChiTietCauHoiTrongDe() {
        return chiTietCauHoiTrongDe;
    }

    public void setChiTietCauHoiTrongDe(List<CauHoiTrongDeThi> chiTietCauHoiTrongDe) {
        this.chiTietCauHoiTrongDe = chiTietCauHoiTrongDe;
    }

    // public int getMaNguoiTao() {
    //     return maNguoiTao;
    // }

    // public void setMaNguoiTao(int maNguoiTao) {
    //     this.maNguoiTao = maNguoiTao;
    // }

    @Override
    public String toString() {
        return "DeThi{" +
                "maDeThi=" + maDeThi +
                ", tenDeThi='" + tenDeThi + '\'' +
                ", tongSoCauHoi=" + tongSoCauHoi +
                '}';
    }
}