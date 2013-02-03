package com.mityok.table;

import java.awt.Component;
import java.awt.Label;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class DateRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value == null) {
			this.setText(null);
		} else {
			Date v = (Date) value;
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			this.setText(format.format(v));
		}
		return this;
	}
}
