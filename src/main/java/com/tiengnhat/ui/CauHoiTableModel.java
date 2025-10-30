package com.tiengnhat.ui;

import com.tiengnhat.model.CauHoi;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class CauHoiTableModel extends AbstractTableModel {
    private List<CauHoi> danhSachCauHoi;
    // Cập nhật tên cột cho phù hợp hơn
    private final String[] columnNames = {"ID", "Nội dung câu hỏi", "Loại", "Độ khó", "Tags", "Audio"};

    public CauHoiTableModel(List<CauHoi> danhSachCauHoi) {
        this.danhSachCauHoi = new ArrayList<>(danhSachCauHoi);
    }

    public void setData(List<CauHoi> danhSachCauHoi) {
        this.danhSachCauHoi = new ArrayList<>(danhSachCauHoi);
        fireTableDataChanged(); // Quan trọng: Thông báo cho JTable cập nhật
    }

    @Override
    public int getRowCount() {
        return danhSachCauHoi.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= danhSachCauHoi.size()) {
            return null;
        }
        CauHoi cauHoi = danhSachCauHoi.get(rowIndex);
        switch (columnIndex) {
            case 0: return cauHoi.getMaCauHoi();
            case 1:
                String noiDung = cauHoi.getNoiDungCauHoi();
                return noiDung.length() > 100 ? noiDung.substring(0, 100) + "..." : noiDung;
            case 2: return cauHoi.getLoaiCauHoi();
            case 3: return cauHoi.getDoKho();
            case 4: return cauHoi.getNhan();
            case 5:
                // Có thể dùng icon ở đây nếu muốn
                return (cauHoi.getDuongDanFileAmThanh() != null && !cauHoi.getDuongDanFileAmThanh().isEmpty()) ? "Có" : "Không";
            default: return null;
        }
    }

    // Phương thức này rất hữu ích để lấy đối tượng CauHoi từ một dòng đã chọn
    public CauHoi getCauHoiAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < danhSachCauHoi.size()) {
            return danhSachCauHoi.get(rowIndex);
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Không cho phép sửa trực tiếp trên bảng
    }
}
