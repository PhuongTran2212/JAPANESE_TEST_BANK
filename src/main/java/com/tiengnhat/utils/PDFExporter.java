package com.tiengnhat.utils;

// Các import giữ nguyên
import com.tiengnhat.dao.CauHoiDAO;
import com.tiengnhat.model.CauHoi;
import com.tiengnhat.model.DeThi;
import com.tiengnhat.model.LuaChonCauHoi;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
// import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject; // (Không được sử dụng trong code bạn cung cấp, có thể xóa)

import java.io.File;
import java.io.IOException;
import java.io.InputStream; // Thêm import này nếu dùng getResourceAsStream
import java.util.List;

public class PDFExporter {
    private CauHoiDAO cauHoiDAO;
    private PDType0Font fontRegular;
    private PDType0Font fontBold;    // Vẫn giữ khai báo, nhưng sẽ trỏ đến fontRegular
    private PDType0Font fontItalic;  // Vẫn giữ khai báo, nhưng sẽ trỏ đến fontRegular
    private float fontSize = 11;
    private float leading = 1.5f * fontSize;

    public PDFExporter() {
        this.cauHoiDAO = new CauHoiDAO();
    }

    public boolean exportDeThi(DeThi deThi, String filePath, boolean includeAnswers, boolean includeAudio) {
        PDDocument document = null;
        InputStream fontStreamRegular = null; // Sử dụng InputStream để tải font từ resources

        try {
            document = new PDDocument();

            // --- THAY ĐỔI CÁCH TẢI FONT ---
            // Cách 1: Tải từ file (như cũ, dễ bị lỗi khi đóng gói JAR)
            // fontRegular = PDType0Font.load(document, new File("src/test/resources/fonts/NotoSansJP-Regular.ttf"));

            // Cách 2: Tải từ classpath (khuyến nghị, đặt font trong src/main/resources/fonts/ Hoặc src/test/resources/fonts/)
            // Nếu font ở src/test/resources/fonts/NotoSansJP-Regular.ttf
            // thì đường dẫn trong getResourceAsStream sẽ là "/fonts/NotoSansJP-Regular.ttf"
            // Giả sử bạn đặt font trong thư mục `resources/fonts` của classpath
            String fontPath = "/fonts/NotoSansJP-Regular.ttf"; // Điều chỉnh nếu cần
            fontStreamRegular = PDFExporter.class.getResourceAsStream(fontPath);
            if (fontStreamRegular == null) {
                // Nếu không tìm thấy, thử tải trực tiếp từ file system như một fallback cho môi trường dev
                // (Không khuyến khích cho bản production)
                File fontFile = new File("src/test/resources/fonts/NotoSansJP-Regular.ttf");
                if (!fontFile.exists()) {
                     fontFile = new File("src/main/resources/fonts/NotoSansJP-Regular.ttf"); // Thử cả main resources
                }
                if (fontFile.exists()) {
                    fontRegular = PDType0Font.load(document, fontFile);
                    System.out.println("Đã tải font từ file: " + fontFile.getAbsolutePath());
                } else {
                    throw new IOException("Không tìm thấy font: " + fontPath + " trong classpath và không tìm thấy file dự phòng.");
                }
            } else {
                fontRegular = PDType0Font.load(document, fontStreamRegular);
                System.out.println("Đã tải font từ classpath: " + fontPath);
            }
            // --- KẾT THÚC THAY ĐỔI CÁCH TẢI FONT ---


            // Gán fontBold và fontItalic bằng fontRegular
            fontBold = fontRegular;
            fontItalic = fontRegular;

            // Create a page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float width = page.getMediaBox().getWidth() - 2 * margin;
            float startY = page.getMediaBox().getHeight() - margin;
            float currentY = startY;

            // Get questions
            List<CauHoi> danhSachCauHoi = deThi.getDanhSachCauHoiTrongDe();
            if (danhSachCauHoi == null || danhSachCauHoi.isEmpty()) {
                danhSachCauHoi = cauHoiDAO.layTatCaCauHoiTrongDeThi(deThi.getMaDeThi());
            }

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Add title
            contentStream.beginText();
            contentStream.setFont(fontBold, 16); // Sẽ dùng fontRegular
            contentStream.newLineAtOffset(margin, currentY);
            contentStream.showText(deThi.getTenDeThi());
            contentStream.endText();
            currentY -= 20;

            // Add description if available
            if (deThi.getMoTa() != null && !deThi.getMoTa().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(fontItalic, 12); // Sẽ dùng fontRegular
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText(deThi.getMoTa());
                contentStream.endText();
                currentY -= 20;
            }

            // Add separator line
            contentStream.setLineWidth(1f);
            contentStream.moveTo(margin, currentY);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
            contentStream.stroke();
            currentY -= 20;

            // Add questions
            int questionNumber = 1;
            for (CauHoi cauHoi : danhSachCauHoi) {
                if (currentY < 100 && danhSachCauHoi.indexOf(cauHoi) > 0) { // Thêm điều kiện để không tạo trang mới ngay câu đầu
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    currentY = startY;
                }

                // Add question
                contentStream.beginText();
                contentStream.setFont(fontBold, fontSize); // Sẽ dùng fontRegular
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText("Câu " + questionNumber + ": " + cauHoi.getLoaiCauHoi());
                contentStream.endText();
                currentY -= leading;

                // Add question content
                String[] lines = wrapText(cauHoi.getNoiDungCauHoi(), fontRegular, fontSize, width);
                contentStream.beginText();
                contentStream.setFont(fontRegular, fontSize);
                contentStream.newLineAtOffset(margin, currentY);
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                    currentY -= leading;
                }
                contentStream.endText();

                // Add audio information if available and requested
                if (includeAudio && cauHoi.getDuongDanFileAmThanh() != null && !cauHoi.getDuongDanFileAmThanh().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(fontItalic, fontSize); // Sẽ dùng fontRegular
                    contentStream.newLineAtOffset(margin, currentY);
                    contentStream.showText("Audio: " + cauHoi.getDuongDanFileAmThanh());
                    contentStream.endText();
                    currentY -= leading;
                }

                // Add choices for multiple choice questions
                if ("Trắc nghiệm".equals(cauHoi.getLoaiCauHoi()) && cauHoi.getDanhSachLuaChon() != null) {
                    for (LuaChonCauHoi luaChon : cauHoi.getDanhSachLuaChon()) {
                        if (currentY < 100) {
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            currentY = startY;
                        }

                        contentStream.beginText();
                        contentStream.setFont(fontRegular, fontSize);
                        contentStream.newLineAtOffset(margin + 20, currentY);

                        String choiceText = luaChon.getKyTuLuaChon() + ". " + luaChon.getNoiDungLuaChon();
                        if (includeAnswers && luaChon.isLaDapAnDung()) {
                            choiceText += " (✓)";
                        }

                        String[] choiceLines = wrapText(choiceText, fontRegular, fontSize, width - 20);
                        for (String line : choiceLines) {
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -leading);
                            currentY -= leading;
                        }
                        contentStream.endText();
                    }
                }

                // Add answer and explanation if requested
                if (includeAnswers) {
                    if (currentY < 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = startY;
                    }

                    contentStream.beginText();
                    contentStream.setFont(fontBold, fontSize); // Sẽ dùng fontRegular
                    contentStream.newLineAtOffset(margin, currentY);
                    contentStream.showText("Đáp án: " + cauHoi.getKhoaDapAnDung());
                    contentStream.endText();
                    currentY -= leading;

                    if (cauHoi.getGiaiThichDapAn() != null && !cauHoi.getGiaiThichDapAn().isEmpty()) {
                        contentStream.beginText();
                        contentStream.setFont(fontItalic, fontSize); // Sẽ dùng fontRegular, có thể bạn muốn dùng fontBold cho chữ "Giải thích:"
                        contentStream.newLineAtOffset(margin, currentY);
                        contentStream.showText("Giải thích: "); // Dòng này có thể không cần newLineAtOffset nếu bạn muốn nó nối tiếp
                        contentStream.endText(); // Kết thúc text cho "Giải thích:"
                        // currentY -= leading; // Có thể không cần nếu nội dung giải thích đi ngay sau

                        // Vẽ nội dung giải thích
                        String[] explanationLines = wrapText(cauHoi.getGiaiThichDapAn(), fontRegular, fontSize, width - fontRegular.getStringWidth("Giải thích: ") / 1000 * fontSize - 5); // Điều chỉnh width
                        
                        // Ghi nội dung giải thích
                        contentStream.beginText();
                        contentStream.setFont(fontRegular, fontSize);
                        // Vị trí bắt đầu của nội dung giải thích, có thể cùng dòng với "Giải thích: " hoặc dòng mới
                        // Nếu muốn cùng dòng:
                        // contentStream.newLineAtOffset(margin + fontRegular.getStringWidth("Giải thích: ") / 1000 * fontSize + 5, currentY);
                        // Nếu muốn dòng mới:
                        contentStream.newLineAtOffset(margin, currentY - leading); // Hoặc currentY nếu "Giải thích:" không trừ leading
                        
                        float tempCurrentY = currentY - leading; // Bắt đầu từ dòng dưới "Giải thích:"
                        if (cauHoi.getGiaiThichDapAn().startsWith("\n")) { // Nếu giải thích bắt đầu bằng xuống dòng
                             tempCurrentY = currentY - leading;
                        } else {
                             tempCurrentY = currentY; // Nếu giải thích không bắt đầu bằng xuống dòng, thì chữ "Giải thích:" và nội dung giải thích nên cùng 1 dòng.
                             contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(margin + fontRegular.getStringWidth("Giải thích: ") / 1000 * fontSize + 2, currentY));
                        }


                        for (String line : explanationLines) {
                             if (line.trim().isEmpty() && explanationLines.length == 1) continue; // Bỏ qua nếu chỉ là dòng trống do split
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -leading);
                            tempCurrentY -= leading;
                        }
                        contentStream.endText();
                        currentY = tempCurrentY + leading; // Cập nhật lại currentY sau khi vẽ giải thích
                    }
                }
                // Add space between questions
                currentY -= 20;
                questionNumber++;
            }

            contentStream.close();
            document.save(filePath);
            System.out.println("PDF đã được xuất thành công tới: " + filePath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi xuất PDF: " + e.getMessage());
            return false;
        } finally {
            if (fontStreamRegular != null) {
                try {
                    fontStreamRegular.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String[] wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
        if (text == null || text.isEmpty()) {
            return new String[]{""}; // Trả về một dòng trống nếu text rỗng
        }
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] paragraphs = text.split("\n"); // Tách theo ký tự xuống dòng thủ công

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) { // Nếu là dòng trống do người dùng nhập \n\n
                lines.add("");
                continue;
            }
            String[] words = paragraph.split(" (?=([\\p{L}\\p{N}]+))"); // Split by space, but keep punctuation with word using lookahead for letter/number
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (word.isEmpty()) continue;

                float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
                float currentLineWidth = font.getStringWidth(currentLine.toString()) / 1000 * fontSize;
                float spaceWidth = currentLine.length() > 0 ? font.getStringWidth(" ") / 1000 * fontSize : 0;

                if (currentLineWidth + spaceWidth + wordWidth <= maxWidth || currentLine.length() == 0) {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                    // Kiểm tra nếu từ đơn lẻ đã dài hơn dòng
                    if (font.getStringWidth(currentLine.toString()) / 1000 * fontSize > maxWidth) {
                        // Xử lý từ quá dài: cắt từ
                        // Đây là một cách xử lý đơn giản, có thể cần cải thiện
                        String longWord = currentLine.toString();
                        currentLine = new StringBuilder();
                        StringBuilder partWord = new StringBuilder();
                        for(char c : longWord.toCharArray()){
                            if(font.getStringWidth(partWord.toString() + c) / 1000 * fontSize <= maxWidth){
                                partWord.append(c);
                            } else {
                                lines.add(partWord.toString());
                                partWord = new StringBuilder(String.valueOf(c));
                            }
                        }
                        if(partWord.length() > 0) {
                             lines.add(partWord.toString()); // Thêm phần còn lại của từ
                             currentLine = new StringBuilder(); // Reset currentLine vì đã xử lý xong từ dài
                        }

                    }
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        if (lines.isEmpty()) return new String[]{""}; // Đảm bảo luôn trả về một mảng
        return lines.toArray(new String[0]);
    }
}