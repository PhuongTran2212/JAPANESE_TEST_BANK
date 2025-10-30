// File: com/tiengnhat/ui/MainFrame.java
package com.tiengnhat.ui;

import com.tiengnhat.service.GeminiAIService; // Quan trọng: import service

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private QuanLyCauHoiPanel quanLyCauHoiPanel;
    private QuanLyDeThiPanel quanLyDeThiPanel; // Giả sử bạn có panel này
    private JPanel statusBar;
    private JLabel statusLabel;

    public MainFrame() {
        setTitle("Quản Lý Ngân Hàng Đề Thi Tiếng Nhật");
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Thay bằng DO_NOTHING_ON_CLOSE để xử lý tùy chỉnh
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1024, 700));

        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Không thể set Look and Feel của hệ thống.");
            }
        }

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPanel);

        setupStatusBar();
        initComponents(); // Gọi sau setupStatusBar

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    private void handleWindowClosing() {
        int confirm = JOptionPane.showConfirmDialog(
            MainFrame.this,
            "Bạn có chắc chắn muốn thoát ứng dụng?",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            GeminiAIService.shutdown(); // Gọi shutdown service AI ở đây
            dispose();
            System.exit(0);
        }
    }

    private void initComponents() {
        JToolBar toolBar = createToolBar();
        getContentPane().add(toolBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBorder(new EmptyBorder(5, 0, 0, 0)); // Khoảng cách từ toolbar xuống tab
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        quanLyCauHoiPanel = new QuanLyCauHoiPanel(this);
        // Khởi tạo QuanLyDeThiPanel nếu có
        quanLyDeThiPanel = new QuanLyDeThiPanel(this); // Ví dụ

        ImageIcon questionIcon = createIcon("/icons/question_mark_16.png");
        tabbedPane.addTab("Quản Lý Câu Hỏi", questionIcon, quanLyCauHoiPanel, "Quản lý ngân hàng câu hỏi thi");

        ImageIcon examIcon = createIcon("/icons/test_16.png");
        tabbedPane.addTab("Quản Lý Đề Thi", examIcon, quanLyDeThiPanel, "Quản lý và tạo đề thi"); // Ví dụ

        tabbedPane.addChangeListener(e -> {
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent == quanLyCauHoiPanel) {
                updateStatus("Đang xem Quản lý câu hỏi");
                // quanLyCauHoiPanel.loadCauHoiData(); // Có thể tải lại dữ liệu khi chuyển tab
            } else if (selectedComponent == quanLyDeThiPanel) {
                updateStatus("Đang xem Quản lý đề thi");
                // quanLyDeThiPanel.loadDeThiData(); // Ví dụ
            }
        });
        updateStatus("Sẵn sàng"); // Trạng thái ban đầu
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false); // Bỏ border mặc định của JToolBar
        toolBar.setMargin(new Insets(5, 5, 5, 5)); // Thêm padding cho toolbar

        JButton btnHome = createToolBarButton("Trang chủ", "/icons/home_24.png", "Về tab đầu tiên");
        btnHome.addActionListener(e -> {
            if (tabbedPane.getTabCount() > 0) tabbedPane.setSelectedIndex(0);
        });

        JButton btnRefresh = createToolBarButton("Làm mới", "/icons/refresh_24.png", "Tải lại dữ liệu cho tab hiện tại");
        btnRefresh.addActionListener(e -> {
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent == quanLyCauHoiPanel) {
                quanLyCauHoiPanel.loadCauHoiData();
            } else if (selectedComponent == quanLyDeThiPanel) {
                // quanLyDeThiPanel.loadDeThiData(); // Ví dụ
                 JOptionPane.showMessageDialog(this, "Chức năng làm mới cho tab này đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnSettings = createToolBarButton("Cài đặt", "/icons/settings_24.png", "Mở cài đặt ứng dụng");
        btnSettings.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Chức năng cài đặt đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        JButton btnHelp = createToolBarButton("Trợ giúp", "/icons/help_24.png", "Thông tin về phần mềm");
        btnHelp.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Phần mềm Quản lý Ngân hàng Đề thi Tiếng Nhật\nPhiên bản 1.0.1 (Gemini Integrated)\n\nLiên hệ hỗ trợ: support@example.com",
            "Trợ giúp", JOptionPane.INFORMATION_MESSAGE));

        toolBar.add(btnHome);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnRefresh);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnSettings);
        toolBar.add(Box.createHorizontalGlue()); // Đẩy nút Help sang phải
        toolBar.add(btnHelp);

        return toolBar;
    }

    private void setupStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY), // Đường kẻ phía trên
            new EmptyBorder(4, 10, 4, 10) // Padding cho status bar
        ));
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("Phiên bản 1.0.1");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        statusBar.add(versionLabel, BorderLayout.EAST);

        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private JButton createToolBarButton(String text, String iconPath, String toolTip) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setToolTipText(toolTip);
        try {
            java.net.URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                button.setIcon(icon);
            } else {
                 System.err.println("Không tìm thấy icon toolbar: " + iconPath);
            }
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải icon toolbar: " + iconPath + " - " + e.getMessage());
        }
        return button;
    }

    private ImageIcon createIcon(String path) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            }
             System.err.println("Không tìm thấy icon tab: " + path);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải icon tab: " + path + " - " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Tùy chọn: Splash screen
                // JWindow splash = new JWindow();
                // JLabel splashLabel = new JLabel(new ImageIcon(MainFrame.class.getResource("/images/splash.png"))); // Cần có ảnh splash
                // splash.getContentPane().add(splashLabel);
                // splash.pack();
                // splash.setLocationRelativeTo(null);
                // splash.setVisible(true);
                // Thread.sleep(1500); // Giả lập thời gian tải
                // splash.dispose();

                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                // Dù có lỗi splash, vẫn cố gắng hiện main frame
                SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
            }
        });
    }
}