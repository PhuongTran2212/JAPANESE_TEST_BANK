package com.tiengnhat.ui;

import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.dao.DeThiDAO;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.CauHoiTrongDeThi;
import com.tiengnhat.model.DeThi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TaoDeThiTuDongDialog extends JDialog {
    private DeThiDAO deThiDAO;
    private CauHoiDAO cauHoiDAO;
    private boolean deThiCreated = false;

    private JTextField txtTenDeThi;
    private JTextArea txtMoTa;
    private JCheckBox chkChoPhepTronCauHoi;
    
    // Số lượng câu hỏi theo loại
    private JSpinner spnTracNghiem;
    private JSpinner spnDienTu;
    private JSpinner spnNgheHieu;
    private JSpinner spnDocHieu;
    private JSpinner spnKanji;
    private JSpinner spnNguPhap;
    private JSpinner spnHoiThoai;
    
    // Độ khó
    private JComboBox<String> cmbDoKho;
    
    private JButton btnTaoDeThi;
    private JButton btnHuy;

    public TaoDeThiTuDongDialog(Frame owner, DeThiDAO deThiDAO) {
        super(owner, "Tạo Đề Thi Tự Động", true);
        this.deThiDAO = deThiDAO;
        this.cauHoiDAO = new CauHoiDAO();

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Thông tin đề thi ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(new TitledBorder("Thông tin đề thi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên đề thi
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(new JLabel("Tên đề thi:"), gbc);
        txtTenDeThi = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        infoPanel.add(txtTenDeThi, gbc);

        // Mô tả
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Mô tả:"), gbc);
        txtMoTa = new JTextArea(3, 30);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        JScrollPane scrollMoTa = new JScrollPane(txtMoTa);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        infoPanel.add(scrollMoTa, gbc);

        // Cho phép trộn câu hỏi
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Trộn câu hỏi:"), gbc);
        chkChoPhepTronCauHoi = new JCheckBox("Cho phép trộn câu hỏi khi thi");
        chkChoPhepTronCauHoi.setSelected(true);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        infoPanel.add(chkChoPhepTronCauHoi, gbc);

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Cấu hình số lượng câu hỏi ---
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("Cấu hình số lượng câu hỏi"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Trắc nghiệm
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        configPanel.add(new JLabel("Trắc nghiệm:"), gbc);
        spnTracNghiem = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        configPanel.add(spnTracNghiem, gbc);

        // Điền từ
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Điền từ:"), gbc);
        spnDienTu = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        configPanel.add(spnDienTu, gbc);

        // Nghe hiểu
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Nghe hiểu:"), gbc);
        spnNgheHieu = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        configPanel.add(spnNgheHieu, gbc);

        // Đọc hiểu
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Đọc hiểu:"), gbc);
        spnDocHieu = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        configPanel.add(spnDocHieu, gbc);

        // Kanji
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Kanji:"), gbc);
        spnKanji = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        configPanel.add(spnKanji, gbc);

        // Ngữ pháp
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Ngữ pháp:"), gbc);
        spnNguPhap = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        configPanel.add(spnNguPhap, gbc);

        // Hội thoại
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Hội thoại:"), gbc);
        spnHoiThoai = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        configPanel.add(spnHoiThoai, gbc);

        // Độ khó
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Độ khó:"), gbc);
        cmbDoKho = new JComboBox<>(new String[]{"Tất cả", "N5", "N4", "N3", "N2", "N1"});
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 1.0;
        configPanel.add(cmbDoKho, gbc);

        mainPanel.add(configPanel);

        // --- Panel nút Tạo và Hủy ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnTaoDeThi = new JButton("Tạo Đề Thi");
        btnHuy = new JButton("Hủy");

        btnTaoDeThi.addActionListener(e -> taoDeThiTuDong());
        btnHuy.addActionListener(e -> {
            deThiCreated = false;
            dispose();
        });

        bottomButtonPanel.add(btnTaoDeThi);
        bottomButtonPanel.add(btnHuy);

        // Thêm các panel vào dialog
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void taoDeThiTuDong() {
        if (txtTenDeThi.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đề thi không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTenDeThi.requestFocus();
            return;
        }
        
        // Lấy số lượng câu hỏi theo loại
        int soTracNghiem = (int) spnTracNghiem.getValue();
        int soDienTu = (int) spnDienTu.getValue();
        int soNgheHieu = (int) spnNgheHieu.getValue();
        int soDocHieu = (int) spnDocHieu.getValue();
        int soKanji = (int) spnKanji.getValue();
        int soNguPhap = (int) spnNguPhap.getValue();
        int soHoiThoai = (int) spnHoiThoai.getValue();
        
        // Lấy độ khó
        String doKho = cmbDoKho.getSelectedItem().toString();
        
        // Tạo đề thi mới
        DeThi deThi = new DeThi();
        deThi.setTenDeThi(txtTenDeThi.getText().trim());
        deThi.setMoTa(txtMoTa.getText().trim());
        deThi.setChoPhepTronCauHoi(chkChoPhepTronCauHoi.isSelected());
        
        // Lấy danh sách câu hỏi từ DAO
        List<CauHoi> tatCaCauHoi = cauHoiDAO.layTatCaCauHoi();
        
        // Lọc câu hỏi theo độ khó nếu cần
        List<CauHoi> cauHoiLoc = new ArrayList<>();
        if (!"Tất cả".equals(doKho)) {
            for (CauHoi cauHoi : tatCaCauHoi) {
                if (doKho.equals(cauHoi.getDoKho())) {
                    cauHoiLoc.add(cauHoi);
                }
            }
        } else {
            cauHoiLoc.addAll(tatCaCauHoi);
        }
        
        // Phân loại câu hỏi theo loại
        List<CauHoi> tracNghiem = new ArrayList<>();
        List<CauHoi> dienTu = new ArrayList<>();
        List<CauHoi> ngheHieu = new ArrayList<>();
        List<CauHoi> docHieu = new ArrayList<>();
        List<CauHoi> kanji = new ArrayList<>();
        List<CauHoi> nguPhap = new ArrayList<>();
        List<CauHoi> hoiThoai = new ArrayList<>();
        
        for (CauHoi cauHoi : cauHoiLoc) {
            String loai = cauHoi.getLoaiCauHoi();
            if ("Trắc nghiệm".equals(loai)) {
                tracNghiem.add(cauHoi);
            } else if ("Điền từ".equals(loai)) {
                dienTu.add(cauHoi);
            } else if ("Nghe hiểu".equals(loai)) {
                ngheHieu.add(cauHoi);
            } else if ("Đọc hiểu".equals(loai)) {
                docHieu.add(cauHoi);
            } else if ("Kanji".equals(loai)) {
                kanji.add(cauHoi);
            } else if ("Ngữ pháp".equals(loai)) {
                nguPhap.add(cauHoi);
            } else if ("Hội thoại".equals(loai)) {
                hoiThoai.add(cauHoi);
            }
        }
        
        // Trộn ngẫu nhiên các danh sách câu hỏi
        Collections.shuffle(tracNghiem);
        Collections.shuffle(dienTu);
        Collections.shuffle(ngheHieu);
        Collections.shuffle(docHieu);
        Collections.shuffle(kanji);
        Collections.shuffle(nguPhap);
        Collections.shuffle(hoiThoai);
        
     // Kiểm tra số lượng câu hỏi có đủ không
        StringBuilder thongBaoThieu = new StringBuilder();
        if (soTracNghiem > tracNghiem.size()) {
            thongBaoThieu.append("- Trắc nghiệm: Yêu cầu " + soTracNghiem + ", có sẵn " + tracNghiem.size() + "\n");
        }
        if (soDienTu > dienTu.size()) {
            thongBaoThieu.append("- Điền từ: Yêu cầu " + soDienTu + ", có sẵn " + dienTu.size() + "\n");
        }
        if (soNgheHieu > ngheHieu.size()) {
            thongBaoThieu.append("- Nghe hiểu: Yêu cầu " + soNgheHieu + ", có sẵn " + ngheHieu.size() + "\n");
        }
        if (soDocHieu > docHieu.size()) {
            thongBaoThieu.append("- Đọc hiểu: Yêu cầu " + soDocHieu + ", có sẵn " + docHieu.size() + "\n");
        }
        if (soKanji > kanji.size()) {
            thongBaoThieu.append("- Kanji: Yêu cầu " + soKanji + ", có sẵn " + kanji.size() + "\n");
        }
        if (soNguPhap > nguPhap.size()) {
            thongBaoThieu.append("- Ngữ pháp: Yêu cầu " + soNguPhap + ", có sẵn " + nguPhap.size() + "\n");
        }
        if (soHoiThoai > hoiThoai.size()) {
            thongBaoThieu.append("- Hội thoại: Yêu cầu " + soHoiThoai + ", có sẵn " + hoiThoai.size() + "\n");
        }
        
        if (thongBaoThieu.length() > 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Số lượng câu hỏi có sẵn không đủ cho một số loại:\n" + thongBaoThieu.toString() +
                    "Bạn có muốn tiếp tục với số lượng câu hỏi có sẵn?",
                    "Thiếu câu hỏi",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Lấy số lượng câu hỏi thực tế
        soTracNghiem = Math.min(soTracNghiem, tracNghiem.size());
        soDienTu = Math.min(soDienTu, dienTu.size());
        soNgheHieu = Math.min(soNgheHieu, ngheHieu.size());
        soDocHieu = Math.min(soDocHieu, docHieu.size());
        soKanji = Math.min(soKanji, kanji.size());
        soNguPhap = Math.min(soNguPhap, nguPhap.size());
        soHoiThoai = Math.min(soHoiThoai, hoiThoai.size());
        
        // Tạo danh sách câu hỏi cho đề thi
        List<CauHoi> danhSachCauHoi = new ArrayList<>();
        List<CauHoiTrongDeThi> danhSachChiTiet = new ArrayList<>();
        
        // Thêm câu hỏi vào danh sách
        int thuTu = 1;
        
        // Thêm câu hỏi trắc nghiệm
        for (int i = 0; i < soTracNghiem; i++) {
            CauHoi cauHoi = tracNghiem.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Thêm câu hỏi điền từ
        for (int i = 0; i < soDienTu; i++) {
            CauHoi cauHoi = dienTu.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Thêm câu hỏi nghe hiểu
        for (int i = 0; i < soNgheHieu; i++) {
            CauHoi cauHoi = ngheHieu.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Thêm câu hỏi đọc hiểu
        for (int i = 0; i < soDocHieu; i++) {
            CauHoi cauHoi = docHieu.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Thêm câu hỏi kanji
        for (int i = 0; i < soKanji; i++) {
            CauHoi cauHoi = kanji.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Thêm câu hỏi ngữ pháp
        for (int i = 0; i < soNguPhap; i++) {
            CauHoi cauHoi = nguPhap.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Thêm câu hỏi hội thoại
        for (int i = 0; i < soHoiThoai; i++) {
            CauHoi cauHoi = hoiThoai.get(i);
            danhSachCauHoi.add(cauHoi);
            
            CauHoiTrongDeThi chiTiet = new CauHoiTrongDeThi();
            chiTiet.setMaCauHoi(cauHoi.getMaCauHoi());
            chiTiet.setThuTuTrongDeThi(thuTu++);
            danhSachChiTiet.add(chiTiet);
        }
        
        // Cập nhật tổng số câu hỏi
        deThi.setTongSoCauHoi(danhSachCauHoi.size());
        deThi.setDanhSachCauHoiTrongDe(danhSachCauHoi);
        deThi.setChiTietCauHoiTrongDe(danhSachChiTiet);
        
        // Lưu đề thi vào CSDL
        boolean result = deThiDAO.themDeThi(deThi);
        if (result) {
            // Thêm câu hỏi vào đề thi
            for (CauHoiTrongDeThi chiTiet : danhSachChiTiet) {
                chiTiet.setMaDeThi(deThi.getMaDeThi());
                deThiDAO.themCauHoiVaoDeThi(deThi.getMaDeThi(), chiTiet.getMaCauHoi(), chiTiet.getThuTuTrongDeThi());
            }
            
            deThiCreated = true;
            JOptionPane.showMessageDialog(this, 
                    "Tạo đề thi tự động thành công!\n" +
                    "Tổng số câu hỏi: " + deThi.getTongSoCauHoi(),
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Tạo đề thi tự động thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDeThiCreated() {
        return deThiCreated;
    }
}