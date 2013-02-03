package com.mityok.table;

import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class DateEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFormattedTextField vr;

	public DateEditor() {
		vr = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));

	}

	@Override
	public Object getCellEditorValue() {
		Date date = null;
		try {
			vr.commitEdit();
			date = (Date) vr.getValue();
		} catch (ParseException e) {
			//parsing error - ignore
		}
		return date;
	}

	@Override
	public Component getTableCellEditorComponent(JTable arg0, Object arg1,
			boolean arg2, int arg3, int arg4) {
		Date v = (Date) arg1;
		vr.setValue(v);
		return vr;
	}
}
