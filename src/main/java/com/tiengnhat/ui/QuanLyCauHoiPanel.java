package com.tiengnhat.ui;

import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.model.CauHoi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class QuanLyCauHoiPanel extends JPanel {
    private JTable cauHoiTable;
    private CauHoiTableModel cauHoiTableModel;
    private CauHoiDAO cauHoiDAO;

    private JButton btnThemCauHoi;
    private JButton btnSuaCauHoi;
    private JButton btnXoaCauHoi;
    private JButton btnTaiLai;
    private JTextField txtTimKiem;
    private JButton btnTimKiem;
    private JComboBox<String> cmbLocLoaiCauHoi;
    private JComboBox<String> cmbLocDoKho;
    private JLabel lblTongSoCauHoi;

    private JFrame parentFrame;

    public QuanLyCauHoiPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        cauHoiDAO = new CauHoiDAO();
        initComponents();
        loadCauHoiData();
    }

    private void initComponents() {
        // --- Panel Tìm kiếm và Lọc ---
        JPanel searchFilterPanel = new JPanel(new BorderLayout(10, 0));
        searchFilterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tìm kiếm và Lọc"),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Panel Tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtTimKiem = new JTextField(25);
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemCauHoi();
                }
            }
        });
        
        btnTimKiem = createButtonWithIcon("Tìm", "/icons/search_16.png");
        searchPanel.add(txtTimKiem);
        searchPanel.add(btnTimKiem);
        
        // Panel Lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.add(new JLabel("Loại câu hỏi:"));
        cmbLocLoaiCauHoi = new JComboBox<>(new String[]{"Tất cả", "Trắc nghiệm", "Điền từ", "Nghe hiểu", "Đọc hiểu", "Kanji", "Ngữ pháp", "Hội thoại"});
        filterPanel.add(cmbLocLoaiCauHoi);
        
        filterPanel.add(new JLabel("Độ khó:"));
        cmbLocDoKho = new JComboBox<>(new String[]{"Tất cả", "N5", "N4", "N3", "N2", "N1"});
        filterPanel.add(cmbLocDoKho);
        
        JButton btnLoc = createButtonWithIcon("Lọc", "/icons/filter_16.png");
        btnLoc.addActionListener(e -> locCauHoi());
        filterPanel.add(btnLoc);
        
        // Thêm các panel vào searchFilterPanel
        searchFilterPanel.add(searchPanel, BorderLayout.WEST);
        searchFilterPanel.add(filterPanel, BorderLayout.EAST);
        
        // --- Panel Các nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        btnThemCauHoi = createButtonWithIcon("Thêm Câu Hỏi", "/icons/add_16.png");
        btnSuaCauHoi = createButtonWithIcon("Sửa Câu Hỏi", "/icons/edit_16.png");
        btnXoaCauHoi = createButtonWithIcon("Xóa Câu Hỏi", "/icons/delete_16.png");
        btnTaiLai = createButtonWithIcon("Tải Lại DS", "/icons/refresh_16.png");

        buttonPanel.add(btnThemCauHoi);
        buttonPanel.add(btnSuaCauHoi);
        buttonPanel.add(btnXoaCauHoi);
        buttonPanel.add(btnTaiLai);
        
        // Label hiển thị tổng số câu hỏi
        lblTongSoCauHoi = new JLabel("Tổng số: 0 câu hỏi");
        lblTongSoCauHoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongSoCauHoi.setBorder(new EmptyBorder(0, 20, 0, 0));
        buttonPanel.add(lblTongSoCauHoi);
        
        // Panel top chứa cả search và button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchFilterPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);

        // --- Bảng hiển thị câu hỏi ---
        cauHoiTableModel = new CauHoiTableModel(new ArrayList<>());
        cauHoiTable = new JTable(cauHoiTableModel);
        cauHoiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cauHoiTable.setFillsViewportHeight(true);
        cauHoiTable.setRowHeight(30);
        cauHoiTable.setIntercellSpacing(new Dimension(0, 1));
        cauHoiTable.setShowGrid(true);
        cauHoiTable.setGridColor(new Color(230, 230, 230));
        
        // Tùy chỉnh header của bảng
        JTableHeader header = cauHoiTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        setupTableColumns();

        // Thêm bảng vào một panel có border
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Danh sách câu hỏi"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(cauHoiTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);

        // --- Xử lý sự kiện ---
        btnThemCauHoi.addActionListener(e -> themCauHoi());
        btnSuaCauHoi.addActionListener(e -> suaCauHoi());
        btnXoaCauHoi.addActionListener(e -> xoaCauHoi());
        btnTaiLai.addActionListener(e -> loadCauHoiData());
        btnTimKiem.addActionListener(e -> timKiemCauHoi());
    }
    
    private void locCauHoi() {
        String loaiCauHoi = cmbLocLoaiCauHoi.getSelectedItem().toString();
        String doKho = cmbLocDoKho.getSelectedItem().toString();
        
        // Hiển thị indicator đang lọc
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Thực hiện lọc (giả định có phương thức lọc trong DAO)
        // Đây là mã giả, bạn cần thực hiện phương thức lọc thực tế
        SwingWorker<List<CauHoi>, Void> worker = new SwingWorker<List<CauHoi>, Void>() {
            @Override
            protected List<CauHoi> doInBackground() {
                List<CauHoi> allCauHoi = cauHoiDAO.layTatCaCauHoi();
                List<CauHoi> filteredList = new ArrayList<>();
                
                for (CauHoi ch : allCauHoi) {
                    boolean matchLoai = loaiCauHoi.equals("Tất cả") || ch.getLoaiCauHoi().equals(loaiCauHoi);
                    boolean matchDoKho = doKho.equals("Tất cả") || ch.getDoKho().equals(doKho);
                    
                    if (matchLoai && matchDoKho) {
                        filteredList.add(ch);
                    }
                }
                
                return filteredList;
            }
            
            @Override
            protected void done() {
                try {
                    List<CauHoi> result = get();
                    cauHoiTableModel.setData(result);
                    lblTongSoCauHoi.setText("Tổng số: " + result.size() + " câu hỏi");
                    
                    if (result.isEmpty()) {
                        JOptionPane.showMessageDialog(parentFrame, 
                            "Không tìm thấy câu hỏi nào phù hợp với điều kiện lọc.", 
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }

    private JButton createButtonWithIcon(String text, String iconPath) {
        JButton button = new JButton(text);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            button.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Không tìm thấy icon cho nút '" + text + "': " + iconPath);
        }
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(5);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return button;
    }

    private void setupTableColumns() {
        // Thiết lập độ rộng và căn chỉnh cho các cột
        cauHoiTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        cauHoiTable.getColumnModel().getColumn(0).setMaxWidth(100);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cauHoiTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        cauHoiTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        cauHoiTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        cauHoiTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        cauHoiTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        cauHoiTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        cauHoiTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        cauHoiTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        cauHoiTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        // Tùy chỉnh renderer cho cột nội dung câu hỏi để hiển thị tooltip
        cauHoiTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    jc.setToolTipText(value.toString());
                }
                return c;
            }
        });
    }

    public void loadCauHoiData() {
        // Hiển thị indicator đang tải
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Cập nhật status nếu parentFrame là MainFrame
        if (parentFrame instanceof MainFrame) {
            ((MainFrame) parentFrame).updateStatus("Đang tải dữ liệu câu hỏi...");
        }
        
        // Sử dụng SwingWorker để tải dữ liệu không chặn UI
        new SwingWorker<List<CauHoi>, Void>() {
            @Override
            protected List<CauHoi> doInBackground() throws Exception {
                return cauHoiDAO.layTatCaCauHoi();
            }

            @Override
            protected void done() {
                try {
                    List<CauHoi> danhSach = get();
                    cauHoiTableModel.setData(danhSach);
                    lblTongSoCauHoi.setText("Tổng số: " + danhSach.size() + " câu hỏi");
                    
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Đã tải " + danhSach.size() + " câu hỏi");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Lỗi tải dữ liệu câu hỏi: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Lỗi tải dữ liệu câu hỏi");
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void timKiemCauHoi() {
        String tuKhoa = txtTimKiem.getText().trim();
        if (tuKhoa.isEmpty()) {
            loadCauHoiData();
            return;
        }
        
        // Hiển thị indicator đang tìm kiếm
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (parentFrame instanceof MainFrame) {
            ((MainFrame) parentFrame).updateStatus("Đang tìm kiếm câu hỏi...");
        }
        
        new SwingWorker<List<CauHoi>, Void>() {
            @Override
            protected List<CauHoi> doInBackground() throws Exception {
                return cauHoiDAO.timKiemCauHoiTheoNoiDung(tuKhoa);
            }

            @Override
            protected void done() {
                try {
                    List<CauHoi> ketQua = get();
                    cauHoiTableModel.setData(ketQua);
                    lblTongSoCauHoi.setText("Tổng số: " + ketQua.size() + " câu hỏi");
                    
                    if (ketQua.isEmpty()){
                        JOptionPane.showMessageDialog(parentFrame, 
                            "Không tìm thấy câu hỏi nào với từ khóa: '" + tuKhoa + "'.", 
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Tìm thấy " + ketQua.size() + " câu hỏi với từ khóa: '" + tuKhoa + "'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Lỗi tìm kiếm câu hỏi: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Lỗi tìm kiếm câu hỏi");
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
    // Các phương thức khác giữ nguyên...
    private void themCauHoi() {
        CauHoiDialog themDialog = new CauHoiDialog(parentFrame, "Thêm Câu Hỏi Mới", null, cauHoiDAO);
        themDialog.setVisible(true);
        if (themDialog.isSaved()) {
            loadCauHoiData();
        }
    }

    private void suaCauHoi() {
        int selectedRow = cauHoiTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = cauHoiTable.convertRowIndexToModel(selectedRow);
            CauHoi cauHoiCanSua = cauHoiTableModel.getCauHoiAt(modelRow);
            if (cauHoiCanSua != null) {
                CauHoi cauHoiDayDu = cauHoiDAO.layCauHoiTheoId(cauHoiCanSua.getMaCauHoi());
                if (cauHoiDayDu != null) {
                    CauHoiDialog suaDialog = new CauHoiDialog(parentFrame, "Sửa Câu Hỏi", cauHoiDayDu, cauHoiDAO);
                    suaDialog.setVisible(true);
                    if (suaDialog.isSaved()) {
                        loadCauHoiData();
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Không tìm thấy chi tiết câu hỏi để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một câu hỏi để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xoaCauHoi() {
        int selectedRow = cauHoiTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = cauHoiTable.convertRowIndexToModel(selectedRow);
            CauHoi cauHoiCanXoa = cauHoiTableModel.getCauHoiAt(modelRow);
            if (cauHoiCanXoa != null) {
                int confirm = JOptionPane.showConfirmDialog(parentFrame,
                        "Bạn có chắc chắn muốn xóa câu hỏi ID: " + cauHoiCanXoa.getMaCauHoi() + "?\n'"
                                + cauHoiCanXoa.getNoiDungCauHoi().substring(0, Math.min(cauHoiCanXoa.getNoiDungCauHoi().length(), 70)) + "...'",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = cauHoiDAO.xoaCauHoi(cauHoiCanXoa.getMaCauHoi());
                    if (success) {
                        JOptionPane.showMessageDialog(parentFrame, "Xóa câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        loadCauHoiData();
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Xóa câu hỏi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một câu hỏi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}