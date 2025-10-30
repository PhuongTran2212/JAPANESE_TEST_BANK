package com.tiengnhat.model;

public class CauHoiTrongDeThi {
    private int maCauHoiDeThi;
    private int maDeThi;
    private int maCauHoi;
    private Integer thuTuTrongDeThi; // Sử dụng Integer để cho phép null nếu thứ tự chưa được xác định
    // private BigDecimal diemSo; // Nếu bạn kích hoạt cột này

    // Constructors
    public CauHoiTrongDeThi() {
    }

    public CauHoiTrongDeThi(int maDeThi, int maCauHoi, Integer thuTuTrongDeThi) {
        this.maDeThi = maDeThi;
        this.maCauHoi = maCauHoi;
        this.thuTuTrongDeThi = thuTuTrongDeThi;
    }

    // Getters and Setters
    public int getMaCauHoiDeThi() {
        return maCauHoiDeThi;
    }

    public void setMaCauHoiDeThi(int maCauHoiDeThi) {
        this.maCauHoiDeThi = maCauHoiDeThi;
    }

    public int getMaDeThi() {
        return maDeThi;
    }

    public void setMaDeThi(int maDeThi) {
        this.maDeThi = maDeThi;
    }

    public int getMaCauHoi() {
        return maCauHoi;
    }

    public void setMaCauHoi(int maCauHoi) {
        this.maCauHoi = maCauHoi;
    }

    public Integer getThuTuTrongDeThi() {
        return thuTuTrongDeThi;
    }

    public void setThuTuTrongDeThi(Integer thuTuTrongDeThi) {
        this.thuTuTrongDeThi = thuTuTrongDeThi;
    }

    // public BigDecimal getDiemSo() {
    //     return diemSo;
    // }

    // public void setDiemSo(BigDecimal diemSo) {
    //     this.diemSo = diemSo;
    // }

    @Override
    public String toString() {
        return "CauHoiTrongDeThi{" +
                "maCauHoiDeThi=" + maCauHoiDeThi +
                ", maDeThi=" + maDeThi +
                ", maCauHoi=" + maCauHoi +
                ", thuTuTrongDeThi=" + thuTuTrongDeThi +
                '}';
    }
}