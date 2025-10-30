// src/com/tiengnhat/ui/MainApp.java (Hoặc ở package gốc)
package com.tiengnhat.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf; // Hoặc FlatDarkLaf, FlatIntelliJLaf, etc.

public class MainApp {
    public static void main(String[] args) {
        // Thiết lập FlatLaf L&F
        try {
            // UIManager.setLookAndFeel(new FlatLightLaf());
            // Hoặc một theme khác bạn thích, ví dụ:
             UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
            // UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatDarkPurpleIJTheme"); // Ví dụ theme tối
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
            // Nếu không được thì dùng L&F hệ thống
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Không thể đặt Look and Feel hệ thống: " + e);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}