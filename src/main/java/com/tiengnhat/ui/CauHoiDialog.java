// File: com/tiengnhat/ui/CauHoiDialog.java
package com.tiengnhat.ui;

// ... (Các import hiện tại của bạn)
import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.LuaChonCauHoi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
// ... các import khác
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import javax.imageio.ImageIO;


public class CauHoiDialog extends JDialog {
    // ... (Các trường giữ nguyên)
    private CauHoi cauHoi;
    private CauHoiDAO cauHoiDAO;
    private boolean saved = false;

    private JTextArea txtNoiDungCauHoi;
    private JComboBox<String> cmbLoaiCauHoi;
    private JComboBox<String> cmbDoKho;
    private JTextField txtKhoaDapAnDung;
    private JTextArea txtGiaiThichDapAn;
    private JTextField txtDuongDanFileAmThanh;
    private JButton btnChonFileAmThanh;
    private JTextField txtNhan;
    private JButton btnNhapTuAnh;

    private JPanel luaChonPanelContainer;
    private List<LuaChonEntryPanel> luaChonEntryPanels;
    private JButton btnThemLuaChon;

    private JButton btnLuu;
    private JButton btnHuy;
    private JLabel lblOcrStatus;

    private final String[] LOAI_CAU_HOI_OPTIONS = {"", "Trắc nghiệm", "Điền từ", "Nghe hiểu", "Đọc hiểu", "Kanji", "Ngữ pháp", "Hội thoại"};
    private final String[] DO_KHO_OPTIONS = {"", "N5", "N4", "N3", "N2", "N1"};
    private final String AUDIO_FILES_DIR = "audio_files";

    public CauHoiDialog(Frame owner, String title, CauHoi cauHoi, CauHoiDAO cauHoiDAO) {
        // ... (Constructor giữ nguyên)
        super(owner, title, true);
        this.cauHoi = (cauHoi == null) ? new CauHoi() : cauHoi;
        this.cauHoiDAO = cauHoiDAO;
        this.luaChonEntryPanels = new ArrayList<>();

        setSize(850, 780);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        if (this.cauHoi.getMaCauHoi() != 0) {
            populateForm();
        } else {
            cmbLoaiCauHoi.setSelectedIndex(0);
            cmbDoKho.setSelectedIndex(0);
        }
        toggleLuaChonPanelVisibility(cmbLoaiCauHoi.getSelectedItem() != null ? cmbLoaiCauHoi.getSelectedItem().toString() : "");
    }

