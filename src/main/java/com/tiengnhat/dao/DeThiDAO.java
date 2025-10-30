package com.tiengnhat.dao;

import com.tiengnhat.db.DatabaseManager;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.CauHoiTrongDeThi;
import com.tiengnhat.model.DeThi;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeThiDAO {
    private CauHoiDAO cauHoiDAO; // Để lấy thông tin chi tiết câu hỏi

    public DeThiDAO() {
        this.cauHoiDAO = new CauHoiDAO(); // Khởi tạo CauHoiDAO
    }

    public boolean themDeThi(DeThi deThi) {
        String sql = "INSERT INTO DE_THI (TenDeThi, MoTa, ChoPhepTronCauHoi, TongSoCauHoi, NgayTao, NgayCapNhat) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DatabaseManager.getConnection();
            // Không cần transaction phức tạp nếu chỉ thêm vào bảng DE_THI
            // Transaction sẽ cần nếu bạn đồng thời thêm câu hỏi vào đề thi
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, deThi.getTenDeThi());
            ps.setString(2, deThi.getMoTa());
            ps.setBoolean(3, deThi.isChoPhepTronCauHoi());
            if (deThi.getTongSoCauHoi() != null) {
                ps.setInt(4, deThi.getTongSoCauHoi());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            deThi.setNgayTao(LocalDateTime.now());
            deThi.setNgayCapNhat(LocalDateTime.now());
            ps.setTimestamp(5, Timestamp.valueOf(deThi.getNgayTao()));
            ps.setTimestamp(6, Timestamp.valueOf(deThi.getNgayCapNhat()));

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    deThi.setMaDeThi(generatedKeys.getInt(1));
                }
                success = true;
                // Lưu ý: Việc thêm câu hỏi vào đề thi (bảng CAU_HOI_TRONG_DE_THI)
                // nên được thực hiện bằng một phương thức riêng biệt, ví dụ: themCauHoiVaoDeThi()
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm đề thi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

    public DeThi layDeThiTheoId(int maDeThi) {
        String sqlDeThi = "SELECT * FROM DE_THI WHERE MaDeThi = ?";
        String sqlCauHoiTrongDe = "SELECT MaCauHoi, ThuTuTrongDeThi FROM CAU_HOI_TRONG_DE_THI WHERE MaDeThi = ? ORDER BY ThuTuTrongDeThi ASC";
        DeThi deThi = null;
        Connection conn = null;
        PreparedStatement psDeThi = null;
        PreparedStatement psCauHoiTrongDe = null;
        ResultSet rsDeThi = null;
        ResultSet rsCauHoiTrongDe = null;

        try {
            conn = DatabaseManager.getConnection();
            psDeThi = conn.prepareStatement(sqlDeThi);
            psDeThi.setInt(1, maDeThi);
            rsDeThi = psDeThi.executeQuery();

            if (rsDeThi.next()) {
                deThi = new DeThi();
                deThi.setMaDeThi(rsDeThi.getInt("MaDeThi"));
                deThi.setTenDeThi(rsDeThi.getString("TenDeThi"));
                deThi.setMoTa(rsDeThi.getString("MoTa"));
                deThi.setChoPhepTronCauHoi(rsDeThi.getBoolean("ChoPhepTronCauHoi"));
                deThi.setTongSoCauHoi(rsDeThi.getObject("TongSoCauHoi", Integer.class)); // Lấy cả null
                Timestamp ngayTaoTs = rsDeThi.getTimestamp("NgayTao");
                if (ngayTaoTs != null) deThi.setNgayTao(ngayTaoTs.toLocalDateTime());
                Timestamp ngayCapNhatTs = rsDeThi.getTimestamp("NgayCapNhat");
                if (ngayCapNhatTs != null) deThi.setNgayCapNhat(ngayCapNhatTs.toLocalDateTime());

                // Lấy danh sách câu hỏi thuộc về đề thi này
                psCauHoiTrongDe = conn.prepareStatement(sqlCauHoiTrongDe);
                psCauHoiTrongDe.setInt(1, maDeThi);
                rsCauHoiTrongDe = psCauHoiTrongDe.executeQuery();

                List<CauHoi> danhSachCauHoi = new ArrayList<>();
                List<CauHoiTrongDeThi> chiTietCauHoi = new ArrayList<>();

                while (rsCauHoiTrongDe.next()) {
                    int maCauHoi = rsCauHoiTrongDe.getInt("MaCauHoi");
                    Integer thuTu = rsCauHoiTrongDe.getObject("ThuTuTrongDeThi", Integer.class);

                    CauHoi cauHoi = cauHoiDAO.layCauHoiTheoId(maCauHoi); // Lấy chi tiết câu hỏi
                    if (cauHoi != null) {
                        danhSachCauHoi.add(cauHoi);
                    }
                    
                    CauHoiTrongDeThi chtdt = new CauHoiTrongDeThi(maDeThi, maCauHoi, thuTu);
                    // Nếu bạn muốn lấy MaCauHoiDeThi (PK của bảng liên kết), bạn cần select nó
                    chiTietCauHoi.add(chtdt);
                }
                deThi.setDanhSachCauHoiTrongDe(danhSachCauHoi);
                deThi.setChiTietCauHoiTrongDe(chiTietCauHoi);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy đề thi theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rsCauHoiTrongDe != null) rsCauHoiTrongDe.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psCauHoiTrongDe != null) psCauHoiTrongDe.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (rsDeThi != null) rsDeThi.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psDeThi != null) psDeThi.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return deThi;
    }
    
    public List<DeThi> layTatCaDeThi() {
        String sql = "SELECT MaDeThi FROM DE_THI";
        List<DeThi> danhSachDeThi = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                DeThi dt = layDeThiTheoId(rs.getInt("MaDeThi"));
                if(dt != null){
                    danhSachDeThi.add(dt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả đề thi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return danhSachDeThi;
    }

    public boolean capNhatDeThi(DeThi deThi) {
        String sql = "UPDATE DE_THI SET TenDeThi = ?, MoTa = ?, ChoPhepTronCauHoi = ?, TongSoCauHoi = ?, NgayCapNhat = ? WHERE MaDeThi = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, deThi.getTenDeThi());
            ps.setString(2, deThi.getMoTa());
            ps.setBoolean(3, deThi.isChoPhepTronCauHoi());
             if (deThi.getTongSoCauHoi() != null) {
                ps.setInt(4, deThi.getTongSoCauHoi());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            deThi.setNgayCapNhat(LocalDateTime.now());
            ps.setTimestamp(5, Timestamp.valueOf(deThi.getNgayCapNhat()));
            ps.setInt(6, deThi.getMaDeThi());

            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                success = true;
                // Việc cập nhật danh sách câu hỏi trong đề thi (bảng CAU_HOI_TRONG_DE_THI)
                // nên được xử lý riêng, ví dụ: xóa hết câu hỏi cũ rồi thêm lại, hoặc so sánh để cập nhật.
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đề thi: " + e.getMessage());
            e.printStackTrace();
        } finally {
             try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }
    
    public boolean xoaDeThi(int maDeThi) {
        String sql = "DELETE FROM DE_THI WHERE MaDeThi = ?";
        // Do ON DELETE CASCADE trên bảng CAU_HOI_TRONG_DE_THI, các liên kết sẽ tự xóa
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maDeThi);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0) {
                success = true;
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi xóa đề thi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

    // --- Quản lý câu hỏi trong đề thi ---

    public boolean themCauHoiVaoDeThi(int maDeThi, int maCauHoi, Integer thuTu) {
        String sql = "INSERT INTO CAU_HOI_TRONG_DE_THI (MaDeThi, MaCauHoi, ThuTuTrongDeThi) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maDeThi);
            ps.setInt(2, maCauHoi);
            if (thuTu != null) {
                ps.setInt(3, thuTu);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                success = true;
                // Cập nhật lại TongSoCauHoi trong bảng DE_THI nếu cần
                capNhatTongSoCauHoiCuaDeThi(maDeThi, conn);
            }
        } catch (SQLException e) {
            // Bắt lỗi UNIQUE KEY UK_DeThi_CauHoi nếu câu hỏi đã tồn tại trong đề
            if (e.getErrorCode() == 1062) { // Mã lỗi cho duplicate entry trong MySQL
                 System.err.println("Lỗi: Câu hỏi (ID: " + maCauHoi +") đã tồn tại trong đề thi (ID: " + maDeThi + ").");
            } else {
                System.err.println("Lỗi khi thêm câu hỏi vào đề thi: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }
    
    public boolean xoaCauHoiKhoiDeThi(int maDeThi, int maCauHoi) {
        String sql = "DELETE FROM CAU_HOI_TRONG_DE_THI WHERE MaDeThi = ? AND MaCauHoi = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maDeThi);
            ps.setInt(2, maCauHoi);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                success = true;
                // Cập nhật lại TongSoCauHoi trong bảng DE_THI
                capNhatTongSoCauHoiCuaDeThi(maDeThi, conn);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa câu hỏi khỏi đề thi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }
    
    // Phương thức cập nhật lại cột TongSoCauHoi trong bảng DE_THI
    private void capNhatTongSoCauHoiCuaDeThi(int maDeThi, Connection conn) throws SQLException {
        // Nếu conn được truyền vào, nghĩa là đang trong một transaction khác, không tự quản lý commit/close
        boolean manageConnection = (conn == null);
        Connection localConn = null;

        String sqlCount = "SELECT COUNT(*) FROM CAU_HOI_TRONG_DE_THI WHERE MaDeThi = ?";
        String sqlUpdate = "UPDATE DE_THI SET TongSoCauHoi = ? WHERE MaDeThi = ?";
        PreparedStatement psCount = null;
        PreparedStatement psUpdate = null;
        ResultSet rsCount = null;

        try {
            if (manageConnection) {
                localConn = DatabaseManager.getConnection();
            } else {
                localConn = conn; // Sử dụng connection được truyền vào
            }

            psCount = localConn.prepareStatement(sqlCount);
            psCount.setInt(1, maDeThi);
            rsCount = psCount.executeQuery();
            int tongSoCauHoiMoi = 0;
            if (rsCount.next()) {
                tongSoCauHoiMoi = rsCount.getInt(1);
            }

            psUpdate = localConn.prepareStatement(sqlUpdate);
            psUpdate.setInt(1, tongSoCauHoiMoi);
            psUpdate.setInt(2, maDeThi);
            psUpdate.executeUpdate();

        } finally {
            try { if (rsCount != null) rsCount.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psCount != null) psCount.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psUpdate != null) psUpdate.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (manageConnection && localConn != null) {
                // Nếu tự quản lý connection thì đóng nó.
                // Lưu ý: không nên đóng connection nếu nó được truyền từ bên ngoài và đang trong transaction.
                // DatabaseManager.closeConnection(); // Không nên đóng ở đây trừ khi bạn muốn đóng toàn cục
            }
        }
    }
    
    // Phương thức để cập nhật thứ tự các câu hỏi trong một đề thi
    public boolean capNhatThuTuCauHoiTrongDeThi(int maDeThi, List<CauHoiTrongDeThi> danhSachCauHoiTheoThuTuMoi) {
        // Cách tiếp cận: Xóa hết các câu hỏi hiện tại của đề thi đó (trong CAU_HOI_TRONG_DE_THI)
        // rồi thêm lại theo thứ tự mới. Hoặc, cập nhật từng bản ghi (phức tạp hơn).
        // Cách đơn giản:
        String sqlDeleteAll = "DELETE FROM CAU_HOI_TRONG_DE_THI WHERE MaDeThi = ?";
        String sqlInsert = "INSERT INTO CAU_HOI_TRONG_DE_THI (MaDeThi, MaCauHoi, ThuTuTrongDeThi) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        boolean success = false;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            psDelete = conn.prepareStatement(sqlDeleteAll);
            psDelete.setInt(1, maDeThi);
            psDelete.executeUpdate();

            psInsert = conn.prepareStatement(sqlInsert);
            for (CauHoiTrongDeThi chtdt : danhSachCauHoiTheoThuTuMoi) {
                psInsert.setInt(1, maDeThi); // Đảm bảo MaDeThi đúng
                psInsert.setInt(2, chtdt.getMaCauHoi());
                if (chtdt.getThuTuTrongDeThi() != null) {
                    psInsert.setInt(3, chtdt.getThuTuTrongDeThi());
                } else {
                    psInsert.setNull(3, Types.INTEGER);
                }
                psInsert.addBatch();
            }
            psInsert.executeBatch();

            conn.commit();
            success = true;
            // Cập nhật lại TongSoCauHoi sau khi thay đổi
            capNhatTongSoCauHoiCuaDeThi(maDeThi, conn); // Truyền conn để không tạo connection mới

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thứ tự câu hỏi trong đề thi: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { if (psDelete != null) psDelete.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psInsert != null) psInsert.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }
    public List<DeThi> timKiemDeThi(String tuKhoa) {
        List<DeThi> ketQua = new ArrayList<>();
        List<DeThi> danhSachDeThi = layTatCaDeThi();
        
        for (DeThi deThi : danhSachDeThi) {
            if ((deThi.getTenDeThi() != null && deThi.getTenDeThi().toLowerCase().contains(tuKhoa.toLowerCase())) ||
                (deThi.getMoTa() != null && deThi.getMoTa().toLowerCase().contains(tuKhoa.toLowerCase()))) {
                ketQua.add(deThi);
            }
        }
        
        return ketQua;
    }
}