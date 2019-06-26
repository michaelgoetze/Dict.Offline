package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dictionary.Dict;
import dictionary.Entry;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 3858229905442780638L;
	private TableRowSorter<DictModel> exactSorter, partialSorter;
	private JTable exactTable, partialTable;
	private DictModel exactModel, partialModel;
	private JTextField queryField = new JTextField();
	private JButton searchButton = new JButton("Search");
	private static final String LINE_BREAK = "\r\n";
	private static final String CELL_BREAK = "\t";
	private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
	private static final Integer LANG1;
	private static final Integer LANG2;
	private static final Integer TYPE;
	private static final Integer CLASS;
	private static final Integer RELEVANCE;
	private boolean mDraggingColumn = false;
	private boolean mColumnCHangedIndex = false;
	private static float FONTSIZE = 12f;
	private Font f = new JLabel().getFont().deriveFont(FONTSIZE);
	private JPanel c = new JPanel();
	private JPanel sub1 = new JPanel();
	private JPanel sub2 = new JPanel();
	private JCheckBox autoFillBox = new JCheckBox("Auto-fill");
	private AutoSuggestor autoSuggestor;

	static {
		String[] colOrder = Dict.getProperty("COL_ORDER").split(",");
		if (colOrder.length == 5) {
			LANG1 = Integer.parseInt(colOrder[0]);
			LANG2 = Integer.parseInt(colOrder[1]);
			TYPE = Integer.parseInt(colOrder[2]);
			CLASS = Integer.parseInt(colOrder[3]);
			RELEVANCE = Integer.parseInt(colOrder[4]);
		} else {
			LANG1 = 0;
			LANG2 = 1;
			TYPE = 2;
			CLASS = 3;
			RELEVANCE = 4;
		}
	}

	public MainFrame() {

		setSize(1024, 768);

		exactModel = new DictModel();
		exactSorter = new TableRowSorter<DictModel>(exactModel);
		partialModel = new DictModel();
		partialSorter = new TableRowSorter<DictModel>(partialModel);
		setTitle("Dict.Offline");

		this.setIconImage(Dict.icon);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override  
			public void windowClosing(WindowEvent e) {
			    int confirmed = JOptionPane.showConfirmDialog(null, 
			        "Are you sure you want to exit the program?", "Exit Program Message Box",
			        JOptionPane.YES_NO_OPTION);

			    if (confirmed == JOptionPane.YES_OPTION) {
			      dispose();
			    }
			  }
			});

		List<TableRowSorter.SortKey> sortKeys = new ArrayList<TableRowSorter.SortKey>();
		sortKeys.add(new TableRowSorter.SortKey(RELEVANCE, SortOrder.DESCENDING));
		exactSorter.setSortKeys(sortKeys);
		partialSorter.setSortKeys(sortKeys);
		// create list for dictionary this in your case might be done via calling a
		// method which queries db and returns results as arraylist
		ArrayList<String> words = Dict.dict.getAllWords();
		autoSuggestor = new AutoSuggestor(queryField, this, words, Color.WHITE.brighter(), Color.black, Color.RED, 1f,
				FONTSIZE);

		exactTable = new JTable(exactModel) {
			private static final long serialVersionUID = -7358328224949460237L;

			// Implement table cell tool tips.
			@Override
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				int rowIndex = rowAtPoint(e.getPoint());
				int colIndex = columnAtPoint(e.getPoint());
				tip = String.valueOf(getValueAt(rowIndex, colIndex));
				return tip;
			}

			@Override
			public TableCellRenderer getCellRenderer(int arg0, int arg1) {
				int colIndex = exactTable.getColumnModel().getColumn(arg1).getModelIndex();
				if (colIndex == LANG1 || colIndex == LANG2) {
					return new HighLightRenderer();
				} else if (colIndex == RELEVANCE) {
					return new ScoreRenderer();
				} else {
					return new DefaultRenderer();
				}

			}

		};
		exactTable.getColumnModel().getColumn(LANG1).setMinWidth(200);//
		exactTable.getColumnModel().getColumn(LANG1).setMaxWidth(1200);//
		exactTable.getColumnModel().getColumn(LANG2).setMinWidth(200);// Nr.
		exactTable.getColumnModel().getColumn(LANG2).setMaxWidth(1200);//
		exactTable.getColumnModel().getColumn(TYPE).setMinWidth(80);// Nr.
		exactTable.getColumnModel().getColumn(TYPE).setMaxWidth(100);//
		exactTable.getColumnModel().getColumn(CLASS).setMinWidth(100);// Nr.
		exactTable.getColumnModel().getColumn(CLASS).setMaxWidth(200);//
		exactTable.getColumnModel().getColumn(RELEVANCE).setMinWidth(100);// Nr.
		exactTable.getColumnModel().getColumn(RELEVANCE).setMaxWidth(200);//

		exactTable.setColumnSelectionAllowed(true);
		exactTable.setRowSelectionAllowed(true);
		exactTable.setRowHeight(25);
		exactTable.setRowSorter(exactSorter);

		exactTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (mDraggingColumn && mColumnCHangedIndex) {
					String order[] = new String[5];
					int columnIndex = exactTable.getColumnModel().getColumn(0).getModelIndex();
					order[0] = exactModel.getColumnName(getColNumber(columnIndex));
					StringBuilder sb = new StringBuilder("" + getColNumber(columnIndex));
					for (int i = 1; i < 5; i++) {
						columnIndex = exactTable.getColumnModel().getColumn(i).getModelIndex();
						order[i] = exactModel.getColumnName(getColNumber(columnIndex));
						sb.append("," + getColNumber(columnIndex));
					}
					setColumnOrder(order, partialTable.getColumnModel());
					partialTable.repaint();
					repaint();
					Dict.updateIni("COL_ORDER", sb.toString());
				}
				mDraggingColumn = false;
				mColumnCHangedIndex = false;
			}

			private int getColNumber(int columnIndex) {
				if (columnIndex == LANG1) {
					return 0;
				} else if (columnIndex == LANG2) {
					return 1;
				} else if (columnIndex == TYPE) {
					return 2;
				} else if (columnIndex == CLASS) {
					return 3;
				} else if (columnIndex == RELEVANCE) {
					return 4;
				}
				return 0;
			}
		});

		exactTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				mDraggingColumn = true;
				if (e.getFromIndex() != e.getToIndex()) {
					mColumnCHangedIndex = true;
				}
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});

		final JPopupMenu exactPopupMenu = new JPopupMenu();
		JMenuItem exactCopyItem = new JMenuItem("Copy");
		exactCopyItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				copyToClipboard(exactTable);
			}
		});
		exactPopupMenu.add(exactCopyItem);
		exactTable.setComponentPopupMenu(exactPopupMenu);

		exactPopupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int[] rows = exactTable.getSelectedRows();
						int[] cols = exactTable.getSelectedColumns();

						Point p = SwingUtilities.convertPoint(exactPopupMenu, new Point(0, 0), exactTable);
						int rowAtPoint = exactTable.rowAtPoint(p);
						int colAtPoint = exactTable.columnAtPoint(p);
						boolean rowSelected = (Arrays.stream(cols).anyMatch(i -> i == colAtPoint));
						boolean colSelected = (Arrays.stream(rows).anyMatch(i -> i == rowAtPoint));
						if (!(rowSelected && colSelected)) {
							if ((colAtPoint > -1) && (rowAtPoint > -1)) {
								exactTable.setColumnSelectionInterval(colAtPoint, colAtPoint);
								exactTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
							}
						}

					}
				});
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}
		});

		partialTable = new JTable(partialModel) {
			private static final long serialVersionUID = -7358328224949460237L;

			// Implement table cell tool tips.
			@Override
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				int rowIndex = rowAtPoint(e.getPoint());
				int colIndex = columnAtPoint(e.getPoint());
				tip = String.valueOf(getValueAt(rowIndex, colIndex));
				return tip;
			}

			@Override
			public TableCellRenderer getCellRenderer(int arg0, int arg1) {
				int colIndex = exactTable.getColumnModel().getColumn(arg1).getModelIndex();
				if (colIndex == LANG1 || colIndex == LANG2) {
					return new HighLightRenderer();
				} else if (colIndex == RELEVANCE) {
					return new ScoreRenderer();
				} else {
					return new DefaultRenderer();
				}
			}

		};
		partialTable.getColumnModel().getColumn(LANG1).setMinWidth(200);
		partialTable.getColumnModel().getColumn(LANG1).setMaxWidth(1200);
		partialTable.getColumnModel().getColumn(LANG2).setMinWidth(200);
		partialTable.getColumnModel().getColumn(LANG2).setMaxWidth(1200);
		partialTable.getColumnModel().getColumn(TYPE).setMinWidth(80);
		partialTable.getColumnModel().getColumn(TYPE).setMaxWidth(100);
		partialTable.getColumnModel().getColumn(CLASS).setMinWidth(100);
		partialTable.getColumnModel().getColumn(CLASS).setMaxWidth(200);
		partialTable.getColumnModel().getColumn(RELEVANCE).setMinWidth(100);
		partialTable.getColumnModel().getColumn(RELEVANCE).setMaxWidth(200);

		partialTable.setColumnSelectionAllowed(true);
		partialTable.setRowSelectionAllowed(true);
		partialTable.setRowHeight(25);
		partialTable.setRowSorter(partialSorter);

		partialTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (mDraggingColumn && mColumnCHangedIndex) {
					String order[] = new String[5];
					int columnIndex = partialTable.getColumnModel().getColumn(0).getModelIndex();
					StringBuilder sb = new StringBuilder("" + getColNumber(columnIndex));
					order[0] = partialModel.getColumnName(getColNumber(columnIndex));
					for (int i = 1; i < 5; i++) {
						columnIndex = partialTable.getColumnModel().getColumn(i).getModelIndex();
						sb.append("," + getColNumber(columnIndex));
						order[i] = partialModel.getColumnName(getColNumber(columnIndex));
					}
					setColumnOrder(order, exactTable.getColumnModel());
					partialTable.repaint();
					repaint();
					Dict.updateIni("COL_ORDER", sb.toString());
				}
				mDraggingColumn = false;
				mColumnCHangedIndex = false;
			}

			private int getColNumber(int columnIndex) {
				if (columnIndex == LANG1) {
					return 0;
				} else if (columnIndex == LANG2) {
					return 1;
				} else if (columnIndex == TYPE) {
					return 2;
				} else if (columnIndex == CLASS) {
					return 3;
				} else if (columnIndex == RELEVANCE) {
					return 4;
				}
				return 0;
			}
		});

		partialTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				mDraggingColumn = true;
				if (e.getFromIndex() != e.getToIndex()) {
					mColumnCHangedIndex = true;
				}
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});

		final JPopupMenu partialPopupMenu = new JPopupMenu();
		JMenuItem partialCopyItem = new JMenuItem("Copy");
		partialCopyItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				copyToClipboard(partialTable);
			}
		});
		partialPopupMenu.add(partialCopyItem);
		partialTable.setComponentPopupMenu(partialPopupMenu);

		partialPopupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int[] rows = partialTable.getSelectedRows();
						int[] cols = partialTable.getSelectedColumns();

						Point p = SwingUtilities.convertPoint(partialPopupMenu, new Point(0, 0), partialTable);
						int rowAtPoint = partialTable.rowAtPoint(p);
						int colAtPoint = partialTable.columnAtPoint(p);
						boolean rowSelected = (Arrays.stream(cols).anyMatch(i -> i == colAtPoint));
						boolean colSelected = (Arrays.stream(rows).anyMatch(i -> i == rowAtPoint));
						if (!(rowSelected && colSelected)) {
							if ((colAtPoint > -1) && (rowAtPoint > -1)) {
								partialTable.setColumnSelectionInterval(colAtPoint, colAtPoint);
								partialTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
							}
						}

					}
				});
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		getRootPane().setDefaultButton(searchButton);

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextArea msgLabel;
				final JProgressBar progressBar;
				final int MAXIMUM = 100;
				JPanel panel;

				progressBar = new JProgressBar(0, MAXIMUM);
				progressBar.setIndeterminate(true);
				msgLabel = new JTextArea("Searching Dictionary");
				msgLabel.setEditable(false);
				msgLabel.setFont(f);

				panel = new JPanel(new BorderLayout(5, 5));
				panel.add(msgLabel, BorderLayout.PAGE_START);
				panel.add(progressBar, BorderLayout.CENTER);
				panel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

				final JDialog dialog = new JDialog();
				dialog.getContentPane().add(panel);
				dialog.setResizable(false);
				dialog.pack();
				dialog.setIconImage(Dict.icon);
				dialog.setSize(500, dialog.getHeight());
				dialog.setLocationRelativeTo(null);
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				msgLabel.setBackground(panel.getBackground());

				SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {

					@Override
					protected void done() {
						// Close the dialog
						dialog.dispose();
					}

					@Override
					protected Object doInBackground() throws Exception {
						String query = queryField.getText();
						queryField.selectAll();
						exactModel.clear();
						for (Entry entry : Dict.getExactEntries(query)) {
							exactModel.addEntry(entry);
						}
						if (exactModel.getRowCount() == 0) {
							exactModel.addEntry(new Entry("Kein Eintrag gefunden", "No entry found"));
						}
						exactModel.setQuery(query);
						repaint();
						partialModel.clear();
						for (Entry entry : Dict.getPartialMatchedEntries(query)) {
							partialModel.addEntry(entry);
						}
						if (partialModel.getRowCount() == 0) {
							partialModel.addEntry(new Entry("Kein Eintrag gefunden", "No entry found"));
						}
						partialModel.setQuery(query);
						repaint();
						return null;
					}

				};
				worker.execute();

			}

		});

		GridBagConstraints gc = new GridBagConstraints();
		c.setBorder(BorderFactory.createTitledBorder("Query"));
		sub1.setBorder(BorderFactory.createTitledBorder("Result"));
		sub2.setBorder(BorderFactory.createTitledBorder("Result matching beginning or end of word"));
		resetFontSize(Float.parseFloat(Dict.getPropertyOrDefault("FONTSIZE", String.valueOf(FONTSIZE))));

		setLayout(new GridBagLayout());

		c.setLayout(new GridBagLayout());
		sub1.setLayout(new GridBagLayout());
		sub2.setLayout(new GridBagLayout());

		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;
		gc.weighty = 0;
		gc.weightx = 1;
		gc.insets = new Insets(2, 2, 2, 2);

		c.add(queryField, gc);
		gc.gridx = 1;
		gc.weightx = 0;
		c.add(searchButton, gc);

		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridwidth = 2;
		gc.gridy++;
		sub1.add(new JScrollPane(exactTable), gc);
		sub2.add(new JScrollPane(partialTable), gc);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 0;
		add(c, gc);
		gc.weighty = 1;
		gc.gridy = 1;
		add(sub1, gc);
		gc.gridy = 2;
		add(sub2, gc);
	}

	public static void setColumnOrder(String[] header, TableColumnModel columnModel) {
		TableColumn column[] = new TableColumn[header.length];

		for (int i = 0; i < column.length; i++) {
			String tHeader = columnModel.getColumn(i).getHeaderValue().toString();
			for (int j = 0; j < header.length; j++) {
				if (tHeader.equals(header[j])) {
					column[j] = columnModel.getColumn(i);
				}
			}
		}

		while (columnModel.getColumnCount() > 0) {
			columnModel.removeColumn(columnModel.getColumn(0));
		}

		for (int i = 0; i < column.length; i++) {
			columnModel.addColumn(column[i]);
		}
	}

	private class HighLightRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = -1260600208698803499L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			String text = "";
			if (value != null) {
				text = "<html><body><p style=\"font-size:" + (int) FONTSIZE + "px\">" + value.toString()
						+ "</p></body></html>";
			}
			try {
				for (String word : ((DictModel) table.getModel()).getQuery().split("[\\s-]")) {
					text = text.replaceAll("(?i)(" + word + ")", "<b>$1</b>");
				}
			} catch (Exception e) {
			}
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			setText(text);
			// unbold
			Font f = this.getFont();
			f = f.deriveFont(f.getStyle() & ~Font.BOLD);
			setFont(f);
			setHorizontalAlignment(SwingConstants.LEFT);
			setOpaque(true);
			setBorder(new EmptyBorder(5, 5, 5, 5));// top,left,bottom,right
			return this;
		}
	}

	private class DefaultRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = -1260600208698803499L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			String text = "";
			if (value != null) {
				text = "<html><body><p style=\"font-size:" + (int) FONTSIZE + "px\">" + value.toString()
						+ "</p></body></html>";
			}
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			setText(text);
			// unbold
			Font f = this.getFont();
			f = f.deriveFont(f.getStyle() & ~Font.BOLD);
			setFont(f);
			setHorizontalAlignment(SwingConstants.CENTER);
			setOpaque(true);
			setBorder(new EmptyBorder(5, 5, 5, 5));// top,left,bottom,right
			return this;
		}
	}

	private class ScoreRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = -1260600208698803499L;

		ScoreRenderer() {
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			setText("");
			setHorizontalAlignment(SwingConstants.CENTER);
			int score = Integer.parseInt(value.toString());
			this.setBackground(scoreToColor(score, (DictModel) table.getModel()));
			setOpaque(true);
			return this;
		}

		private Color scoreToColor(int score, DictModel model) {
			Color color = new Color(0, 0, 0);

			double intensity = ((double) score - model.getMin()) / (model.getMax() - model.getMin());
			if (intensity > 1)
				intensity = 1;
			if (intensity < 0)
				intensity = 0;

			if (intensity >= 0) {
				int red, red0, red1, red2, green, green0, green1, green2, blue, blue0, blue1, blue2;
				double colorPos = 0.3;
				red0 = 250;
				green0 = 180;
				blue0 = 0;

				red1 = 250;
				green1 = 250;
				blue1 = 126;

				red2 = 54;
				green2 = 200;
				blue2 = 26;

				if (intensity < colorPos) {
					red = (int) ((intensity / colorPos) * (red1 - red0) + red0);
					green = (int) ((intensity / colorPos) * (green1 - green0) + green0);
					blue = (int) ((intensity / colorPos) * (blue1 - blue0) + blue0);

				} else {
					red = (int) (((intensity - colorPos) / (1 - colorPos)) * (red2 - red1) + red1);
					green = (int) (((intensity - colorPos) / (1 - colorPos)) * (green2 - green1) + green1);
					blue = (int) (((intensity - colorPos) / (1 - colorPos)) * (blue2 - blue1) + blue1);
				}
				color = new Color(red, green, blue);
			}
			return color;
		}
	}

	private void resetFontSize(float size) {
		FONTSIZE = size;
		Dict.updateIni("FONTSIZE", String.valueOf(FONTSIZE));
		f = new JLabel().getFont().deriveFont(FONTSIZE);
		exactTable.setRowHeight((int) size + 10);
		partialTable.setRowHeight((int) size + 10);
		autoSuggestor.setFont(f);
		exactTable.setFont(f);
		partialTable.setFont(f);
		autoFillBox.setFont(f);
		((TitledBorder) c.getBorder()).setTitleFont(f);
		((TitledBorder) sub1.getBorder()).setTitleFont(f);
		((TitledBorder) sub2.getBorder()).setTitleFont(f);
		exactTable.getTableHeader().setFont(f);
		partialTable.getTableHeader().setFont(f);
		searchButton.setFont(f);
		queryField.setFont(f);
		this.setFont(f);
		JMenuBar menu = createMenuBar();
		menu.setFont(f);
		this.setJMenuBar(menu);

		repaint();
	}

	private void copyToClipboard(JTable table) {

		int numCols = table.getSelectedColumnCount();
		int numRows = table.getSelectedRowCount();
		int[] rowsSelected = table.getSelectedRows();
		int[] colsSelected = table.getSelectedColumns();
		if (numRows != rowsSelected[rowsSelected.length - 1] - rowsSelected[0] + 1 || numRows != rowsSelected.length
				|| numCols != colsSelected[colsSelected.length - 1] - colsSelected[0] + 1
				|| numCols != colsSelected.length) {

			JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		StringBuffer excelStr = new StringBuffer();
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {

				excelStr.append(escape(table.getValueAt(rowsSelected[i], colsSelected[j])).replaceAll("<.+?>", ""));
				if (j < numCols - 1) {
					excelStr.append(CELL_BREAK);
				}
			}
			excelStr.append(LINE_BREAK);
		}

		StringSelection sel = new StringSelection(excelStr.toString());
		CLIPBOARD.setContents(sel, sel);
	}

	private String escape(Object cell) {
		return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
	}

	private class DictModel implements TableModel {
		private ArrayList<Entry> entryArrayList = new ArrayList<Entry>();
		private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
		private String query;
		private int min = Integer.MAX_VALUE, max = 0;

		private void addEntry(Entry entry) {
			// Das wird der Index des Vehikels werden
			int index = entryArrayList.size();
			entryArrayList.add(entry);
			min = Math.min(min, entry.getRelevance());
			max = Math.max(max, entry.getRelevance());
			// Jetzt werden alle Listeners benachrichtigt

			// Zuerst ein Event, "neue Row an der Stelle index" herstellen
			TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS,
					TableModelEvent.INSERT);

			// Nun das Event verschicken
			for (int i = 0, n = listeners.size(); i < n; i++) {
				listeners.get(i).tableChanged(e);
			}
		}

		private void clear() {
			entryArrayList.clear();
			min = Integer.MAX_VALUE;
			max = 0;
			TableModelEvent e = new TableModelEvent(this);

			// Nun das Event verschicken
			for (int i = 0, n = listeners.size(); i < n; i++) {
				listeners.get(i).tableChanged(e);
			}
		}

		// Die Anzahl Columns
		@Override
		public int getColumnCount() {
			return 5;
		}

		// Die Anzahl and Candidate
		@Override
		public int getRowCount() {
			return entryArrayList.size();
		}

		// Die Titel der einzelnen Columns
		@Override
		public String getColumnName(int column) {
			if (column == LANG1) {
				return Dict.dict.getLanguage1();
			} else if (column == LANG2) {
				return Dict.dict.getLanguage2();
			} else if (column == TYPE) {
				return "Type";
			} else if (column == CLASS) {
				return "Classes";
			} else if (column == RELEVANCE) {
				return "Relevance";
			} else {
				return null;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Entry entry = entryArrayList.get(rowIndex);
			if (columnIndex == LANG1) {
				return entry.getLanguage1();
			} else if (columnIndex == LANG2) {
				return entry.getLanguage2();
			} else if (columnIndex == TYPE) {
				return entry.getType();
			} else if (columnIndex == CLASS) {
				return entry.getClassification();
			} else if (columnIndex == RELEVANCE) {
				return entry.getRelevance();
			} else {
				return null;
			}

		}

		// Eine Angabe, welchen Typ von Objekten in den Columns angezeigt
		// werden soll
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int columnIndex) {
			if (columnIndex == RELEVANCE) {
				return Integer.class;
			} else {
				return String.class;
			}
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}

	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;
		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("Start");
		menu.setFont(f);
		menu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(menu);

		menuItem = new JMenuItem("Load Dictionary-File");
		menuItem.setFont(f);
		menuItem.setToolTipText("Load Dict.cc dictionary file");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dictFile = "";
				Frame f = new Frame("dummy");
				f.setIconImage(Dict.icon);
				JFileChooser fc = new JFileChooser(Dict.getHomeDir());
				int returnVal = fc.showOpenDialog(f);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					dictFile = fc.getSelectedFile().getAbsolutePath();
					if (new File(dictFile).exists()) {
						Dict.loadDictionary(dictFile);
						Dict.updateIni("DICT_DIR", dictFile);
						MainFrame.this.setVisible(false);
					}
				}
			}
		});
		menu.add(menuItem);

		autoFillBox.setSelected(!Dict.getProperty("AUTOFILL").equalsIgnoreCase("false"));
		autoSuggestor.setActive(autoFillBox.isSelected());
		autoFillBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Dict.updateIni("AUTOFILL", String.valueOf(autoFillBox.isSelected()));
				autoSuggestor.setActive(autoFillBox.isSelected());
			}
		});
		menu.add(autoFillBox);

		JMenu subMenu = new JMenu("Font-size:");
		subMenu.setFont(f);
		menu.add(subMenu);
		menuItem = new JMenuItem("10px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(10f);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JMenuItem("11px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(11f);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JMenuItem("12px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(12f);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JMenuItem("13px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(13f);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JMenuItem("14px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(14f);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JMenuItem("15px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(15f);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JMenuItem("16px");
		menuItem.setFont(f);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetFontSize(16f);
			}
		});
		subMenu.add(menuItem);

		menuItem = new JMenuItem("Exit");
		menuItem.setFont(f);
		menuItem.setToolTipText("Close program");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.dispose();
				System.exit(0);
			}
		});
		menu.add(menuItem);

		return menuBar;

	}

}