    private void initComponents() {
        // ... (initComponents giữ nguyên, đảm bảo các icon được load đúng)
        // Đảm bảo btnNhapTuAnh gọi nhapTuAnh()
        // Các phần khác của UI giữ nguyên
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // --- Nhóm 1: Nội dung câu hỏi và OCR ---
        JPanel groupNoiDung = new JPanel(new BorderLayout(5, 5));
        groupNoiDung.setBorder(new TitledBorder("Nội dung câu hỏi"));

        txtNoiDungCauHoi = new JTextArea(6, 20);
        txtNoiDungCauHoi.setLineWrap(true);
        txtNoiDungCauHoi.setWrapStyleWord(true);
        JScrollPane noiDungScrollPane = new JScrollPane(txtNoiDungCauHoi);
        groupNoiDung.add(noiDungScrollPane, BorderLayout.CENTER);

        JPanel ocrPanel = new JPanel(new BorderLayout(5,0));
        btnNhapTuAnh = createButtonWithIcon("Nhập từ ảnh (OCR)", "/icons/ocr_import_16.png"); // Cần icon này
        btnNhapTuAnh.addActionListener(e -> nhapTuAnh());
        ocrPanel.add(btnNhapTuAnh, BorderLayout.WEST);
        lblOcrStatus = new JLabel(" ");
        lblOcrStatus.setForeground(Color.BLUE);
        ocrPanel.add(lblOcrStatus, BorderLayout.CENTER);
        groupNoiDung.add(ocrPanel, BorderLayout.SOUTH);
        contentPanel.add(groupNoiDung);
        contentPanel.add(Box.createVerticalStrut(10));

        // ... (Các nhóm khác giữ nguyên)
        // --- Nhóm 2: Thuộc tính câu hỏi ---
        JPanel groupThuocTinh = new JPanel(new GridBagLayout());
        groupThuocTinh.setBorder(new TitledBorder("Thuộc tính câu hỏi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        groupThuocTinh.add(new JLabel("Loại câu hỏi:"), gbc);
        cmbLoaiCauHoi = new JComboBox<>(LOAI_CAU_HOI_OPTIONS);
        cmbLoaiCauHoi.addActionListener(e ->
            toggleLuaChonPanelVisibility(cmbLoaiCauHoi.getSelectedItem() != null ? cmbLoaiCauHoi.getSelectedItem().toString() : "")
        );
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        groupThuocTinh.add(cmbLoaiCauHoi, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        groupThuocTinh.add(new JLabel("Độ khó:"), gbc);
        cmbDoKho = new JComboBox<>(DO_KHO_OPTIONS);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        groupThuocTinh.add(cmbDoKho, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        groupThuocTinh.add(new JLabel("Nhãn (tags):"), gbc);
        txtNhan = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        groupThuocTinh.add(txtNhan, gbc);
        contentPanel.add(groupThuocTinh);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- Nhóm 3: Đáp án & Giải thích ---
        JPanel groupDapAn = new JPanel(new GridBagLayout());
        groupDapAn.setBorder(new TitledBorder("Đáp án và Giải thích"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        groupDapAn.add(new JLabel("Đáp án (key/điền từ):"), gbc);
        txtKhoaDapAnDung = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        groupDapAn.add(txtKhoaDapAnDung, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        groupDapAn.add(new JLabel("Giải thích đáp án:"), gbc);
        txtGiaiThichDapAn = new JTextArea(4, 20);
        txtGiaiThichDapAn.setLineWrap(true);
        txtGiaiThichDapAn.setWrapStyleWord(true);
        JScrollPane giaiThichScrollPane = new JScrollPane(txtGiaiThichDapAn);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        groupDapAn.add(giaiThichScrollPane, gbc);
        contentPanel.add(groupDapAn);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- Nhóm 4: File Âm Thanh ---
        JPanel groupAudio = new JPanel(new BorderLayout(5,5));
        groupAudio.setBorder(new TitledBorder("File Âm Thanh (nếu có)"));
        txtDuongDanFileAmThanh = new JTextField(30);
        txtDuongDanFileAmThanh.setEditable(false);
        groupAudio.add(txtDuongDanFileAmThanh, BorderLayout.CENTER);
        btnChonFileAmThanh = createButtonWithIcon("Chọn File", "/icons/audio_file_16.png"); // Cần icon này
        btnChonFileAmThanh.addActionListener(e -> chonFileAmThanh());
        groupAudio.add(btnChonFileAmThanh, BorderLayout.EAST);
        contentPanel.add(groupAudio);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- Nhóm 5: Panel chứa các lựa chọn ---
        luaChonPanelContainer = new JPanel();
        luaChonPanelContainer.setLayout(new BoxLayout(luaChonPanelContainer, BoxLayout.Y_AXIS));
        luaChonPanelContainer.setBorder(new TitledBorder("Các Lựa Chọn Đáp Án (cho câu hỏi trắc nghiệm)"));

        JPanel themLuaChonButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnThemLuaChon = createButtonWithIcon("Thêm Lựa Chọn", "/icons/add_16.png"); // Cần icon này
        btnThemLuaChon.addActionListener(e -> themMoiLuaChonEntry(null));
        themLuaChonButtonPanel.add(btnThemLuaChon);
        luaChonPanelContainer.add(themLuaChonButtonPanel);
        
        JScrollPane luaChonScrollPane = new JScrollPane(luaChonPanelContainer);
        luaChonScrollPane.setPreferredSize(new Dimension(700, 150)); 
        luaChonScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        luaChonScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentPanel.add(luaChonScrollPane);

        // --- Panel Nút Lưu và Hủy ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnLuu = createButtonWithIcon("Lưu", "/icons/save_16.png"); // Cần icon này
        btnHuy = createButtonWithIcon("Hủy", "/icons/cancel_16.png"); // Cần icon này

        btnLuu.addActionListener(e -> luuThayDoi());
        btnHuy.addActionListener(e -> {
            saved = false;
            dispose();
        });

        bottomButtonPanel.add(btnLuu);
        bottomButtonPanel.add(btnHuy);

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    // --- SỬA ĐỔI/THÊM MỚI CÁC PHƯƠNG THỨC SAU ---
    private void processOcrTextAndPopulateForm(String textToProcess) {
        if (textToProcess == null || textToProcess.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có văn bản để xử lý.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        txtNoiDungCauHoi.setText("");
        clearLuaChonEntriesVisual();
        txtKhoaDapAnDung.setText("");
        // txtGiaiThichDapAn.setText(""); // Cân nhắc có xóa giải thích cũ không

        String[] lines = textToProcess.split("\\r?\\n");
        StringBuilder currentQuestionContentAggregator = new StringBuilder(); // Để ghép nối nội dung câu hỏi
        List<LuaChonCauHoi> parsedChoices = new ArrayList<>();
        String suggestedAnswerKeyFromAI = null;

        boolean isLikelyAIOutput = textToProcess.contains("Câu hỏi ") && textToProcess.contains("Đáp án gợi ý:");
        boolean inQuestionContentSection = false; // Cờ cho biết đang đọc phần "Nội dung:" của AI output
        boolean inChoicesSection = false;       // Cờ cho biết đang đọc phần "Lựa chọn:" của AI output

        // Regex patterns
        Pattern questionHeaderPattern = Pattern.compile("^Câu hỏi\\s*\\d+[:.]?", Pattern.CASE_INSENSITIVE);
        Pattern contentLabelPattern = Pattern.compile("^Nội dung[:.]\\s*", Pattern.CASE_INSENSITIVE);
        Pattern choicesLabelPattern = Pattern.compile("^Lựa chọn[:.]?", Pattern.CASE_INSENSITIVE);
        Pattern choicePattern = Pattern.compile("^\\s*([A-Da-dＡ-Ｄａ-ｄ1-9１-９①-⑨IVXivx]{1,2})\\s*[.。．)・]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
        Pattern suggestedAnswerPattern = Pattern.compile("^Đáp án gợi ý[:.]\\s*([A-Da-dＡ-Ｄａ-ｄ1-9１-９①-⑨IVXivx]+|Không chắc chắn).*", Pattern.CASE_INSENSITIVE);

        // Chỉ xử lý khối câu hỏi đầu tiên nếu là AI output
        boolean firstAIQuestionBlockProcessed = false;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (isLikelyAIOutput) {
                if (firstAIQuestionBlockProcessed && questionHeaderPattern.matcher(trimmedLine).find()) {
                    System.out.println("Đã xử lý khối câu hỏi AI đầu tiên, bỏ qua các khối sau.");
                    break; // Chỉ xử lý câu hỏi đầu tiên từ AI output
                }

                if (questionHeaderPattern.matcher(trimmedLine).find()) {
                    inQuestionContentSection = false; // Reset các cờ khi gặp header câu hỏi mới
                    inChoicesSection = false;
                    currentQuestionContentAggregator.setLength(0); // Xóa nội dung đang ghép
                    parsedChoices.clear();
                    suggestedAnswerKeyFromAI = null;
                    continue; // Bỏ qua dòng "Câu hỏi X:"
                }
                if (contentLabelPattern.matcher(trimmedLine).find()) {
                    inQuestionContentSection = true;
                    inChoicesSection = false;
                    // Lấy phần text sau "Nội dung: "
                    String contentText = trimmedLine.substring(contentLabelPattern.matcher(trimmedLine).regionEnd()).trim();
                    if (!contentText.isEmpty()) currentQuestionContentAggregator.append(contentText).append("\n");
                    continue;
                }
                if (choicesLabelPattern.matcher(trimmedLine).find()) {
                    inQuestionContentSection = false;
                    inChoicesSection = true;
                    continue; // Bỏ qua dòng "Lựa chọn:"
                }
                Matcher saMatcher = suggestedAnswerPattern.matcher(trimmedLine);
                if (saMatcher.matches()) {
                    String key = saMatcher.group(1).trim();
                    if (!"Không chắc chắn".equalsIgnoreCase(key)) {
                        suggestedAnswerKeyFromAI = key.toUpperCase();
                    }
                    firstAIQuestionBlockProcessed = true; // Đánh dấu đã xử lý xong khối AI
                    // Không 'continue' ngay, có thể còn lựa chọn hoặc nội dung phía dưới nếu AI format không chuẩn
                }

                if (inQuestionContentSection) {
                    currentQuestionContentAggregator.append(trimmedLine).append("\n");
                } else if (inChoicesSection) {
                    Matcher choiceMatcher = choicePattern.matcher(trimmedLine);
                    if (choiceMatcher.matches()) {
                        String choiceChar = choiceMatcher.group(1).trim().toUpperCase();
                        String choiceText = choiceMatcher.group(2).trim();
                        parsedChoices.add(new LuaChonCauHoi(0, choiceChar, choiceText, false));
                    } else if (!parsedChoices.isEmpty() && !trimmedLine.equals("----------------------------------------")) {
                        // Nối vào lựa chọn cuối nếu có nhiều dòng
                        LuaChonCauHoi lastChoice = parsedChoices.get(parsedChoices.size() - 1);
                        lastChoice.setNoiDungLuaChon(lastChoice.getNoiDungLuaChon() + "\n" + trimmedLine);
                    }
                } else if (!firstAIQuestionBlockProcessed && !trimmedLine.equals("----------------------------------------") && !suggestedAnswerPattern.matcher(trimmedLine).matches()) {
                     // Nếu chưa vào section nào rõ ràng và chưa xử lý xong khối AI, và không phải dòng đáp án gợi ý
                     // thì có thể là phần tiếp theo của nội dung câu hỏi AI
                     currentQuestionContentAggregator.append(trimmedLine).append("\n");
                }

            } else { // Xử lý OCR thô (không có cấu trúc AI)
                Matcher choiceMatcher = choicePattern.matcher(trimmedLine);
                if (choiceMatcher.matches()) {
                    if (currentQuestionContentAggregator.length() > 0 && txtNoiDungCauHoi.getText().isEmpty()) {
                        // Lần đầu gặp lựa chọn, nội dung đã thu thập được là của câu hỏi
                        txtNoiDungCauHoi.setText(currentQuestionContentAggregator.toString().trim());
                        currentQuestionContentAggregator.setLength(0); // Reset
                    }
                    String choiceChar = choiceMatcher.group(1).trim().toUpperCase();
                    String choiceText = choiceMatcher.group(2).trim();
                    parsedChoices.add(new LuaChonCauHoi(0, choiceChar, choiceText, false));
                } else if (!trimmedLine.isEmpty()) {
                    if (parsedChoices.isEmpty()) { // Chưa có lựa chọn nào -> vẫn là nội dung câu hỏi
                        // Lọc các dòng hướng dẫn không cần thiết
                        if (!(trimmedLine.toLowerCase().contains("もんだい") ||
                              trimmedLine.toLowerCase().contains("えらんでください") ||
                              trimmedLine.toLowerCase().contains("ただしいものをえらびなさい") ||
                              trimmedLine.matches("^\\s*[1-4１-４]\\s*・\\s*[1-4１-４]\\s*から.*$"))) {
                             currentQuestionContentAggregator.append(trimmedLine).append("\n");
                        }
                    } else { // Đã có lựa chọn, đây có thể là phần tiếp theo của lựa chọn cuối
                        LuaChonCauHoi lastChoice = parsedChoices.get(parsedChoices.size() - 1);
                        lastChoice.setNoiDungLuaChon(lastChoice.getNoiDungLuaChon() + "\n" + trimmedLine);
                    }
                }
            }
        }

        // Gán nội dung câu hỏi cuối cùng nếu còn
        if (currentQuestionContentAggregator.length() > 0 && txtNoiDungCauHoi.getText().trim().isEmpty()) {
            txtNoiDungCauHoi.setText(currentQuestionContentAggregator.toString().trim());
        } else if (currentQuestionContentAggregator.length() > 0) { // Nếu txtNoiDungCauHoi đã có gì đó (từ AI) thì ghép thêm
             txtNoiDungCauHoi.append("\n" + currentQuestionContentAggregator.toString().trim());
        }


        if (!parsedChoices.isEmpty()) {
            cmbLoaiCauHoi.setSelectedItem("Trắc nghiệm");
            toggleLuaChonPanelVisibility("Trắc nghiệm");
            for (LuaChonCauHoi lc : parsedChoices) {
                if (suggestedAnswerKeyFromAI != null && lc.getKyTuLuaChon().equalsIgnoreCase(suggestedAnswerKeyFromAI)) {
                    lc.setLaDapAnDung(true);
                }
                themMoiLuaChonEntry(lc);
            }
        } else {
            if (!"Trắc nghiệm".equals(cmbLoaiCauHoi.getSelectedItem())) {
                 toggleLuaChonPanelVisibility(cmbLoaiCauHoi.getSelectedItem() != null ? cmbLoaiCauHoi.getSelectedItem().toString() : "");
            }
        }

        if (suggestedAnswerKeyFromAI != null && !parsedChoices.stream().anyMatch(LuaChonCauHoi::isLaDapAnDung)) {
            txtKhoaDapAnDung.setText(suggestedAnswerKeyFromAI);
        }

        txtNoiDungCauHoi.requestFocusInWindow();
        JOptionPane.showMessageDialog(this, "Đã điền dữ liệu. Vui lòng kiểm tra và hoàn tất.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }


    private void nhapTuAnh() {
        // ... (Giữ nguyên logic của nhapTuAnh như đã cung cấp ở câu trả lời trước,
        // đảm bảo nó gọi OcrToAISuggestionDialog và sau đó gọi processOcrTextAndPopulateForm)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh chứa đề thi");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ảnh (PNG, JPG, TIF, BMP)", "png", "jpg", "jpeg", "tif", "tiff", "bmp"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToScan = fileChooser.getSelectedFile();
            lblOcrStatus.setText("Đang xử lý OCR...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            btnNhapTuAnh.setEnabled(false);
            btnLuu.setEnabled(false);

            new SwingWorker<String, Void>() {
                private String errorMessage = null;

                @Override
                protected String doInBackground() {
                    String tessDataPath = System.getenv("TESSDATA_PREFIX");
                    if (tessDataPath == null) {
                        tessDataPath = "C:/Program Files/Tesseract-OCR/tessdata"; // Hoặc đường dẫn mặc định khác
                    }

                    try {
                        BufferedImage originalImage = ImageIO.read(fileToScan);
                        if (originalImage == null) {
                            errorMessage = "Không thể đọc file ảnh."; return null;
                        }
                        ITesseract instance = new Tesseract();
                        File tessDataDir = new File(tessDataPath);
                        if (!tessDataDir.exists() || !tessDataDir.isDirectory()) {
                            errorMessage = "Lỗi: Không tìm thấy thư mục tessdata tại: " + tessDataPath +
                                           "\nVui lòng cài đặt Tesseract và đặt biến TESSDATA_PREFIX, hoặc sửa đường dẫn.";
                            return null;
                        }
                        instance.setDatapath(tessDataPath);
                        instance.setLanguage("jpn+eng");
                        instance.setPageSegMode(3); // PSM_AUTO
                        return instance.doOCR(originalImage);
                    } catch (IOException ioe) {
                        errorMessage = "Lỗi I/O: " + ioe.getMessage();
                    } catch (TesseractException te) {
                        errorMessage = "Lỗi Tesseract: " + te.getMessage();
                    } catch (UnsatisfiedLinkError ule) {
                        errorMessage = "Lỗi thư viện Tesseract (UnsatisfiedLinkError): " + ule.getMessage();
                    } catch (Exception e) {
                        errorMessage = "Lỗi không xác định khi OCR: " + e.getMessage();
                    }
                    if (errorMessage != null) System.err.println(errorMessage);
                    return null;
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    btnNhapTuAnh.setEnabled(true);
                    btnLuu.setEnabled(true);
                    lblOcrStatus.setText(" ");

                    try {
                        String ocrResultText = get();
                        if (errorMessage != null) {
                            JOptionPane.showMessageDialog(CauHoiDialog.this, errorMessage, "Lỗi OCR", JOptionPane.ERROR_MESSAGE);
                        } else if (ocrResultText == null || ocrResultText.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(CauHoiDialog.this, "OCR không nhận dạng được văn bản.", "Kết quả OCR", JOptionPane.WARNING_MESSAGE);
                        } else {
                            OcrToAISuggestionDialog suggestDialog = new OcrToAISuggestionDialog(CauHoiDialog.this, ocrResultText.trim());
                            suggestDialog.setVisible(true);
                            String textToApplyToForm = suggestDialog.getTextToApply();
                            if (textToApplyToForm != null) {
                                processOcrTextAndPopulateForm(textToApplyToForm);
                            } else {
                                lblOcrStatus.setText("Đã hủy áp dụng kết quả.");
                            }
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(CauHoiDialog.this, "Lỗi khi lấy kết quả OCR: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // ... (Các phương thức khác: createButtonWithIcon, toggleLuaChonPanelVisibility, themMoiLuaChonEntry, populateForm, clearLuaChonEntriesVisual, luuThayDoi, chonFileAmThanh, isSaved, class LuaChonEntryPanel giữ nguyên)
    private JButton createButtonWithIcon(String text, String iconPath) {
        JButton button = new JButton(text);
        java.net.URL imgURL = getClass().getResource(iconPath);
        if (imgURL != null) {
            button.setIcon(new ImageIcon(imgURL));
        } else {
            System.err.println("Không tìm thấy icon cho nút '" + text + "': " + iconPath + " trong " + getClass().getName());
        }
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(5);
        return button;
    }

    private void toggleLuaChonPanelVisibility(String loaiCauHoi) {
        boolean isTracNghiem = "Trắc nghiệm".equalsIgnoreCase(loaiCauHoi);
        if (luaChonPanelContainer.getParent() != null && luaChonPanelContainer.getParent().getParent() instanceof JScrollPane) {
            luaChonPanelContainer.getParent().getParent().setVisible(isTracNghiem);
        } else if (luaChonPanelContainer.getParent() != null) { // Fallback
            luaChonPanelContainer.getParent().setVisible(isTracNghiem);
        }
        btnThemLuaChon.setEnabled(isTracNghiem);
        this.revalidate();
        this.repaint();
    }

    private void themMoiLuaChonEntry(LuaChonCauHoi luaChon) {
        LuaChonEntryPanel entryPanel = new LuaChonEntryPanel(luaChon, luaChonEntryPanels.size() + 1);
        luaChonEntryPanels.add(entryPanel);
        Component themButtonContainer = luaChonPanelContainer.getComponent(luaChonPanelContainer.getComponentCount() -1);
        luaChonPanelContainer.remove(themButtonContainer);
        luaChonPanelContainer.add(entryPanel);
        luaChonPanelContainer.add(themButtonContainer);
        luaChonPanelContainer.revalidate();
        luaChonPanelContainer.repaint();
    }
    
    private void populateForm() {
        txtNoiDungCauHoi.setText(cauHoi.getNoiDungCauHoi());
        cmbLoaiCauHoi.setSelectedItem(cauHoi.getLoaiCauHoi());
        cmbDoKho.setSelectedItem(cauHoi.getDoKho());
        txtKhoaDapAnDung.setText(cauHoi.getKhoaDapAnDung());
        txtGiaiThichDapAn.setText(cauHoi.getGiaiThichDapAn());
        txtDuongDanFileAmThanh.setText(cauHoi.getDuongDanFileAmThanh());
        txtNhan.setText(cauHoi.getNhan());
        clearLuaChonEntriesVisual();
        if (cauHoi.getDanhSachLuaChon() != null) {
            for (LuaChonCauHoi lc : cauHoi.getDanhSachLuaChon()) {
                themMoiLuaChonEntry(lc);
            }
        }
        toggleLuaChonPanelVisibility(cauHoi.getLoaiCauHoi());
    }
    
    private void clearLuaChonEntriesVisual() {
        Component themLuaChonButtonPanel = null;
        if (luaChonPanelContainer.getComponentCount() > 0) {
             themLuaChonButtonPanel = luaChonPanelContainer.getComponent(luaChonPanelContainer.getComponentCount() - 1);
        }
        luaChonPanelContainer.removeAll();
        luaChonEntryPanels.clear();
        if (themLuaChonButtonPanel != null) {
            luaChonPanelContainer.add(themLuaChonButtonPanel);
        }
        luaChonPanelContainer.revalidate();
        luaChonPanelContainer.repaint();
    }

    private void luuThayDoi() {
        // ... (Giữ nguyên logic luuThayDoi)
        if (txtNoiDungCauHoi.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung câu hỏi không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cmbLoaiCauHoi.getSelectedItem() == null || cmbLoaiCauHoi.getSelectedItem().toString().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại câu hỏi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        cauHoi.setNoiDungCauHoi(txtNoiDungCauHoi.getText().trim());
        cauHoi.setLoaiCauHoi(cmbLoaiCauHoi.getSelectedItem().toString());
        cauHoi.setDoKho(cmbDoKho.getSelectedItem() != null ? cmbDoKho.getSelectedItem().toString() : "");
        cauHoi.setKhoaDapAnDung(txtKhoaDapAnDung.getText().trim());
        cauHoi.setGiaiThichDapAn(txtGiaiThichDapAn.getText().trim());
        cauHoi.setDuongDanFileAmThanh(txtDuongDanFileAmThanh.getText().trim());
        cauHoi.setNhan(txtNhan.getText().trim());

        List<LuaChonCauHoi> danhSachLuaChonMoi = new ArrayList<>();
        boolean isLuaChonVisible = false;
        if (luaChonPanelContainer.getParent() != null && luaChonPanelContainer.getParent().getParent() instanceof JScrollPane) {
            isLuaChonVisible = luaChonPanelContainer.getParent().getParent().isVisible();
        } else if (luaChonPanelContainer.getParent() != null) {
            isLuaChonVisible = luaChonPanelContainer.getParent().isVisible();
        }

        if (isLuaChonVisible) {
            for (LuaChonEntryPanel entryPanel : luaChonEntryPanels) {
                LuaChonCauHoi lc = entryPanel.getLuaChonData();
                if (lc != null && !lc.getNoiDungLuaChon().trim().isEmpty()) {
                    danhSachLuaChonMoi.add(lc);
                }
            }
            if ("Trắc nghiệm".equalsIgnoreCase(cauHoi.getLoaiCauHoi())) {
                boolean coDapAnDungQuaLuaChon = danhSachLuaChonMoi.stream().anyMatch(LuaChonCauHoi::isLaDapAnDung);
                boolean coDapAnDungQuaKey = !txtKhoaDapAnDung.getText().trim().isEmpty();
                if (danhSachLuaChonMoi.isEmpty() && !coDapAnDungQuaKey) {
                     JOptionPane.showMessageDialog(this, "Câu hỏi trắc nghiệm phải có các lựa chọn hoặc một đáp án dạng 'key'.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                     return;
                }
                if (!danhSachLuaChonMoi.isEmpty() && !coDapAnDungQuaLuaChon && !coDapAnDungQuaKey) {
                    JOptionPane.showMessageDialog(this, "Câu hỏi trắc nghiệm phải có ít nhất một lựa chọn là đáp án đúng, hoặc nhập ký tự đáp án đúng vào ô 'Đáp án (key/điền từ)'.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        cauHoi.setDanhSachLuaChon(danhSachLuaChonMoi);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnLuu.setEnabled(false); btnHuy.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return cauHoi.getMaCauHoi() == 0 ? cauHoiDAO.themCauHoi(cauHoi) : cauHoiDAO.capNhatCauHoi(cauHoi);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        saved = true;
                        JOptionPane.showMessageDialog(CauHoiDialog.this, "Lưu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else { JOptionPane.showMessageDialog(CauHoiDialog.this, "Lưu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CauHoiDialog.this, "Lỗi khi lưu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    btnLuu.setEnabled(true); btnHuy.setEnabled(true);
                }
            }
        }.execute();
    }

    private void chonFileAmThanh() {
        // ... (Giữ nguyên chonFileAmThanh)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file âm thanh");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (MP3, WAV)", "mp3", "wav"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // ... (logic copy file và set text)
        }
    }
    public boolean isSaved() { return saved; }

    private class LuaChonEntryPanel extends JPanel {
        // ... (Giữ nguyên class LuaChonEntryPanel)
        private JLabel lblKyTu;
        private JTextArea txtNoiDungLuaChon;
        private JCheckBox chkLaDapAnDung;
        private JButton btnXoaLuaChonNay;
        private int stt;

        public LuaChonEntryPanel(LuaChonCauHoi luaChon, int soThuTu) {
            this.stt = soThuTu;
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0, Color.LIGHT_GRAY),
                new EmptyBorder(5,5,5,5)
            ));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 5, 2, 5);
            c.anchor = GridBagConstraints.WEST;

            lblKyTu = new JLabel(); 
            updateKyTuLabel();
            c.gridx = 0; c.gridy = 0; c.weightx = 0;
            add(lblKyTu, c);

            txtNoiDungLuaChon = new JTextArea(2, 35);
            txtNoiDungLuaChon.setLineWrap(true);
            txtNoiDungLuaChon.setWrapStyleWord(true);
            JScrollPane spNoiDung = new JScrollPane(txtNoiDungLuaChon);
            spNoiDung.setMinimumSize(new Dimension(200, 50));
            c.gridx = 1; c.gridy = 0; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
            add(spNoiDung, c);

            chkLaDapAnDung = new JCheckBox("Đúng?");
            c.gridx = 2; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            add(chkLaDapAnDung, c);

            btnXoaLuaChonNay = createButtonWithIcon("", "/icons/delete_entry_16.png");
            btnXoaLuaChonNay.setToolTipText("Xóa lựa chọn này");
            btnXoaLuaChonNay.setPreferredSize(new Dimension(28,28));
            c.gridx = 3; c.gridy = 0;
            add(btnXoaLuaChonNay, c);

            if (luaChon != null) {
                if (luaChon.getKyTuLuaChon() != null && !luaChon.getKyTuLuaChon().matches("^[A-Z]$")) {
                    lblKyTu.setText(luaChon.getKyTuLuaChon() + ".");
                }
                txtNoiDungLuaChon.setText(luaChon.getNoiDungLuaChon());
                chkLaDapAnDung.setSelected(luaChon.isLaDapAnDung());
            }

            btnXoaLuaChonNay.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this.getParent().getParent(),
                    "Bạn có chắc muốn xóa lựa chọn " + lblKyTu.getText() + "?",
                    "Xác nhận xóa lựa chọn", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Container parentContainer = LuaChonEntryPanel.this.getParent();
                    parentContainer.remove(LuaChonEntryPanel.this);
                    luaChonEntryPanels.remove(LuaChonEntryPanel.this);
                    for(int i=0; i < luaChonEntryPanels.size(); i++){
                        luaChonEntryPanels.get(i).updateStt(i + 1);
                    }
                    parentContainer.revalidate();
                    parentContainer.repaint();
                }
            });
        }
        private void updateKyTuLabel() { this.lblKyTu.setText((char)('A' + stt -1) + "."); }
        public void updateStt(int newStt){ this.stt = newStt; updateKyTuLabel(); }
        public LuaChonCauHoi getLuaChonData() {
            String kyTu = lblKyTu.getText().replace(".", "");
            String noiDung = txtNoiDungLuaChon.getText().trim();
            boolean laDapAnDung = chkLaDapAnDung.isSelected();
            if (noiDung.isEmpty() && !laDapAnDung) return null;
            return new LuaChonCauHoi(0, kyTu, noiDung, laDapAnDung);
        }
    }
}