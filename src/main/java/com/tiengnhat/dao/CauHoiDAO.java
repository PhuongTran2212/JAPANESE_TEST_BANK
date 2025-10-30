package com.tiengnhat.dao;

import com.tiengnhat.db.DatabaseManager;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.LuaChonCauHoi;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CauHoiDAO {

    // Phương thức thêm một câu hỏi mới vào CSDL
    // Bao gồm cả việc thêm các lựa chọn nếu có
    public boolean themCauHoi(CauHoi cauHoi) {
        String sqlCauHoi = "INSERT INTO CAU_HOI (NoiDungCauHoi, LoaiCauHoi, DoKho, KhoaDapAnDung, GiaiThichDapAn, DapAnGoiYTuAI, DuongDanFileAmThanh, Nhan, NgayTao, NgayCapNhat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlLuaChon = "INSERT INTO LUA_CHON_CAU_HOI (MaCauHoi, KyTuLuaChon, NoiDungLuaChon, LaDapAnDung) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement psCauHoi = null;
        PreparedStatement psLuaChon = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction để đảm bảo tính toàn vẹn

            // Thêm vào bảng CAU_HOI
            psCauHoi = conn.prepareStatement(sqlCauHoi, Statement.RETURN_GENERATED_KEYS);
            psCauHoi.setString(1, cauHoi.getNoiDungCauHoi());
            psCauHoi.setString(2, cauHoi.getLoaiCauHoi());
            psCauHoi.setString(3, cauHoi.getDoKho());
            psCauHoi.setString(4, cauHoi.getKhoaDapAnDung());
            psCauHoi.setString(5, cauHoi.getGiaiThichDapAn());
            psCauHoi.setString(6, cauHoi.getDapAnGoiYTuAI());
            psCauHoi.setString(7, cauHoi.getDuongDanFileAmThanh());
            psCauHoi.setString(8, cauHoi.getNhan());
            cauHoi.setNgayTao(LocalDateTime.now()); // Đặt ngày tạo
            cauHoi.setNgayCapNhat(LocalDateTime.now()); // Đặt ngày cập nhật ban đầu
            psCauHoi.setTimestamp(9, Timestamp.valueOf(cauHoi.getNgayTao()));
            psCauHoi.setTimestamp(10, Timestamp.valueOf(cauHoi.getNgayCapNhat()));

            int affectedRows = psCauHoi.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = psCauHoi.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int maCauHoiMoi = generatedKeys.getInt(1);
                    cauHoi.setMaCauHoi(maCauHoiMoi); // Cập nhật ID cho đối tượng CauHoi

                    // Nếu câu hỏi có các lựa chọn (ví dụ: trắc nghiệm)
                    if (cauHoi.getDanhSachLuaChon() != null && !cauHoi.getDanhSachLuaChon().isEmpty()) {
                        psLuaChon = conn.prepareStatement(sqlLuaChon);
                        for (LuaChonCauHoi luaChon : cauHoi.getDanhSachLuaChon()) {
                            luaChon.setMaCauHoi(maCauHoiMoi); // Đặt MaCauHoi cho lựa chọn
                            psLuaChon.setInt(1, luaChon.getMaCauHoi());
                            psLuaChon.setString(2, luaChon.getKyTuLuaChon());
                            psLuaChon.setString(3, luaChon.getNoiDungLuaChon());
                            psLuaChon.setBoolean(4, luaChon.isLaDapAnDung());
                            psLuaChon.addBatch(); // Thêm vào batch để thực thi một lần
                        }
                        psLuaChon.executeBatch(); // Thực thi batch các lựa chọn
                    }
                }
                conn.commit(); // Hoàn tất transaction thành công
                success = true;
            } else {
                conn.rollback(); // Nếu không thêm được câu hỏi thì rollback
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm câu hỏi: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu có lỗi
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Đóng các resources
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psLuaChon != null) psLuaChon.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psCauHoi != null) psCauHoi.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Không đóng connection ở đây nếu nó được quản lý tập trung bởi DatabaseManager (Singleton)
            // Chỉ đóng khi ứng dụng tắt hoặc khi bạn chắc chắn không dùng nữa.
            // Để đơn giản, chúng ta có thể không setAutoCommit(true) lại ở đây nếu kết nối được dùng lại.
            // Tuy nhiên, thực hành tốt là trả lại trạng thái autoCommit.
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    // Phương thức lấy một câu hỏi theo MaCauHoi, bao gồm cả các lựa chọn của nó
    public CauHoi layCauHoiTheoId(int maCauHoi) {
        String sqlCauHoi = "SELECT * FROM CAU_HOI WHERE MaCauHoi = ?";
        String sqlLuaChon = "SELECT * FROM LUA_CHON_CAU_HOI WHERE MaCauHoi = ?";
        CauHoi cauHoi = null;
        Connection conn = null;
        PreparedStatement psCauHoi = null;
        PreparedStatement psLuaChon = null;
        ResultSet rsCauHoi = null;
        ResultSet rsLuaChon = null;

        try {
            conn = DatabaseManager.getConnection();
            psCauHoi = conn.prepareStatement(sqlCauHoi);
            psCauHoi.setInt(1, maCauHoi);
            rsCauHoi = psCauHoi.executeQuery();

            if (rsCauHoi.next()) {
                cauHoi = new CauHoi();
                cauHoi.setMaCauHoi(rsCauHoi.getInt("MaCauHoi"));
                cauHoi.setNoiDungCauHoi(rsCauHoi.getString("NoiDungCauHoi"));
                cauHoi.setLoaiCauHoi(rsCauHoi.getString("LoaiCauHoi"));
                cauHoi.setDoKho(rsCauHoi.getString("DoKho"));
                cauHoi.setKhoaDapAnDung(rsCauHoi.getString("KhoaDapAnDung"));
                cauHoi.setGiaiThichDapAn(rsCauHoi.getString("GiaiThichDapAn"));
                cauHoi.setDapAnGoiYTuAI(rsCauHoi.getString("DapAnGoiYTuAI"));
                cauHoi.setDuongDanFileAmThanh(rsCauHoi.getString("DuongDanFileAmThanh"));
                cauHoi.setNhan(rsCauHoi.getString("Nhan"));
                Timestamp ngayTaoTs = rsCauHoi.getTimestamp("NgayTao");
                if (ngayTaoTs != null) {
                    cauHoi.setNgayTao(ngayTaoTs.toLocalDateTime());
                }
                Timestamp ngayCapNhatTs = rsCauHoi.getTimestamp("NgayCapNhat");
                if (ngayCapNhatTs != null) {
                    cauHoi.setNgayCapNhat(ngayCapNhatTs.toLocalDateTime());
                }

                // Lấy các lựa chọn cho câu hỏi này
                psLuaChon = conn.prepareStatement(sqlLuaChon);
                psLuaChon.setInt(1, maCauHoi);
                rsLuaChon = psLuaChon.executeQuery();
                List<LuaChonCauHoi> danhSachLuaChon = new ArrayList<>();
                while (rsLuaChon.next()) {
                    LuaChonCauHoi luaChon = new LuaChonCauHoi();
                    luaChon.setMaLuaChon(rsLuaChon.getInt("MaLuaChon"));
                    luaChon.setMaCauHoi(rsLuaChon.getInt("MaCauHoi"));
                    luaChon.setKyTuLuaChon(rsLuaChon.getString("KyTuLuaChon"));
                    luaChon.setNoiDungLuaChon(rsLuaChon.getString("NoiDungLuaChon"));
                    luaChon.setLaDapAnDung(rsLuaChon.getBoolean("LaDapAnDung"));
                    danhSachLuaChon.add(luaChon);
                }
                cauHoi.setDanhSachLuaChon(danhSachLuaChon);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy câu hỏi theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rsLuaChon != null) rsLuaChon.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psLuaChon != null) psLuaChon.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (rsCauHoi != null) rsCauHoi.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psCauHoi != null) psCauHoi.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return cauHoi;
    }

    // Phương thức lấy tất cả các câu hỏi
    public List<CauHoi> layTatCaCauHoi() {
        String sql = "SELECT MaCauHoi FROM CAU_HOI"; // Chỉ lấy ID để tránh load nhiều dữ liệu không cần thiết ngay
        List<CauHoi> danhSachCauHoi = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                // Với mỗi MaCauHoi, gọi lại layCauHoiTheoId để lấy đầy đủ thông tin và lựa chọn
                // Điều này có thể không hiệu quả nếu có RẤT NHIỀU câu hỏi (N+1 problem)
                // Một cách khác là JOIN các bảng lại, nhưng sẽ phức tạp hơn trong việc map ResultSet
                CauHoi ch = layCauHoiTheoId(rs.getInt("MaCauHoi"));
                if (ch != null) {
                    danhSachCauHoi.add(ch);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả câu hỏi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return danhSachCauHoi;
    }
    
    // Phương thức cập nhật một câu hỏi
    // Cần xử lý cả việc cập nhật, thêm mới, hoặc xóa các lựa chọn cũ
    public boolean capNhatCauHoi(CauHoi cauHoi) {
        String sqlUpdateCauHoi = "UPDATE CAU_HOI SET NoiDungCauHoi = ?, LoaiCauHoi = ?, DoKho = ?, KhoaDapAnDung = ?, GiaiThichDapAn = ?, DapAnGoiYTuAI = ?, DuongDanFileAmThanh = ?, Nhan = ?, NgayCapNhat = ? WHERE MaCauHoi = ?";
        String sqlDeleteLuaChonCu = "DELETE FROM LUA_CHON_CAU_HOI WHERE MaCauHoi = ?";
        String sqlInsertLuaChonMoi = "INSERT INTO LUA_CHON_CAU_HOI (MaCauHoi, KyTuLuaChon, NoiDungLuaChon, LaDapAnDung) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psUpdateCauHoi = null;
        PreparedStatement psDeleteLuaChon = null;
        PreparedStatement psInsertLuaChon = null;
        boolean success = false;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Cập nhật thông tin chính của câu hỏi
            psUpdateCauHoi = conn.prepareStatement(sqlUpdateCauHoi);
            psUpdateCauHoi.setString(1, cauHoi.getNoiDungCauHoi());
            psUpdateCauHoi.setString(2, cauHoi.getLoaiCauHoi());
            psUpdateCauHoi.setString(3, cauHoi.getDoKho());
            psUpdateCauHoi.setString(4, cauHoi.getKhoaDapAnDung());
            psUpdateCauHoi.setString(5, cauHoi.getGiaiThichDapAn());
            psUpdateCauHoi.setString(6, cauHoi.getDapAnGoiYTuAI());
            psUpdateCauHoi.setString(7, cauHoi.getDuongDanFileAmThanh());
            psUpdateCauHoi.setString(8, cauHoi.getNhan());
            cauHoi.setNgayCapNhat(LocalDateTime.now()); // Cập nhật thời gian
            psUpdateCauHoi.setTimestamp(9, Timestamp.valueOf(cauHoi.getNgayCapNhat()));
            psUpdateCauHoi.setInt(10, cauHoi.getMaCauHoi());
            int affectedRows = psUpdateCauHoi.executeUpdate();

            if (affectedRows > 0) {
                // 2. Xóa tất cả các lựa chọn cũ của câu hỏi này
                psDeleteLuaChon = conn.prepareStatement(sqlDeleteLuaChonCu);
                psDeleteLuaChon.setInt(1, cauHoi.getMaCauHoi());
                psDeleteLuaChon.executeUpdate(); // Không cần quan tâm số dòng bị xóa

                // 3. Thêm lại các lựa chọn mới (nếu có)
                if (cauHoi.getDanhSachLuaChon() != null && !cauHoi.getDanhSachLuaChon().isEmpty()) {
                    psInsertLuaChon = conn.prepareStatement(sqlInsertLuaChonMoi);
                    for (LuaChonCauHoi luaChon : cauHoi.getDanhSachLuaChon()) {
                        luaChon.setMaCauHoi(cauHoi.getMaCauHoi()); // Đảm bảo MaCauHoi đúng
                        psInsertLuaChon.setInt(1, luaChon.getMaCauHoi());
                        psInsertLuaChon.setString(2, luaChon.getKyTuLuaChon());
                        psInsertLuaChon.setString(3, luaChon.getNoiDungLuaChon());
                        psInsertLuaChon.setBoolean(4, luaChon.isLaDapAnDung());
                        psInsertLuaChon.addBatch();
                    }
                    psInsertLuaChon.executeBatch();
                }
                conn.commit();
                success = true;
            } else {
                conn.rollback(); // Nếu không cập nhật được câu hỏi
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật câu hỏi: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try { if (psInsertLuaChon != null) psInsertLuaChon.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psDeleteLuaChon != null) psDeleteLuaChon.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (psUpdateCauHoi != null) psUpdateCauHoi.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    // Phương thức xóa một câu hỏi (sẽ tự động xóa các lựa chọn liên quan do ON DELETE CASCADE)
    public boolean xoaCauHoi(int maCauHoi) {
        String sql = "DELETE FROM CAU_HOI WHERE MaCauHoi = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DatabaseManager.getConnection();
            // Không cần transaction phức tạp ở đây vì ON DELETE CASCADE sẽ xử lý LUA_CHON_CAU_HOI
            // Tuy nhiên, nếu có các bảng khác liên quan không có CASCADE, bạn cần transaction.
            // Cũng cần lưu ý bảng CAU_HOI_TRONG_DE_THI cũng có ON DELETE CASCADE.
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maCauHoi);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                success = true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa câu hỏi: " + e.getMessage());
            e.printStackTrace();
            // Nếu có lỗi khóa ngoại từ bảng CAU_HOI_TRONG_DE_THI mà không có ON DELETE CASCADE,
            // thì việc xóa sẽ thất bại. Schema của bạn đã có ON DELETE CASCADE nên ổn.
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }
 // Thêm phương thức này vào lớp CauHoiDAO hiện có

    public List<CauHoi> layTatCaCauHoiTrongDeThi(int maDeThi) {
        String sql = "SELECT ch.* FROM CAU_HOI ch " +
                     "JOIN CAU_HOI_TRONG_DE_THI chdt ON ch.MaCauHoi = chdt.MaCauHoi " +
                     "WHERE chdt.MaDeThi = ? " +
                     "ORDER BY chdt.ThuTuTrongDeThi ASC";
        List<CauHoi> danhSachCauHoi = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maDeThi);
            rs = ps.executeQuery();

            while (rs.next()) {
                int maCauHoi = rs.getInt("MaCauHoi");
                // Sử dụng phương thức đã có để lấy chi tiết câu hỏi
                CauHoi cauHoi = layCauHoiTheoId(maCauHoi);
                if (cauHoi != null) {
                    danhSachCauHoi.add(cauHoi);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy câu hỏi trong đề thi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return danhSachCauHoi;
    }
    // PHƯƠNG THỨC MỚI ĐỂ TÌM KIẾM CÂU HỎI
    public List<CauHoi> timKiemCauHoiTheoNoiDung(String tuKhoa) {
        List<CauHoi> danhSachKetQua = new ArrayList<>();
        // Câu SQL tìm kiếm trên các trường NoiDungCauHoi, LoaiCauHoi, DoKho, Nhan (Tags)
        String sql = "SELECT MaCauHoi FROM CAU_HOI " + // Chỉ lấy MaCauHoi để tối ưu, sau đó gọi layCauHoiTheoId
                     "WHERE NoiDungCauHoi LIKE ? " +
                     "OR LoaiCauHoi LIKE ? " +
                     "OR DoKho LIKE ? " +
                     "OR Nhan LIKE ? " + // Giả sử Nhan đóng vai trò như tags
                     "OR GiaiThichDapAn LIKE ?"; // Có thể tìm cả trong giải thích

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);

            String likePattern = "%" + tuKhoa.toLowerCase() + "%"; // Chuyển từ khóa về chữ thường nếu CSDL của bạn case-sensitive
                                                                 // và dữ liệu cũng nên được chuẩn hóa hoặc dùng LOWER() trong SQL

            pstmt.setString(1, likePattern); // Tìm trong Nội dung Câu Hỏi
            pstmt.setString(2, likePattern); // Tìm trong Loại Câu Hỏi
            pstmt.setString(3, likePattern); // Tìm trong Độ Khó
            pstmt.setString(4, likePattern); // Tìm trong Nhãn (Tags)
            pstmt.setString(5, likePattern); // Tìm trong Giải thích đáp án


            rs = pstmt.executeQuery();

            while (rs.next()) {
                // Lấy đầy đủ thông tin câu hỏi bằng ID
                // Đây là cách tiếp cận N+1, nhưng nhất quán với phương thức layTatCaCauHoi của bạn
                CauHoi cauHoi = layCauHoiTheoId(rs.getInt("MaCauHoi"));
                if (cauHoi != null) {
                    danhSachKetQua.add(cauHoi);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm câu hỏi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Không đóng connection ở đây nếu DatabaseManager quản lý
        }
        return danhSachKetQua;
    }
    // (Tùy chọn) Bạn có thể thêm các phương thức tìm kiếm câu hỏi theo tiêu chí khác nhau
    // ví dụ: tìm theo Độ Khó, Loại Câu Hỏi, Nhãn (Tags), v.v.
}