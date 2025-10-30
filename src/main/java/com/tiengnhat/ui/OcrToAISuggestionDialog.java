// File: com/tiengnhat/ui/OcrToAISuggestionDialog.java
package com.tiengnhat.ui;

import com.tiengnhat.service.GeminiAIService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent; // Đã có

public class OcrToAISuggestionDialog extends JDialog {

    // ... (Các trường giữ nguyên như trước)
    private JTextArea txtOcrResult;
    private JTextArea txtAiSuggestions;
    private JButton btnGetAiSuggestions;
    private JButton btnApplyToForm;
    private JButton btnCancel;
    private JLabel lblStatus;
    private String initialOcrText;
    private String textToApply = null;


    public OcrToAISuggestionDialog(Dialog owner, String ocrText) {
        // ... (Constructor giữ nguyên)
        super(owner, "Kết quả OCR và Gợi ý AI", true);
        this.initialOcrText = ocrText;
        setSize(850, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        if (getContentPane() instanceof JPanel) {
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        }
        initComponents();
        txtOcrResult.setText(this.initialOcrText);
    }

    private void initComponents() {
        // ... (initComponents giữ nguyên, đảm bảo các icon được load đúng)
        // Panel Input
        JPanel ocrPanel = new JPanel(new BorderLayout(5, 5));
        ocrPanel.setBorder(new TitledBorder("Văn bản từ OCR (có thể chỉnh sửa)"));
        txtOcrResult = new JTextArea(10, 70);
        txtOcrResult.setLineWrap(true);
        txtOcrResult.setWrapStyleWord(true);
        JScrollPane ocrScrollPane = new JScrollPane(txtOcrResult);
        ocrPanel.add(ocrScrollPane, BorderLayout.CENTER);

        // Panel AI Suggestions
        JPanel aiPanel = new JPanel(new BorderLayout(5, 5));
        aiPanel.setBorder(new TitledBorder("Gợi ý từ AI (dựa trên văn bản OCR ở trên)"));
        txtAiSuggestions = new JTextArea(15, 70);
        txtAiSuggestions.setLineWrap(true);
        txtAiSuggestions.setWrapStyleWord(true);
        txtAiSuggestions.setEditable(false);
        txtAiSuggestions.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAiSuggestions.setBackground(new Color(248, 248, 248));
        JScrollPane aiScrollPane = new JScrollPane(txtAiSuggestions);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);

        // Panel Buttons
        JPanel buttonControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGetAiSuggestions = new JButton("Lấy Gợi Ý AI");
        ImageIcon aiIcon = createIcon("/icons/ai_magic_16.png");
        if (aiIcon != null) btnGetAiSuggestions.setIcon(aiIcon);
        buttonControlPanel.add(btnGetAiSuggestions);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnApplyToForm = new JButton("Áp dụng vào Form Câu Hỏi");
        ImageIcon applyIcon = createIcon("/icons/apply_16.png");
        if (applyIcon != null) btnApplyToForm.setIcon(applyIcon);
        btnCancel = new JButton("Hủy");
        ImageIcon cancelIcon = createIcon("/icons/cancel_16.png");
        if (cancelIcon != null) btnCancel.setIcon(cancelIcon);

        actionButtonPanel.add(btnApplyToForm);
        actionButtonPanel.add(btnCancel);

        lblStatus = new JLabel("Sẵn sàng.");
        JPanel bottomPanel = new JPanel(new BorderLayout(10,0));
        bottomPanel.add(lblStatus, BorderLayout.CENTER);
        bottomPanel.add(buttonControlPanel, BorderLayout.WEST);
        bottomPanel.add(actionButtonPanel, BorderLayout.EAST);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ocrPanel, aiPanel);
        splitPane.setResizeWeight(0.40);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnGetAiSuggestions.addActionListener(this::fetchAiSuggestions);
        btnApplyToForm.addActionListener(this::applyAndClose);
        btnCancel.addActionListener(e -> {
            textToApply = null;
            dispose();
        });
    }

    private void fetchAiSuggestions(ActionEvent e) {
        String currentOcrText = txtOcrResult.getText();
        if (currentOcrText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có văn bản OCR.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- SỬA ĐỔI PROMPT Ở ĐÂY ---
        String prompt = "Bạn là một trợ lý AI chuyên phân tích đề thi trắc nghiệm tiếng Nhật.\n" +
                "KHÔNG sử dụng định dạng Markdown trong phản hồi của bạn. Chỉ sử dụng văn bản thuần túy.\n" +
                "Cho đoạn văn bản sau (nằm trong phần VĂN BẢN CẦN PHÂN TÍCH):\n" +
                "Hãy thực hiện các yêu cầu sau cho TỪNG câu hỏi có trong VĂN BẢN CẦN PHÂN TÍCH:\n" +
                "1. Trích xuất đầy đủ nội dung câu hỏi.\n" +
                "2. Liệt kê tất cả các lựa chọn (ví dụ: A, B, C, D hoặc 1, 2, 3, 4, etc.).\n" +
                "3. **Hãy cố gắng xác định và đưa ra đáp án đúng nhất** kèm theo ký tự lựa chọn (ví dụ: Đáp án gợi ý: B). " + // Nhấn mạnh việc cố gắng xác định
                "   Nếu sau khi đã cố gắng hết sức mà vẫn không thể chắc chắn, thì mới ghi: \"Đáp án gợi ý: Không chắc chắn.\"\n" + // Điều kiện cho "Không chắc chắn"
                "4. Trình bày kết quả cho mỗi câu hỏi một cách rõ ràng, sử dụng định dạng sau:\n\n" +
                "Câu hỏi [số thứ tự tuần tự, bắt đầu từ 1]:\n" +
                "Nội dung: [Nội dung câu hỏi đầy đủ]\n" +
                "Lựa chọn:\n" +
                "  A. [Nội dung lựa chọn A]\n" +
                "  B. [Nội dung lựa chọn B]\n" +
                "  ...\n" +
                "Đáp án gợi ý: [Ký tự đáp án HOẶC 'Không chắc chắn']\n" + // Sửa lại cho rõ ràng
                "----------------------------------------\n\n" +
                "LƯU Ý QUAN TRỌNG: Chỉ tập trung vào việc trích xuất câu hỏi, lựa chọn và gợi ý đáp án từ VĂN BẢN CẦN PHÂN TÍCH. " +
                "Không thêm bất kỳ lời bình luận, giới thiệu, tóm tắt, hay thông tin nào khác ngoài định dạng yêu cầu. " +
                "Đảm bảo mỗi câu hỏi được phân cách bằng '----------------------------------------'.\n" +
                "Hãy phân tích kỹ lưỡng để đưa ra gợi ý đáp án chính xác nhất có thể."; // Thêm câu khích lệ
        // --- KẾT THÚC SỬA ĐỔI PROMPT ---

        lblStatus.setText("Đang lấy gợi ý từ AI, vui lòng đợi...");
        txtAiSuggestions.setText("");
        // ... (Phần gọi GeminiAIService và xử lý kết quả giữ nguyên như trước)
        btnGetAiSuggestions.setEnabled(false);
        btnApplyToForm.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        GeminiAIService.getSuggestionsFromTextAsync(currentOcrText, prompt)
            .thenAcceptAsync(result -> {
                txtAiSuggestions.setText(result);
                txtAiSuggestions.setCaretPosition(0);
                lblStatus.setText("Đã nhận gợi ý từ AI. Xem xét và nhấn 'Áp dụng'.");
            }, SwingUtilities::invokeLater)
            .exceptionallyAsync(ex -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                txtAiSuggestions.setText("Lỗi khi nhận gợi ý từ AI: " + cause.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi khi xử lý với AI:\n" + cause.getMessage(), "Lỗi AI", JOptionPane.ERROR_MESSAGE);
                lblStatus.setText("Lỗi khi lấy gợi ý AI.");
                return null;
            }, SwingUtilities::invokeLater)
            .whenCompleteAsync((unused, throwable) -> {
                btnGetAiSuggestions.setEnabled(true);
                btnApplyToForm.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
            }, SwingUtilities::invokeLater);
    }

    private void applyAndClose(ActionEvent e) {
        // ... (Giữ nguyên logic applyAndClose như trước)
        if (txtAiSuggestions.getText() != null && !txtAiSuggestions.getText().trim().isEmpty() &&
            !txtAiSuggestions.getText().trim().startsWith("Lỗi khi nhận gợi ý từ AI") &&
             txtAiSuggestions.getText().contains("Đáp án gợi ý:") ) { // Kiểm tra có vẻ là output hợp lệ
            
            // Kiểm tra cụ thể hơn nếu output AI không phải là "Không chắc chắn"
            if (txtAiSuggestions.getText().contains("Đáp án gợi ý:") && !txtAiSuggestions.getText().matches(".*Đáp án gợi ý:\\s*Không chắc chắn.*")) {
                 this.textToApply = txtAiSuggestions.getText();
                 System.out.println("Sẽ áp dụng văn bản từ gợi ý AI (có đáp án cụ thể).");
            } else {
                // Nếu AI trả về "Không chắc chắn" hoặc không có đáp án rõ ràng, hỏi người dùng
                int choice = JOptionPane.showConfirmDialog(this,
                        "AI không đưa ra đáp án chắc chắn hoặc gợi ý có vẻ chưa hoàn chỉnh.\n" +
                        "Bạn có muốn sử dụng văn bản OCR gốc (đã chỉnh sửa) để điền vào form không?\n" +
                        "(Chọn 'No' nếu muốn thử lại với AI hoặc tự điền tay sau.)",
                        "Xác nhận áp dụng",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    this.textToApply = txtOcrResult.getText();
                    System.out.println("Người dùng chọn áp dụng văn bản OCR gốc (đã chỉnh sửa).");
                } else {
                    this.textToApply = null; // Không áp dụng gì cả
                    System.out.println("Người dùng chọn không áp dụng hoặc sẽ thử lại.");
                    return; // Không đóng dialog nếu người dùng không muốn áp dụng
                }
            }
        } else {
            this.textToApply = txtOcrResult.getText();
            System.out.println("Không có gợi ý AI hợp lệ. Sẽ áp dụng văn bản OCR gốc (đã chỉnh sửa).");
        }
        dispose();
    }

    public String getTextToApply() {
        return textToApply;
    }

    private ImageIcon createIcon(String path) {
        // ... (Giữ nguyên createIcon)
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Không tìm thấy icon: " + path + " cho " + getClass().getName());
            return null;
        }
    }

    // display() method
    public void display() {
        setVisible(true);
    }
}