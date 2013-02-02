package com.mityok;

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
}
