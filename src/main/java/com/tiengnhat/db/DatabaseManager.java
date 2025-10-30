package com.tiengnhat.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // THAY ĐỔI THÔNG TIN KẾT NỐI NẾU CẦN
    private static final String DB_URL = "jdbc:mysql://localhost:3306/NGAN_HANG_DE_THI?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"; // ĐÃ SỬA
    private static final String USER = "root"; // Tên người dùng MySQL của bạn
    private static final String PASS = "1234"; // Mật khẩu MySQL của bạn

    private static Connection connection = null;

    // Private constructor để ngăn việc tạo instance từ bên ngoài (Singleton pattern)
    private DatabaseManager() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // MySQL Connector/J 8.0 trở lên không cần Class.forName()
                // Class.forName("com.mysql.cj.jdbc.Driver");

                connection = DriverManager.getConnection(DB_URL, USER, PASS);
                System.out.println("Kết nối tới MySQL (NGAN_HANG_DE_THI) thành công!"); // ĐÃ SỬA

            } catch (SQLException e) {
                System.err.println("Lỗi kết nối tới MySQL: " + e.getMessage());
                e.printStackTrace();
                // Trong ứng dụng thực tế, bạn nên xử lý lỗi này một cách phù hợp hơn,
                // ví dụ: hiển thị thông báo cho người dùng, ghi log, hoặc throw một custom exception.
            }
            // catch (ClassNotFoundException e) {
            //     System.err.println("Không tìm thấy MySQL JDBC Driver. Hãy đảm bảo bạn đã thêm thư viện vào project.");
            //     e.printStackTrace();
            // }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Đặt lại để có thể kết nối lại nếu cần
                System.out.println("Đã đóng kết nối MySQL.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối MySQL: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Phương thức main để kiểm tra kết nối nhanh
    public static void main(String[] args) {
        Connection conn = DatabaseManager.getConnection();
        if (conn != null) {
            System.out.println("Kiểm tra: Kết nối thành công!");
            DatabaseManager.closeConnection();
        } else {
            System.out.println("Kiểm tra: Kết nối thất bại!");
        }
    }
}