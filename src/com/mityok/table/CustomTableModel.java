package com.mityok.table;

import java.util.Date;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public CustomTableModel(Object[][] object, String[] columnNames) {
		super(object, columnNames);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column != 1) {
			return true;
		} else {
			return false;
		}
	}

	public Class getColumnClass(int column) {
		Object obj = getValueAt(0, column);
		if (column == 4) {
			return Date.class;
		}
		return obj.getClass();
	}
}
