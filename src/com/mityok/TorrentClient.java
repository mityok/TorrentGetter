package com.mityok;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.mityok.inter.PopulateTable;
import com.mityok.inter.TableDataHahdler;
import com.mityok.inter.TorrentLoadUpdateHandler;
import com.mityok.model.AirDateData;
import com.mityok.table.CustomTableModel;
import com.mityok.table.DateEditor;
import com.mityok.table.DateRenderer;

public class TorrentClient extends JFrame implements PopulateTable {

	private static final int FRAME_WIDTH = 700;
	private static final long serialVersionUID = 1L;
	TrayIcon trayIcon;
	SystemTray tray;

	private InfoHolder info;
	private JTable table;
	private TorrentValidator validator;
	private DefaultTableModel defaultTableModel;

	public TorrentClient(String string) {
		this.setSize(FRAME_WIDTH, 500);
		setTitle(string);
		if (SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();
		}
		validator = new TorrentValidator();

		addWindowStateListener(new WindowStateListener() {

			@Override
			public void windowStateChanged(WindowEvent e) {
				System.out.println(e.toString());
				if (e.getNewState() == ICONIFIED) {
					try {
						tray.add(trayIcon);
						setVisible(false);
						System.out.println("added to SystemTray");
					} catch (AWTException ex) {
						System.out.println("unable to add to tray");
					}
				} else if (e.getNewState() == NORMAL) {
					tray.remove(trayIcon);
					setVisible(true);
				}

			}
		});
		Image image = new ImageIcon(this.getClass().getResource(
				"images/icon.png")).getImage();
		this.setIconImage(image);
		PopupMenu popup = new PopupMenu();
		MenuItem defaultItem = new MenuItem("Open");
		defaultItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(true);
				setExtendedState(JFrame.NORMAL);

			}
		});
		popup.add(defaultItem);
		//
		defaultItem = new MenuItem("Exit");
		defaultItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Exiting....");
				System.exit(0);

			}
		});
		popup.add(defaultItem);
		trayIcon = new TrayIcon(image, "SystemTray Demo", popup);
		trayIcon.setImageAutoSize(true);
		// load torrent

	}

	public static void main(String[] args) {
		TorrentClient window = new TorrentClient("TorrentClient");

		window.addComponentsToPane(window.getContentPane());

		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.initData();
	}

	private void initData() {
		info = new InfoHolder(this);
		validator.setDataGetter(new TableDataHahdler() {
			public Object[][] init() {
				return info.getData();
			}
		});
		validator.init();
		validator.handleUpdate(new TorrentLoadUpdateHandler() {

			@Override
			public void update(List<AirDateData> links) {
				for (AirDateData airDateData : links) {
					if (airDateData.isLoading()) {
						Object[] obj = new Object[6];
						obj[0] = airDateData.getTitle();
						obj[1] = airDateData.getImdbLink();
						obj[2] = airDateData.getSeason();
						obj[3] = airDateData.getEpisode();
						obj[4] = airDateData.getDate();
						obj[5] = airDateData.isByDate();
						info.updateRow(obj);
					}
				}
				populate(info.getData());
			}
		});
	}

	private void addComponentsToPane(Container pane) {
		final JTextField titleField = new JTextField(20);
		final JTextField imdbField = new JTextField(20);
		SpinnerModel sm = new SpinnerNumberModel(0, 0, 100, 1);
		final JSpinner episodeField = new JSpinner(sm);
		final JSpinner seasonField = new JSpinner(sm);
		final JFormattedTextField dateField = new JFormattedTextField(
				new SimpleDateFormat("dd/MM/yyyy"));
		final JCheckBox checkBox=new JCheckBox();
		JButton applyButton = new JButton("ADD NEW");
		applyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("sdfsd: " + checkBox.isSelected());
				if (!titleField.getText().isEmpty()
						&& !imdbField.getText().isEmpty()) {
					Object[] obj = new Object[6];
					obj[0] = titleField.getText();
					obj[1] = imdbField.getText();
					obj[2] = (Integer) seasonField.getValue();
					obj[3] = (Integer) episodeField.getValue();
					obj[4] = (Date) dateField.getValue();
					obj[5] = (Boolean) checkBox.isSelected();
					info.addNewItem(obj);
					titleField.setText("");
					imdbField.setText("");
					seasonField.setValue(0);
					episodeField.setValue(0);
					dateField.setValue(null);
					defaultTableModel.addRow(obj);
				}
			}
		});
		JPanel controls = new JPanel();
		controls.setLayout(new GridLayout(2, 7));
		//
		controls.add(new Label("TITLE:"));
		controls.add(new Label("IMDB LINK:"));
		controls.add(new Label("LAST SEASON"));
		controls.add(new Label("LAST EPISODE"));
		controls.add(new Label("LAST DATE"));
		controls.add(new Label("BY DATE"));
		controls.add(new Label(" "));
		controls.add(titleField);
		controls.add(imdbField);
		controls.add(seasonField);
		controls.add(episodeField);
		controls.add(dateField);
		controls.add(checkBox);
		controls.add(applyButton);
		controls.setPreferredSize(new Dimension(FRAME_WIDTH, 40));
		//
		String[] columnNames = { "Title", "Imdb", "Season", "Episode", "Date" ,"Is By Date"};

		table = new JTable();
		defaultTableModel = new CustomTableModel(null, columnNames);
		table.setModel(defaultTableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(new Dimension(FRAME_WIDTH, 70));
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollPane = new JScrollPane(table);
//
		
		table.setDefaultRenderer(Date.class, new DateRenderer());
        table.setDefaultEditor(Date.class, new DateEditor());
		// Add the scroll pane to this panel.
		add(scrollPane);
		scrollPane.setPreferredSize(new Dimension(FRAME_WIDTH, 400));
		//
		JToolBar toolBar = new JToolBar();
		JButton resetButton = new JButton("RESET");
		toolBar.add(resetButton);
		//
		final JButton refreshButton = new JButton("REFRESH");
		toolBar.add(refreshButton);
		//
		final JButton deleteButton = new JButton("DELETE");
		toolBar.add(deleteButton);
		//
		toolBar.setFloatable(false);
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				validator.init();
			}
		});
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int dialogButton = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog(
						TorrentClient.this,
						"Would You Like to Clear AllYour Data?", "Warning",
						dialogButton);
				if (dialogResult == JOptionPane.YES_OPTION) {
					info.resetAll();
					clearTable();
				}
			}
		});
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = table.getSelectedRow();
				info.removeRow(getRowData(selectedRow));
				defaultTableModel.removeRow(selectedRow);
			}
		});

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						int[] selectedRows = table.getSelectedRows();
						deleteButton.setEnabled(selectedRows.length > 0);
					}
				});
		defaultTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent arg0) {
				if (TableModelEvent.UPDATE == arg0.getType()) {
					Object[] rowData = getRowData(arg0.getLastRow());
					info.updateRow(rowData);
				}
			}
		});
		pane.add(toolBar, BorderLayout.PAGE_START);
		pane.add(scrollPane, BorderLayout.CENTER);
		pane.add(controls, BorderLayout.PAGE_END);
	}

	private Object[] getRowData(int rowIndex) {
		if (rowIndex >= defaultTableModel.getRowCount()) {
			return null;
		}
		Object[] rowData = new Object[table.getColumnCount()];
		for (int i = 0; i < table.getColumnCount(); i++) {
			rowData[i] = table.getValueAt(rowIndex, i);
		}
		return rowData;
	}

	private void clearTable() {
		while (defaultTableModel.getRowCount() > 0) {
			defaultTableModel.removeRow(0);
		}
	}

	@Override
	public void populate(Object[][] data) {
		clearTable();
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				((DefaultTableModel) table.getModel()).addRow(data[i]);
			}
		}
	}
}
