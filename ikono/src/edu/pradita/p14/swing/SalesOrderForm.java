package edu.pradita.p14.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class SalesOrderForm extends JPanel implements IForm {

	private static final long serialVersionUID = -4059202279774436603L;
	private JTable table;
	private JTextField txtTotal;
	private JTextField txtCode;
	private JTextField txtDate;
	private JTextArea txtNote;

	public boolean isAddMode = false;
	private JButton btnAddItem;
	private JButton btnDeleteItem;
	private JButton btnConfirm;

	/**
	 * Create the application.
	 * 
	 * @param mainForm
	 * 
	 * @throws SQLException
	 */
	public SalesOrderForm(MainForm mainForm) throws SQLException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws SQLException
	 */
	private void initialize() throws SQLException {

		this.setFont(new Font("Tahoma", Font.PLAIN, 16));
		this.setBounds(100, 100, 893, 421);
		setLayout(new BorderLayout(0, 0));

		JPanel southPanel = new JPanel();
		this.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		southPanel.add(panel);

		JLabel lblTotal = new JLabel("Total");
		lblTotal.setFont(new Font("Dialog", Font.BOLD, 20));
		panel.add(lblTotal);

		txtTotal = new JTextField();
		txtTotal.setFont(new Font("Dialog", Font.BOLD, 20));
		txtTotal.setEditable(false);
		txtTotal.setColumns(10);
		panel.add(txtTotal);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_1.getLayout();
		flowLayout_1.setAlignment(FlowLayout.RIGHT);
		southPanel.add(panel_1);

		btnConfirm = new JButton("Confirm");
		btnConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PreparedStatement statement;
				try {
					// get max code
					statement = MainForm.CONNECTION.prepareStatement("SELECT Max(code) code FROM `order`;");
					ResultSet resultSet = statement.executeQuery();
					String maxCode = null;
					if (resultSet.next()) {
						maxCode = resultSet.getString("code");
					}
					String newCode = String.valueOf(Integer.valueOf(maxCode) + 1);
					resultSet.close();
					statement.close();

					statement = MainForm.CONNECTION.prepareStatement("insert into `order`(code, note) values(?, ?);");
					statement.setString(1, newCode);
					statement.setString(2, txtNote.getText());
					statement.executeUpdate();
					statement.close();

					for (int i = 0; i < table.getRowCount(); i++) {
						statement = MainForm.CONNECTION.prepareStatement(
								"insert into `order_detail`(code, line, itemcode, name, price, quantity)"
										+ " values(?, ?, ?, ?, ?, ?);");
						statement.setString(1, newCode);
						statement.setInt(2, (int) table.getValueAt(i, 0));
						statement.setString(3, (String) table.getValueAt(i, 1));
						statement.setString(4, (String) table.getValueAt(i, 2));
						statement.setDouble(5, Double.valueOf(table.getValueAt(i, 3).toString()));
						statement.setDouble(6, Double.valueOf(table.getValueAt(i, 4).toString()));
						statement.executeUpdate();

						// update the stock
						statement = MainForm.CONNECTION
								.prepareStatement("UPDATE item SET quantity = quantity - ? " + "WHERE code = ?;");
						statement.setDouble(1, Double.valueOf(table.getValueAt(i, 4).toString()));
						statement.setString(2, (String) table.getValueAt(i, 1));
						statement.executeUpdate();
					}

					isAddMode = false;
					enableDisableElements();

					displayLastOrder();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}
		});
		btnConfirm.setFont(new Font("Tahoma", Font.PLAIN, 20));
		panel_1.add(btnConfirm);
		JScrollPane scrollPane = new JScrollPane();
		this.add(scrollPane);

		table = new JTable();
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("Tahoma", Font.PLAIN, 16));
		scrollPane.setViewportView(table);
		table.setModel(new DefaultTableModel(new Object[][] { { null, null, null, null, null, null }, },
				new String[] { "No.", "Item Code", "Name", "Price", "Quantity", "Total" }) {
			private static final long serialVersionUID = 3305501380893558451L;
			Class<?>[] columnTypes = new Class[] { Integer.class, String.class, String.class, Double.class,
					Double.class, Double.class };

			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, false, false, false, true, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});

		JPanel northPanel = new JPanel();
		this.add(northPanel, BorderLayout.NORTH);
		GridBagLayout gbl_northPanel = new GridBagLayout();
		gbl_northPanel.columnWidths = new int[] { 709, 0 };
		gbl_northPanel.rowHeights = new int[] { 50, 78, 0 };
		gbl_northPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_northPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		northPanel.setLayout(gbl_northPanel);

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_2.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		northPanel.add(panel_2, gbc_panel_2);

		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isAddMode = true;
				enableDisableElements();
				txtCode.setText("");
				txtNote.setText("");
				txtDate.setText("");
				txtTotal.setText("");
				table.setEnabled(true);
				DefaultTableModel dtm = (DefaultTableModel) table.getModel();
				while (dtm.getRowCount() > 0) {
					dtm.removeRow(0);
				}
			}
		});
		btnNew.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnNew);

		JButton btnFind = new JButton("Find");
		btnFind.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnFind);

		JLabel lblNewLabel = new JLabel("Code");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(lblNewLabel);

		txtCode = new JTextField();
		txtCode.setEditable(false);
		txtCode.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtCode.setColumns(10);
		panel_2.add(txtCode);

		JButton btnFirst = new JButton("First");
		btnFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SalesOrderForm.this.displayFirstOrder();
			}
		});
		btnFirst.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnFirst);

		JButton btnPrevious = new JButton("Prev");
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SalesOrderForm.this.displayPrevOrder();
			}
		});
		btnPrevious.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnPrevious);

		JButton btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SalesOrderForm.this.displayNextOrder();
			}
		});
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnNext);

		JButton btnLast = new JButton("Last");
		btnLast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SalesOrderForm.this.displayLastOrder();
			}
		});
		btnLast.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnLast);

		JLabel lblDate = new JLabel("Date");
		lblDate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(lblDate);

		txtDate = new JTextField();
		txtDate.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDate.setEditable(false);
		txtDate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtDate.setColumns(12);
		panel_2.add(txtDate);

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_4.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.anchor = GridBagConstraints.SOUTH;
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		northPanel.add(panel_4, gbc_panel_4);

		btnAddItem = new JButton("Add Item");
		btnAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String query = "SELECT code, name, price FROM item WHERE quantity > 0;";
				SelectForm selectForm = new SelectForm(query);
				selectForm.setOnSelectListener(new OnSelectListener() {
					@Override
					public void select(Object[] values) {
						if (values != null && values.length > 0) {
							DefaultTableModel dtm = (DefaultTableModel) table.getModel();

							int line = dtm.getRowCount() + 1;
							String code = (String) values[0];
							String name = (String) values[1];
							double price = ((BigDecimal) values[2]).doubleValue();
							double quantity = 1;
							double lineTotal = price * quantity;

							Object[] orderLine = new Object[] { line, code, name, price, 1, 1 * price, lineTotal };
							dtm.addRow(orderLine);

							setTotalOrder(dtm);
						}
					}
				});
				selectForm.setVisible(true);

			}
		});
		btnAddItem.setFont(new Font("Tashoma", Font.PLAIN, 16));
		panel_4.add(btnAddItem);

		btnDeleteItem = new JButton("Remove Item");
		btnDeleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = table.getSelectedRow();
				if (selectedIndex < 0)
					return;
				DefaultTableModel dtm = (DefaultTableModel) table.getModel();
				dtm.removeRow(selectedIndex);
				for (int i = 0; i < dtm.getRowCount(); i++) {
					dtm.setValueAt(i + 1, i, 0);
				}

				setTotalOrder(dtm);
			}
		});
		btnDeleteItem.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_4.add(btnDeleteItem);

		Component rigidArea_1 = Box.createRigidArea(new Dimension(100, 20));
		panel_4.add(rigidArea_1);

		JLabel lblNote = new JLabel("Note");
		lblNote.setVerticalAlignment(SwingConstants.TOP);
		lblNote.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_4.add(lblNote);

		txtNote = new JTextArea();
		txtNote.setRows(3);
		txtNote.setColumns(30);
		panel_4.add(txtNote);
		table.getColumnModel().getColumn(0).setPreferredWidth(27);
		table.getColumnModel().getColumn(1).setPreferredWidth(64);
		table.getColumnModel().getColumn(2).setPreferredWidth(195);
		table.getColumnModel().getColumn(3).setPreferredWidth(68);
		table.getColumnModel().getColumn(4).setPreferredWidth(50);
		table.getColumnModel().getColumn(5).setPreferredWidth(108);
		table.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("tableCellEditor".equals(evt.getPropertyName()) && evt.getOldValue() != null) {
					DefaultTableModel dtm = (DefaultTableModel) table.getModel();
					int selectedIndex = table.getSelectedRow();
					DefaultCellEditor temp = (DefaultCellEditor) evt.getOldValue();
					double quantity = (double) temp.getCellEditorValue();
					double price = (double) dtm.getValueAt(selectedIndex, 3);
					double lineTotal = price * quantity;
					dtm.setValueAt(lineTotal, selectedIndex, 5);

					setTotalOrder(dtm);

				}
			}

		});

		displayLastOrder();

	}

	private void displayFirstOrder() {
		try {
			PreparedStatement statement = MainForm.CONNECTION
					.prepareStatement("select * from `order` t1 where t1.code = (select min(code)  from `order` t2);");
			displayOrder(statement);
			isAddMode = false;
			enableDisableElements();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void displayPrevOrder() {
		try {
			PreparedStatement statement = MainForm.CONNECTION.prepareStatement(
					"select * from `order` t1 where t1.code = (select max(code)  from `order` t2 where t2.code < ?) limit 1;");
			String currentCode = txtCode.getText();
			statement.setString(1, currentCode);
			displayOrder(statement);
			isAddMode = false;
			enableDisableElements();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void displayNextOrder() {
		try {
			PreparedStatement statement = MainForm.CONNECTION.prepareStatement(
					"select * from `order` t1 where t1.code = (select min(code)  from `order` t2 where t2.code > ?) limit 1;");
			String currentCode = txtCode.getText();
			statement.setString(1, currentCode);
			displayOrder(statement);
			isAddMode = false;
			enableDisableElements();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void displayLastOrder() {
		try {
			PreparedStatement statement = MainForm.CONNECTION.prepareStatement(
					"select * from `order` t1 where t1.code = (select max(code)  from `order` t2) limit 1");
			displayOrder(statement);
			isAddMode = false;
			enableDisableElements();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void displayOrder(PreparedStatement statement) throws SQLException {
		// initial order header query
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			String code = resultSet.getString("code");
			txtCode.setText(code);
			txtDate.setText(resultSet.getString("date"));
			txtNote.setText(resultSet.getString("note"));
			resultSet.close();
			statement.close();

			// detail order detail query
			statement = MainForm.CONNECTION
					.prepareStatement("select line, itemcode, name, price, quantity, (quantity * price) total "
							+ "from `order_detail` t1 where t1.code = ?");
			statement.setString(1, code);
			resultSet = statement.executeQuery();

			double grandTotal = 0;
			DefaultTableModel dtm = (DefaultTableModel) table.getModel();
			while (dtm.getRowCount() > 0) {
				dtm.removeRow(0);
			}
			while (resultSet.next()) {
				int line = resultSet.getInt("line");
				String itemcode = resultSet.getString("itemcode");
				String name = resultSet.getString("name");
				double quantity = resultSet.getInt("quantity");
				double price = resultSet.getInt("price");
				double total = resultSet.getInt("total");
				grandTotal = grandTotal + total;

				dtm.addRow(new Object[] { line, itemcode, name, price, quantity, total });
			}

			setTotalOrder(dtm);
		}

		resultSet.close();
		statement.close();
	}

	public void enableDisableElements() {
		if (isAddMode) {
			table.setEnabled(true);
			btnAddItem.setEnabled(true);
			btnDeleteItem.setEnabled(true);
			btnConfirm.setEnabled(true);
			txtNote.setEnabled(true);
		} else {
			table.setEnabled(false);
			btnAddItem.setEnabled(false);
			btnDeleteItem.setEnabled(false);
			btnConfirm.setEnabled(false);
			txtNote.setEnabled(true);
		}
	}

	private void setTotalOrder(DefaultTableModel dtm) {
		double total = 0;
		for (int i = 0; i < dtm.getRowCount(); i++) {
			double t = (double) dtm.getValueAt(i, 5);
			total = total + t;
		}
		txtTotal.setText(String.valueOf(total));
	}

	@Override
	public String getDocumentCode() {
		return txtCode.getText();
	}
}
