package com.tiengnhat.ui;

import com.tiengnhat.model.DeThi;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class DeThiTableModel extends AbstractTableModel {
    private List<DeThi> danhSachDeThi;
    private final String[] columnNames = {"ID", "Tên đề thi", "Mô tả", "Số câu hỏi", "Trộn câu hỏi"};

    public DeThiTableModel(List<DeThi> danhSachDeThi) {
        this.danhSachDeThi = new ArrayList<>(danhSachDeThi);
    }

    public void setData(List<DeThi> danhSachDeThi) {
        this.danhSachDeThi = new ArrayList<>(danhSachDeThi);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return danhSachDeThi.size();
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
        if (rowIndex < 0 || rowIndex >= danhSachDeThi.size()) {
            return null;
        }
        DeThi deThi = danhSachDeThi.get(rowIndex);
        switch (columnIndex) {
            case 0: return deThi.getMaDeThi();
            case 1: return deThi.getTenDeThi();
            case 2: 
                String moTa = deThi.getMoTa();
                return moTa != null && moTa.length() > 100 ? moTa.substring(0, 100) + "..." : moTa;
            case 3: return deThi.getTongSoCauHoi();
            case 4: return deThi.isChoPhepTronCauHoi() ? "Có" : "Không";
            default: return null;
        }
    }

    public DeThi getDeThiAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < danhSachDeThi.size()) {
            return danhSachDeThi.get(rowIndex);
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}