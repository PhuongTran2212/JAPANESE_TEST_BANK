package com.tiengnhat.ui;

import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.dao.DeThiDAO;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.CauHoiTrongDeThi;
import com.tiengnhat.model.DeThi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DeThiDialog extends JDialog {
    private DeThi deThi;
    private DeThiDAO deThiDAO;
    private CauHoiDAO cauHoiDAO;
    private boolean saved = false;

    private JTextField txtTenDeThi;
    private JTextArea txtMoTa;
    private JCheckBox chkChoPhepTronCauHoi;
    private JLabel lblTongSoCauHoi;

    // Bảng câu hỏi đã chọn
    private JTable tblCauHoiDaChon;
    private DefaultTableModel modelCauHoiDaChon;

    // Bảng câu hỏi có sẵn
    private JTable tblCauHoiCoSan;
    private DefaultTableModel modelCauHoiCoSan;

    // Các nút điều khiển
    private JButton btnThemCauHoi;
    private JButton btnXoaCauHoi;
    private JButton btnLenTren;
    private JButton btnXuongDuoi;
    private JButton btnLuu;
    private JButton btnHuy;

    // Bộ lọc
    private JComboBox<String> cmbLocLoaiCauHoi;
    private JComboBox<String> cmbLocDoKho;
    private JButton btnLocCauHoi;

    public DeThiDialog(Frame owner, String title, DeThi deThi, DeThiDAO deThiDAO) {
        super(owner, title, true);
        this.deThi = (deThi == null) ? new DeThi() : deThi;
        this.deThiDAO = deThiDAO;
        this.cauHoiDAO = new CauHoiDAO();

        setSize(1000, 800);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        if (this.deThi.getMaDeThi() != 0) {
            populateForm();
        }
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
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        infoPanel.add(chkChoPhepTronCauHoi, gbc);

        // Tổng số câu hỏi
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Tổng số câu hỏi:"), gbc);
        lblTongSoCauHoi = new JLabel("0");
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        infoPanel.add(lblTongSoCauHoi, gbc);

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Panel chọn câu hỏi ---
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(new TitledBorder("Chọn câu hỏi cho đề thi"));

        // Panel bên trái: Câu hỏi có sẵn
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("Ngân hàng câu hỏi"));

        // Panel lọc câu hỏi
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Loại câu hỏi:"));
        cmbLocLoaiCauHoi = new JComboBox<>(new String[]{"Tất cả", "Trắc nghiệm", "Điền từ", "Nghe hiểu", "Đọc hiểu", "Kanji", "Ngữ pháp", "Hội thoại"});
        filterPanel.add(cmbLocLoaiCauHoi);
        
        filterPanel.add(new JLabel("Độ khó:"));
        cmbLocDoKho = new JComboBox<>(new String[]{"Tất cả", "N5", "N4", "N3", "N2", "N1"});
        filterPanel.add(cmbLocDoKho);
        
        btnLocCauHoi = new JButton("Lọc");
        btnLocCauHoi.addActionListener(e -> locCauHoi());
        filterPanel.add(btnLocCauHoi);
        
        leftPanel.add(filterPanel, BorderLayout.NORTH);

        // Bảng câu hỏi có sẵn
        String[] colNamesCauHoiCoSan = {"ID", "Nội dung câu hỏi", "Loại", "Độ khó"};
        modelCauHoiCoSan = new DefaultTableModel(colNamesCauHoiCoSan, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCauHoiCoSan = new JTable(modelCauHoiCoSan);
        tblCauHoiCoSan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCauHoiCoSan.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblCauHoiCoSan.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblCauHoiCoSan.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblCauHoiCoSan.getColumnModel().getColumn(3).setPreferredWidth(80);
        JScrollPane scrollCauHoiCoSan = new JScrollPane(tblCauHoiCoSan);
        leftPanel.add(scrollCauHoiCoSan, BorderLayout.CENTER);

        // Panel giữa: Các nút điều khiển
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBorder(new EmptyBorder(10, 5, 10, 5));

        btnThemCauHoi = createButtonWithIcon(">>", "/icons/right_arrow_16.png");
        btnXoaCauHoi = createButtonWithIcon("<<", "/icons/left_arrow_16.png");
        btnLenTren = createButtonWithIcon("↑", "/icons/up_arrow_16.png");
        btnXuongDuoi = createButtonWithIcon("↓", "/icons/down_arrow_16.png");

        middlePanel.add(Box.createVerticalGlue());
        middlePanel.add(btnThemCauHoi);
        middlePanel.add(Box.createVerticalStrut(10));
        middlePanel.add(btnXoaCauHoi);
        middlePanel.add(Box.createVerticalStrut(30));
        middlePanel.add(btnLenTren);
        middlePanel.add(Box.createVerticalStrut(10));
        middlePanel.add(btnXuongDuoi);
        middlePanel.add(Box.createVerticalGlue());

        // Panel bên phải: Câu hỏi đã chọn
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Câu hỏi đã chọn"));

        String[] colNamesCauHoiDaChon = {"STT", "ID", "Nội dung câu hỏi", "Loại", "Độ khó"};
        modelCauHoiDaChon = new DefaultTableModel(colNamesCauHoiDaChon, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCauHoiDaChon = new JTable(modelCauHoiDaChon);
        tblCauHoiDaChon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCauHoiDaChon.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblCauHoiDaChon.getColumnModel().getColumn(1).setPreferredWidth(50);
        tblCauHoiDaChon.getColumnModel().getColumn(2).setPreferredWidth(300);
        tblCauHoiDaChon.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblCauHoiDaChon.getColumnModel().getColumn(4).setPreferredWidth(80);
        JScrollPane scrollCauHoiDaChon = new JScrollPane(tblCauHoiDaChon);
        rightPanel.add(scrollCauHoiDaChon, BorderLayout.CENTER);

        // Thêm các panel vào panel chọn câu hỏi
        questionPanel.add(leftPanel, BorderLayout.WEST);
        questionPanel.add(middlePanel, BorderLayout.CENTER);
        questionPanel.add(rightPanel, BorderLayout.EAST);

        // Đặt kích thước tương đối cho các panel
        leftPanel.setPreferredSize(new Dimension(450, 400));
        rightPanel.setPreferredSize(new Dimension(450, 400));

        mainPanel.add(questionPanel);

        // --- Panel nút Lưu và Hủy ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnLuu = createButtonWithIcon("Lưu", "/icons/save_16.png");
        btnHuy = createButtonWithIcon("Hủy", "/icons/cancel_16.png");

        btnLuu.addActionListener(e -> luuThayDoi());
        btnHuy.addActionListener(e -> {
            saved = false;
            dispose();
        });

        bottomButtonPanel.add(btnLuu);
        bottomButtonPanel.add(btnHuy);

        // Thêm các panel vào dialog
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);

        // Xử lý sự kiện cho các nút
        btnThemCauHoi.addActionListener(e -> themCauHoiVaoDeThi());
        btnXoaCauHoi.addActionListener(e -> xoaCauHoiKhoiDeThi());
        btnLenTren.addActionListener(e -> diChuyenCauHoiLenTren());
        btnXuongDuoi.addActionListener(e -> diChuyenCauHoiXuongDuoi());

        // Load dữ liệu câu hỏi có sẵn
        loadCauHoiCoSan();
    }

    private JButton createButtonWithIcon(String text, String iconPath) {
        JButton button = new JButton(text);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            button.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Không tìm thấy icon: " + iconPath);
        }
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(5);
        return button;
    }

    private void loadCauHoiCoSan() {
        // Xóa dữ liệu cũ
        while (modelCauHoiCoSan.getRowCount() > 0) {
            modelCauHoiCoSan.removeRow(0);
        }

        // Lấy danh sách câu hỏi từ DAO
        List<CauHoi> danhSachCauHoi = cauHoiDAO.layTatCaCauHoi();
        
        // Lọc câu hỏi nếu cần
        String loaiCauHoi = cmbLocLoaiCauHoi.getSelectedItem().toString();
        String doKho = cmbLocDoKho.getSelectedItem().toString();
        
        for (CauHoi cauHoi : danhSachCauHoi) {
            // Kiểm tra xem câu hỏi đã có trong đề thi chưa
            boolean daTonTai = false;
            for (int i = 0; i < modelCauHoiDaChon.getRowCount(); i++) {
                int maCauHoi = (int) modelCauHoiDaChon.getValueAt(i, 1);
                if (maCauHoi == cauHoi.getMaCauHoi()) {
                    daTonTai = true;
                    break;
                }
            }
            
            if (daTonTai) continue;
            
            // Lọc theo loại câu hỏi
            if (!"Tất cả".equals(loaiCauHoi) && !loaiCauHoi.equals(cauHoi.getLoaiCauHoi())) {
                continue;
            }
            
            // Lọc theo độ khó
            if (!"Tất cả".equals(doKho) && !doKho.equals(cauHoi.getDoKho())) {
                continue;
            }
            
            // Thêm vào bảng
            String noiDung = cauHoi.getNoiDungCauHoi();
            if (noiDung.length() > 100) {
                noiDung = noiDung.substring(0, 100) + "...";
            }
            
            modelCauHoiCoSan.addRow(new Object[]{
                cauHoi.getMaCauHoi(),
                noiDung,
                cauHoi.getLoaiCauHoi(),
                cauHoi.getDoKho()
            });
        }
    }

    private void locCauHoi() {
        loadCauHoiCoSan();
    }

    private void themCauHoiVaoDeThi() {
        int selectedRow = tblCauHoiCoSan.getSelectedRow();
        if (selectedRow >= 0) {
            int maCauHoi = (int) modelCauHoiCoSan.getValueAt(selectedRow, 0);
            String noiDung = (String) modelCauHoiCoSan.getValueAt(selectedRow, 1);
            String loai = (String) modelCauHoiCoSan.getValueAt(selectedRow, 2);
            String doKho = (String) modelCauHoiCoSan.getValueAt(selectedRow, 3);
            
            // Thêm vào bảng câu hỏi đã chọn
            int stt = modelCauHoiDaChon.getRowCount() + 1;
            modelCauHoiDaChon.addRow(new Object[]{
                stt,
                maCauHoi,
                noiDung,
                loai,
                doKho
            });
            
            // Xóa khỏi bảng câu hỏi có sẵn
            modelCauHoiCoSan.removeRow(selectedRow);
            
            // Cập nhật tổng số câu hỏi
            lblTongSoCauHoi.setText(String.valueOf(modelCauHoiDaChon.getRowCount()));
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để thêm vào đề thi.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xoaCauHoiKhoiDeThi() {
        int selectedRow = tblCauHoiDaChon.getSelectedRow();
        if (selectedRow >= 0) {
            int maCauHoi = (int) modelCauHoiDaChon.getValueAt(selectedRow, 1);
            String noiDung = (String) modelCauHoiDaChon.getValueAt(selectedRow, 2);
            String loai = (String) modelCauHoiDaChon.getValueAt(selectedRow, 3);
            String doKho = (String) modelCauHoiDaChon.getValueAt(selectedRow, 4);
            
            // Thêm lại vào bảng câu hỏi có sẵn
            modelCauHoiCoSan.addRow(new Object[]{
                maCauHoi,
                noiDung,
                loai,
                doKho
            });
            
            // Xóa khỏi bảng câu hỏi đã chọn
            modelCauHoiDaChon.removeRow(selectedRow);
            
            // Cập nhật lại STT
            for (int i = 0; i < modelCauHoiDaChon.getRowCount(); i++) {
                modelCauHoiDaChon.setValueAt(i + 1, i, 0);
            }
            
            // Cập nhật tổng số câu hỏi
            lblTongSoCauHoi.setText(String.valueOf(modelCauHoiDaChon.getRowCount()));
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa khỏi đề thi.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void diChuyenCauHoiLenTren() {
        int selectedRow = tblCauHoiDaChon.getSelectedRow();
        if (selectedRow > 0) {
            // Lưu dữ liệu dòng hiện tại
            Object[] currentRow = new Object[5];
            for (int i = 0; i < 5; i++) {
                currentRow[i] = modelCauHoiDaChon.getValueAt(selectedRow, i);
            }
            
            // Lưu dữ liệu dòng trên
            Object[] aboveRow = new Object[5];
            for (int i = 0; i < 5; i++) {
                aboveRow[i] = modelCauHoiDaChon.getValueAt(selectedRow - 1, i);
            }
            
            // Hoán đổi STT
            modelCauHoiDaChon.setValueAt(selectedRow, selectedRow - 1, 0);
            modelCauHoiDaChon.setValueAt(selectedRow + 1, selectedRow, 0);
            
            // Hoán đổi dữ liệu
            for (int i = 1; i < 5; i++) {
                modelCauHoiDaChon.setValueAt(currentRow[i], selectedRow - 1, i);
                modelCauHoiDaChon.setValueAt(aboveRow[i], selectedRow, i);
            }
            
            // Chọn dòng mới
            tblCauHoiDaChon.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        }
    }

    private void diChuyenCauHoiXuongDuoi() {
        int selectedRow = tblCauHoiDaChon.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < modelCauHoiDaChon.getRowCount() - 1) {
            // Lưu dữ liệu dòng hiện tại
            Object[] currentRow = new Object[5];
            for (int i = 0; i < 5; i++) {
                currentRow[i] = modelCauHoiDaChon.getValueAt(selectedRow, i);
            }
            
            // Lưu dữ liệu dòng dưới
            Object[] belowRow = new Object[5];
            for (int i = 0; i < 5; i++) {
                belowRow[i] = modelCauHoiDaChon.getValueAt(selectedRow + 1, i);
            }
            
            // Hoán đổi STT
            modelCauHoiDaChon.setValueAt(selectedRow + 2, selectedRow, 0);
            modelCauHoiDaChon.setValueAt(selectedRow + 1, selectedRow + 1, 0);
            
            // Hoán đổi dữ liệu
            for (int i = 1; i < 5; i++) {
                modelCauHoiDaChon.setValueAt(belowRow[i], selectedRow, i);
                modelCauHoiDaChon.setValueAt(currentRow[i], selectedRow + 1, i);
            }
            
            // Chọn dòng mới
            tblCauHoiDaChon.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
    }

    private void populateForm() {
        txtTenDeThi.setText(deThi.getTenDeThi());
        txtMoTa.setText(deThi.getMoTa());
        chkChoPhepTronCauHoi.setSelected(deThi.isChoPhepTronCauHoi());
        
        // Hiển thị tổng số câu hỏi
        if (deThi.getTongSoCauHoi() != null) {
            lblTongSoCauHoi.setText(deThi.getTongSoCauHoi().toString());
        } else {
            lblTongSoCauHoi.setText("0");
        }
        
        // Xóa dữ liệu cũ trong bảng câu hỏi đã chọn
        while (modelCauHoiDaChon.getRowCount() > 0) {
            modelCauHoiDaChon.removeRow(0);
        }
        
        // Thêm câu hỏi vào bảng câu hỏi đã chọn
        if (deThi.getDanhSachCauHoiTrongDe() != null) {
            List<CauHoi> danhSachCauHoi = deThi.getDanhSachCauHoiTrongDe();
            List<CauHoiTrongDeThi> chiTietCauHoi = deThi.getChiTietCauHoiTrongDe();
            
            // Sắp xếp theo thứ tự
            for (int i = 0; i < danhSachCauHoi.size(); i++) {
                CauHoi cauHoi = danhSachCauHoi.get(i);
                int thuTu = i + 1;
                
                // Tìm thứ tự từ chi tiết câu hỏi
                for (CauHoiTrongDeThi chiTiet : chiTietCauHoi) {
                    if (chiTiet.getMaCauHoi() == cauHoi.getMaCauHoi()) {
                        if (chiTiet.getThuTuTrongDeThi() != null) {
                            thuTu = chiTiet.getThuTuTrongDeThi();
                        }
                        break;
                    }
                }
                
                String noiDung = cauHoi.getNoiDungCauHoi();
                if (noiDung.length() > 100) {
                    noiDung = noiDung.substring(0, 100) + "...";
                }
                
                modelCauHoiDaChon.addRow(new Object[]{
                    thuTu,
                    cauHoi.getMaCauHoi(),
                    noiDung,
                    cauHoi.getLoaiCauHoi(),
                    cauHoi.getDoKho()
                });
            }
        }
        
        // Load lại câu hỏi có sẵn
        loadCauHoiCoSan();
    }

    private void luuThayDoi() {
        if (txtTenDeThi.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đề thi không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTenDeThi.requestFocus();
            return;
        }
        
        // Cập nhật thông tin đề thi
        deThi.setTenDeThi(txtTenDeThi.getText().trim());
        deThi.setMoTa(txtMoTa.getText().trim());
        deThi.setChoPhepTronCauHoi(chkChoPhepTronCauHoi.isSelected());
        deThi.setTongSoCauHoi(modelCauHoiDaChon.getRowCount());
        
        // Tạo danh sách câu hỏi trong đề thi
        List<CauHoiTrongDeThi> danhSachCauHoiTrongDe = new ArrayList<>();
        for (int i = 0; i < modelCauHoiDaChon.getRowCount(); i++) {
            int thuTu = (int) modelCauHoiDaChon.getValueAt(i, 0);
            int maCauHoi = (int) modelCauHoiDaChon.getValueAt(i, 1);
            
            CauHoiTrongDeThi cauHoiTrongDe = new CauHoiTrongDeThi();
            cauHoiTrongDe.setMaCauHoi(maCauHoi);
            cauHoiTrongDe.setThuTuTrongDeThi(thuTu);
            
            danhSachCauHoiTrongDe.add(cauHoiTrongDe);
        }
        
        boolean result;
        if (deThi.getMaDeThi() == 0) {
            // Thêm mới đề thi
            result = deThiDAO.themDeThi(deThi);
            if (result) {
                // Thêm câu hỏi vào đề thi
                for (CauHoiTrongDeThi cauHoiTrongDe : danhSachCauHoiTrongDe) {
                    cauHoiTrongDe.setMaDeThi(deThi.getMaDeThi());
                    deThiDAO.themCauHoiVaoDeThi(deThi.getMaDeThi(), cauHoiTrongDe.getMaCauHoi(), cauHoiTrongDe.getThuTuTrongDeThi());
                }
            }
        } else {
            // Cập nhật đề thi
            result = deThiDAO.capNhatDeThi(deThi);
            if (result) {
                // Cập nhật thứ tự câu hỏi trong đề thi
                result = deThiDAO.capNhatThuTuCauHoiTrongDeThi(deThi.getMaDeThi(), danhSachCauHoiTrongDe);
            }
        }
        
        if (result) {
            saved = true;
            JOptionPane.showMessageDialog(this, "Lưu đề thi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu đề thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}