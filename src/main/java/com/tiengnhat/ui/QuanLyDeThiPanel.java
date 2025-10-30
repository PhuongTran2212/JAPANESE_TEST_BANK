package com.tiengnhat.ui;

import com.tiengnhat.dao.DeThiDAO;
import com.tiengnhat.model.DeThi;

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

public class QuanLyDeThiPanel extends JPanel {

    private JTable deThiTable;
    private DeThiTableModel deThiTableModel;
    private DeThiDAO deThiDAO;

    private JButton btnThemDeThi;
    private JButton btnSuaDeThi;
    private JButton btnXoaDeThi;
    private JButton btnTaiLai;
    private JButton btnXuatPDF;
    private JButton btnTaoDeThiTuDong;
    private JTextField txtTimKiem;
    private JButton btnTimKiem;
    private JLabel lblTongSoDeThi;

    private JFrame parentFrame;

    public QuanLyDeThiPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        deThiDAO = new DeThiDAO();
        initComponents();
        loadDeThiData();
    }

    private void initComponents() {
        // --- Panel Tìm kiếm ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tìm kiếm đề thi"),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel innerSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        innerSearchPanel.add(new JLabel("Tìm kiếm:"));
        txtTimKiem = new JTextField(25);
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemDeThi();
                }
            }
        });
        
        btnTimKiem = createButtonWithIcon("Tìm", "/icons/search_16.png");
        innerSearchPanel.add(txtTimKiem);
        innerSearchPanel.add(btnTimKiem);
        
        searchPanel.add(innerSearchPanel, BorderLayout.WEST);
        
        // --- Panel Các nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        btnThemDeThi = createButtonWithIcon("Thêm Đề Thi", "/icons/add_16.png");
        btnSuaDeThi = createButtonWithIcon("Sửa Đề Thi", "/icons/edit_16.png");
        btnXoaDeThi = createButtonWithIcon("Xóa Đề Thi", "/icons/delete_16.png");
        btnTaiLai = createButtonWithIcon("Tải Lại DS", "/icons/refresh_16.png");
        btnXuatPDF = createButtonWithIcon("Xuất PDF", "/icons/pdf_16.png");
        btnTaoDeThiTuDong = createButtonWithIcon("Tạo Đề Tự Động", "/icons/auto_16.png");

        buttonPanel.add(btnThemDeThi);
        buttonPanel.add(btnSuaDeThi);
        buttonPanel.add(btnXoaDeThi);
        buttonPanel.add(btnTaiLai);
        buttonPanel.add(btnXuatPDF);
        buttonPanel.add(btnTaoDeThiTuDong);
        
        // Label hiển thị tổng số đề thi
        lblTongSoDeThi = new JLabel("Tổng số: 0 đề thi");
        lblTongSoDeThi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongSoDeThi.setBorder(new EmptyBorder(0, 20, 0, 0));
        buttonPanel.add(lblTongSoDeThi);
        
        // Panel top chứa cả search và button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);

        // --- Bảng hiển thị đề thi ---
        deThiTableModel = new DeThiTableModel(new ArrayList<>());
        deThiTable = new JTable(deThiTableModel);
        deThiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deThiTable.setFillsViewportHeight(true);
        deThiTable.setRowHeight(30);
        deThiTable.setIntercellSpacing(new Dimension(0, 1));
        deThiTable.setShowGrid(true);
        deThiTable.setGridColor(new Color(230, 230, 230));
        
        // Tùy chỉnh header của bảng
        JTableHeader header = deThiTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        setupTableColumns();

        // Thêm bảng vào một panel có border
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Danh sách đề thi"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(deThiTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);

        // --- Xử lý sự kiện ---
        btnThemDeThi.addActionListener(e -> themDeThi());
        btnSuaDeThi.addActionListener(e -> suaDeThi());
        btnXoaDeThi.addActionListener(e -> xoaDeThi());
        btnTaiLai.addActionListener(e -> loadDeThiData());
        btnXuatPDF.addActionListener(e -> xuatPDF());
        btnTaoDeThiTuDong.addActionListener(e -> taoDeThiTuDong());
        btnTimKiem.addActionListener(e -> timKiemDeThi());
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
        deThiTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        deThiTable.getColumnModel().getColumn(0).setMaxWidth(100);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        deThiTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        deThiTable.getColumnModel().getColumn(1).setPreferredWidth(350);
        deThiTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        deThiTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        deThiTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        deThiTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        deThiTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // Tùy chỉnh renderer cho cột tên đề thi và mô tả để hiển thị tooltip
        DefaultTableCellRenderer tooltipRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent && value != null) {
                    JComponent jc = (JComponent) c;
                    jc.setToolTipText(value.toString());
                }
                return c;
            }
        };
        
        deThiTable.getColumnModel().getColumn(1).setCellRenderer(tooltipRenderer);
        deThiTable.getColumnModel().getColumn(2).setCellRenderer(tooltipRenderer);
    }

    public void loadDeThiData() {
        // Hiển thị indicator đang tải
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (parentFrame instanceof MainFrame) {
            ((MainFrame) parentFrame).updateStatus("Đang tải dữ liệu đề thi...");
        }
        
        new SwingWorker<List<DeThi>, Void>() {
            @Override
            protected List<DeThi> doInBackground() throws Exception {
                return deThiDAO.layTatCaDeThi();
            }
            
            @Override
            protected void done() {
                try {
                    List<DeThi> danhSach = get();
                    deThiTableModel.setData(danhSach);
                    lblTongSoDeThi.setText("Tổng số: " + danhSach.size() + " đề thi");
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Đã tải " + danhSach.size() + " đề thi");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Lỗi tải dữ liệu đề thi: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Lỗi tải dữ liệu đề thi");
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void timKiemDeThi() {
        String tuKhoa = txtTimKiem.getText().trim();
        if (tuKhoa.isEmpty()) {
            loadDeThiData();
            return;
        }
        
        // Hiển thị indicator đang tìm kiếm
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (parentFrame instanceof MainFrame) {
            ((MainFrame) parentFrame).updateStatus("Đang tìm kiếm đề thi...");
        }
        
        new SwingWorker<List<DeThi>, Void>() {
            @Override
            protected List<DeThi> doInBackground() throws Exception {
                return deThiDAO.timKiemDeThi(tuKhoa);
            }
            
            @Override
            protected void done() {
                try {
                    List<DeThi> ketQua = get();
                    deThiTableModel.setData(ketQua);
                    lblTongSoDeThi.setText("Tổng số: " + ketQua.size() + " đề thi");
                    
                    if (ketQua.isEmpty()) {
                        JOptionPane.showMessageDialog(parentFrame, 
                            "Không tìm thấy đề thi nào với từ khóa: '" + tuKhoa + "'.", 
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Tìm thấy " + ketQua.size() + " đề thi với từ khóa: '" + tuKhoa + "'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Lỗi tìm kiếm đề thi: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateStatus("Lỗi tìm kiếm đề thi");
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    // Các phương thức khác giữ nguyên...
    private void themDeThi() {
        DeThiDialog themDialog = new DeThiDialog(parentFrame, "Thêm Đề Thi Mới", null, deThiDAO);
        themDialog.setVisible(true);
        if (themDialog.isSaved()) {
            loadDeThiData();
        }
    }

    private void suaDeThi() {
        int selectedRow = deThiTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = deThiTable.convertRowIndexToModel(selectedRow);
            DeThi deThiCanSua = deThiTableModel.getDeThiAt(modelRow);
            if (deThiCanSua != null) {
                DeThi deThiDayDu = deThiDAO.layDeThiTheoId(deThiCanSua.getMaDeThi());
                if (deThiDayDu != null) {
                    DeThiDialog suaDialog = new DeThiDialog(parentFrame, "Sửa Đề Thi", deThiDayDu, deThiDAO);
                    suaDialog.setVisible(true);
                    if (suaDialog.isSaved()) {
                        loadDeThiData();
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Không tìm thấy chi tiết đề thi để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một đề thi để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xoaDeThi() {
        int selectedRow = deThiTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = deThiTable.convertRowIndexToModel(selectedRow);
            DeThi deThiCanXoa = deThiTableModel.getDeThiAt(modelRow);
            if (deThiCanXoa != null) {
                int confirm = JOptionPane.showConfirmDialog(parentFrame,
                        "Bạn có chắc chắn muốn xóa đề thi ID: " + deThiCanXoa.getMaDeThi() + "?\n'"
                                + deThiCanXoa.getTenDeThi() + "'",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = deThiDAO.xoaDeThi(deThiCanXoa.getMaDeThi());
                    if (success) {
                        JOptionPane.showMessageDialog(parentFrame, "Xóa đề thi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        loadDeThiData();
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Xóa đề thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một đề thi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuatPDF() {
        int selectedRow = deThiTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = deThiTable.convertRowIndexToModel(selectedRow);
            DeThi deThiCanXuat = deThiTableModel.getDeThiAt(modelRow);
            if (deThiCanXuat != null) {
                DeThi deThiDayDu = deThiDAO.layDeThiTheoId(deThiCanXuat.getMaDeThi());
                if (deThiDayDu != null) {
                    XuatPDFDialog xuatDialog = new XuatPDFDialog(parentFrame, deThiDayDu);
                    xuatDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Không tìm thấy chi tiết đề thi để xuất.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một đề thi để xuất PDF.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void taoDeThiTuDong() {
        TaoDeThiTuDongDialog dialog = new TaoDeThiTuDongDialog(parentFrame, deThiDAO);
        dialog.setVisible(true);
        if (dialog.isDeThiCreated()) {
            loadDeThiData();
        }
    }
}