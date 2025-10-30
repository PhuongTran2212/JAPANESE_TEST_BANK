// Đặt trong package com.tiengnhat hoặc com.tiengnhat.test
// package com.tiengnhat; // Hoặc package com.tiengnhat.test
package com.tiengnhat.test;
import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.dao.DeThiDAO;
import com.tiengnhat.db.DatabaseManager;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.DeThi;
import com.tiengnhat.model.LuaChonCauHoi;

import java.util.List;

public class MainTestDAO {
    public static void main(String[] args) {
        CauHoiDAO cauHoiDAO = new CauHoiDAO();
        DeThiDAO deThiDAO = new DeThiDAO();

        // --- Test CauHoiDAO ---
        System.out.println("--- Bắt đầu Test CauHoiDAO ---");
        // 1. Thêm câu hỏi mới
        CauHoi chMoi = new CauHoi("Câu hỏi trắc nghiệm ví dụ 1?", "Trắc nghiệm", "N5", "A", "Giải thích A đúng.", null, "tag1, tag2");
        chMoi.themLuaChon(new LuaChonCauHoi("A", "Lựa chọn A", true));
        chMoi.themLuaChon(new LuaChonCauHoi("B", "Lựa chọn B", false));
        chMoi.themLuaChon(new LuaChonCauHoi("C", "Lựa chọn C", false));
        
        boolean themCHThanhCong = cauHoiDAO.themCauHoi(chMoi);
        System.out.println("Thêm câu hỏi mới thành công: " + themCHThanhCong + ", ID: " + chMoi.getMaCauHoi());

        CauHoi chMoi2 = new CauHoi("Câu hỏi điền khuyết ví dụ 2?", "Điền khuyết", "N4", "Đáp án điền", "Giải thích cho điền khuyết.", null, "dien-khuyet, N4");
        boolean themCH2ThanhCong = cauHoiDAO.themCauHoi(chMoi2);
        System.out.println("Thêm câu hỏi mới 2 thành công: " + themCH2ThanhCong + ", ID: " + chMoi2.getMaCauHoi());


        // 2. Lấy câu hỏi theo ID
        if (themCHThanhCong) {
            CauHoi chLayRa = cauHoiDAO.layCauHoiTheoId(chMoi.getMaCauHoi());
            if (chLayRa != null) {
                System.out.println("Lấy câu hỏi ID " + chMoi.getMaCauHoi() + ": " + chLayRa.getNoiDungCauHoi());
                System.out.println("Số lựa chọn: " + chLayRa.getDanhSachLuaChon().size());
                for(LuaChonCauHoi lc : chLayRa.getDanhSachLuaChon()){
                    System.out.println("  - " + lc.getKyTuLuaChon() + ": " + lc.getNoiDungLuaChon() + (lc.isLaDapAnDung() ? " (Đúng)" : ""));
                }
            }
        }

        // 3. Lấy tất cả câu hỏi
        List<CauHoi> dsCauHoi = cauHoiDAO.layTatCaCauHoi();
        System.out.println("Tổng số câu hỏi hiện có: " + dsCauHoi.size());
        for (CauHoi ch : dsCauHoi) {
             System.out.println("  ID: " + ch.getMaCauHoi() + " - " + ch.getNoiDungCauHoi().substring(0, Math.min(ch.getNoiDungCauHoi().length(), 30)) + "...");
        }

        // 4. Cập nhật câu hỏi
        if (themCHThanhCong) {
            chMoi.setNoiDungCauHoi("Câu hỏi trắc nghiệm ví dụ 1 ĐÃ CẬP NHẬT?");
            chMoi.setKhoaDapAnDung("B"); // Thay đổi đáp án đúng
            // Cập nhật lựa chọn
            chMoi.getDanhSachLuaChon().clear();
            chMoi.themLuaChon(new LuaChonCauHoi("A", "Lựa chọn A mới", false));
            chMoi.themLuaChon(new LuaChonCauHoi("B", "Lựa chọn B mới", true));
            chMoi.themLuaChon(new LuaChonCauHoi("C", "Lựa chọn C mới", false));
            
            boolean capNhatCHThanhCong = cauHoiDAO.capNhatCauHoi(chMoi);
            System.out.println("Cập nhật câu hỏi ID " + chMoi.getMaCauHoi() + " thành công: " + capNhatCHThanhCong);
            
            CauHoi chSauCapNhat = cauHoiDAO.layCauHoiTheoId(chMoi.getMaCauHoi());
             if (chSauCapNhat != null) {
                System.out.println("Nội dung sau cập nhật: " + chSauCapNhat.getNoiDungCauHoi());
                System.out.println("Đáp án đúng sau cập nhật: " + chSauCapNhat.getKhoaDapAnDung());
                System.out.println("Ngày cập nhật: " + chSauCapNhat.getNgayCapNhat());
                for(LuaChonCauHoi lc : chSauCapNhat.getDanhSachLuaChon()){
                    System.out.println("  - " + lc.getKyTuLuaChon() + ": " + lc.getNoiDungLuaChon() + (lc.isLaDapAnDung() ? " (Đúng)" : ""));
                }
            }
        }
        
        // --- Test DeThiDAO ---
        System.out.println("\n--- Bắt đầu Test DeThiDAO ---");
        DeThi dtMoi = new DeThi("Đề thi thử N5 số 1", "Mô tả cho đề thi N5", false, null);
        boolean themDTThanhCong = deThiDAO.themDeThi(dtMoi);
        System.out.println("Thêm đề thi mới thành công: " + themDTThanhCong + ", ID: " + dtMoi.getMaDeThi());

        if (themDTThanhCong && themCHThanhCong) { // Cần có câu hỏi để thêm vào đề
             // Thêm câu hỏi vào đề thi
            deThiDAO.themCauHoiVaoDeThi(dtMoi.getMaDeThi(), chMoi.getMaCauHoi(), 1);
            System.out.println("Đã thêm câu hỏi ID " + chMoi.getMaCauHoi() + " vào đề thi ID " + dtMoi.getMaDeThi());
            
            // Thử thêm lại câu hỏi đã có (sẽ báo lỗi unique key)
            System.out.println("Thử thêm lại câu hỏi ID " + chMoi.getMaCauHoi() + " vào đề thi ID " + dtMoi.getMaDeThi() + " lần nữa:");
            deThiDAO.themCauHoiVaoDeThi(dtMoi.getMaDeThi(), chMoi.getMaCauHoi(), 2);


            DeThi dtLayRa = deThiDAO.layDeThiTheoId(dtMoi.getMaDeThi());
            if (dtLayRa != null) {
                System.out.println("Lấy đề thi ID " + dtLayRa.getMaDeThi() + ": " + dtLayRa.getTenDeThi());
                System.out.println("Số câu hỏi trong đề: " + dtLayRa.getDanhSachCauHoiTrongDe().size());
                if(!dtLayRa.getDanhSachCauHoiTrongDe().isEmpty()){
                     System.out.println("  Câu hỏi 1: " + dtLayRa.getDanhSachCauHoiTrongDe().get(0).getNoiDungCauHoi());
                }
                System.out.println("Tổng số câu hỏi (cột TongSoCauHoi): " + dtLayRa.getTongSoCauHoi());
            }

            // Xóa câu hỏi khỏi đề thi
            deThiDAO.xoaCauHoiKhoiDeThi(dtMoi.getMaDeThi(), chMoi.getMaCauHoi());
            System.out.println("Đã xóa câu hỏi ID " + chMoi.getMaCauHoi() + " khỏi đề thi ID " + dtMoi.getMaDeThi());
            dtLayRa = deThiDAO.layDeThiTheoId(dtMoi.getMaDeThi());
            if (dtLayRa != null) {
                 System.out.println("Số câu hỏi trong đề sau khi xóa: " + dtLayRa.getDanhSachCauHoiTrongDe().size());
                 System.out.println("Tổng số câu hỏi (cột TongSoCauHoi) sau khi xóa: " + dtLayRa.getTongSoCauHoi());
            }
        }
        
        // 5. Xóa câu hỏi (cuối cùng để dọn dẹp)
        System.out.println("\n--- Dọn dẹp ---");
        if (themCHThanhCong) {
            boolean xoaCHThanhCong = cauHoiDAO.xoaCauHoi(chMoi.getMaCauHoi());
            System.out.println("Xóa câu hỏi ID " + chMoi.getMaCauHoi() + " thành công: " + xoaCHThanhCong);
        }
         if (themCH2ThanhCong) {
            boolean xoaCH2ThanhCong = cauHoiDAO.xoaCauHoi(chMoi2.getMaCauHoi());
            System.out.println("Xóa câu hỏi ID " + chMoi2.getMaCauHoi() + " thành công: " + xoaCH2ThanhCong);
        }
        // Xóa đề thi
        if (themDTThanhCong) {
            boolean xoaDTThanhCong = deThiDAO.xoaDeThi(dtMoi.getMaDeThi());
            System.out.println("Xóa đề thi ID " + dtMoi.getMaDeThi() + " thành công: " + xoaDTThanhCong);
        }


        DatabaseManager.closeConnection(); // Đóng kết nối khi test xong
        System.out.println("--- Kết thúc Test ---");
    }
}