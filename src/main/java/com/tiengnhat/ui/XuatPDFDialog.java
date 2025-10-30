package com.tiengnhat.ui;

import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.DeThi;
import com.tiengnhat.model.LuaChonCauHoi;
import com.tiengnhat.utils.PDFExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class XuatPDFDialog extends JDialog {
    private DeThi deThi;
    private CauHoiDAO cauHoiDAO;

    private JTextField txtDuongDan;
    private JButton btnChonDuongDan;
    private JCheckBox chkXuatDapAn;
    private JCheckBox chkXuatGiaiThich;
    private JCheckBox chkXuatAudio;
    private JButton btnXuatPDF;
    private JButton btnHuy;

    public XuatPDFDialog(Frame owner, DeThi deThi) {
        super(owner, "Xuất Đề Thi PDF", true);
        this.deThi = deThi;
        this.cauHoiDAO = new CauHoiDAO();

        setSize(500, 300);
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
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        infoPanel.add(new JLabel(deThi.getTenDeThi()), gbc);

        // Tổng số câu hỏi
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Tổng số câu hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        infoPanel.add(new JLabel(deThi.getTongSoCauHoi() != null ? deThi.getTongSoCauHoi().toString() : "0"), gbc);

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Cấu hình xuất PDF ---
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("Cấu hình xuất PDF"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Đường dẫn lưu file
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        configPanel.add(new JLabel("Đường dẫn:"), gbc);
        txtDuongDan = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        configPanel.add(txtDuongDan, gbc);
        btnChonDuongDan = new JButton("...");
        btnChonDuongDan.addActionListener(e -> chonDuongDan());
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        configPanel.add(btnChonDuongDan, gbc);

        // Xuất đáp án
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        configPanel.add(new JLabel("Xuất đáp án:"), gbc);
        chkXuatDapAn = new JCheckBox("Tạo file đáp án riêng");
        chkXuatDapAn.setSelected(true);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        configPanel.add(chkXuatDapAn, gbc);

        // Xuất giải thích
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.gridwidth = 1;
        configPanel.add(new JLabel("Xuất giải thích:"), gbc);
        chkXuatGiaiThich = new JCheckBox("Bao gồm giải thích đáp án");
        chkXuatGiaiThich.setSelected(true);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 2;
        configPanel.add(chkXuatGiaiThich, gbc);

        // Xuất audio
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.gridwidth = 1;
        configPanel.add(new JLabel("Xuất audio:"), gbc);
        chkXuatAudio = new JCheckBox("Bao gồm thông tin file audio");
        chkXuatAudio.setSelected(true);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; gbc.gridwidth = 2;
        configPanel.add(chkXuatAudio, gbc);

        mainPanel.add(configPanel);

        // --- Panel nút Xuất và Hủy ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnXuatPDF = new JButton("Xuất PDF");
        btnHuy = new JButton("Hủy");

        btnXuatPDF.addActionListener(e -> xuatPDF());
        btnHuy.addActionListener(e -> dispose());

        bottomButtonPanel.add(btnXuatPDF);
        bottomButtonPanel.add(btnHuy);

        // Thêm các panel vào dialog
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void chonDuongDan() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn thư mục lưu file PDF");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtDuongDan.setText(selectedFile.getAbsolutePath());
        }
    }

    private void xuatPDF() {
        String duongDan = txtDuongDan.getText().trim();
        if (duongDan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đường dẫn lưu file PDF.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo tên file
        String tenFile = deThi.getTenDeThi().replaceAll("[\\\\/:*?\"<>|]", "_");
        String duongDanDeThi = duongDan + File.separator + tenFile + ".pdf";
        String duongDanDapAn = duongDan + File.separator + tenFile + "_dap_an.pdf";
        
        try {
            // Tạo đối tượng PDFExporter
            PDFExporter pdfExporter = new PDFExporter();
            
            // Xuất đề thi
            boolean result = pdfExporter.exportDeThi(deThi, duongDanDeThi, false, chkXuatAudio.isSelected());
            
            // Xuất đáp án nếu cần
            boolean resultDapAn = true;
            if (chkXuatDapAn.isSelected()) {
                resultDapAn = pdfExporter.exportDeThi(deThi, duongDanDapAn, true, chkXuatAudio.isSelected());
            }
            
            if (result && resultDapAn) {
                JOptionPane.showMessageDialog(this, 
                        "Xuất PDF thành công!\n" +
                        "Đề thi: " + duongDanDeThi + 
                        (chkXuatDapAn.isSelected() ? "\nĐáp án: " + duongDanDapAn : ""),
                        "Thành công", 
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Xuất PDF thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}