/**
 * @file BuilderGUI.java
 * @author Jia Chen
 * @date 09/06/2011
 * @description 
 * 		BuilderGUI.java displays the deck building client. Allowing 
 * 		users to save and load decks as well as customize them
 */

/**
 * drag and drop (not possible currently, need research)
 * print deck with card translation and image (research)
 */

package DeckBuilder;

import java.awt.*;
/*import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;*/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
//import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

//import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.UIManager.*;

import CardAssociation.*;

public class BuilderGUI extends JFrame {

	private static final long serialVersionUID = -6401235618421175285L;

	// private properties of BuilderGUI

	private Box searchBox;
	private Box listBox;
	private JMenuBar menu;
	private JPanel cardInfo;
	private Box deckList;

	// private JMenuItem newD;
	// private JMenuItem save;
	// private JMenuItem load;
	// private JMenuItem exit;

	private JButton newdb;
	private JButton saveb;
	private JButton loadb;
	private JButton exitb;

	private Deck currentDeck;
	private boolean changes;
	private Card selectedCard;

	private final int OFFSET = 35;
	private final int DECKPERLINE = 10;
	private final int RESULTPERLINE = 10;
	private final int MAXIMUMRESULTSHOWN = 500;

	private JFileChooser fc;
	private ArrayList<Card> completeList;
	private HashMap<String, Card> cardHolder;

	private ArrayList<Card> resultList;
	private File file;

	// private static String datafile;

	// UI Components (Panes)
	private JTabbedPane resultArea;
	private JScrollPane resultPane;
	private JScrollPane resultThumbPane;

	private JTabbedPane deckArea;
	private JScrollPane deckPane;
	private JScrollPane extraDeckPane;

	// Header for result area
	private JLabel resultHeader;

	// Components for tables
	private TableCellRenderer cardIDRenderer;
	private TableCellRenderer numberRenderer;
	private TableCellRenderer statCompRenderer;
	private JTable resultListTable;
	private ResultListTableModel resultListModel;
	private JTable deckListTable;
	private DeckListTableModel deckListModel;
	private JTable extraDeckListTable;
	private DeckListTableModel extraDeckListModel;

	// Text in stats box
	private JLabel cardCountText;
	private JLabel charaCountText;
	private JLabel extraCountText;
	private JLabel eventCountText;
	private JLabel setCountText;

	private JComponent previousFocus;

	private JScrollPane deckThumbPane;

	/**
	 * Start the GUI client
	 */
	public BuilderGUI() {
		super("ChaOS Deck Builder");

		resultList = new ArrayList<Card>();

		searchBox = Box.createVerticalBox();
		listBox = Box.createVerticalBox();
		listBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Search Result"), null));
		listBox.setPreferredSize(new Dimension(520, 500));
		menu = new JMenuBar();
		cardInfo = new JPanel();
		deckList = Box.createHorizontalBox();
		deckList.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Deck List"), null));

		fc = new JFileChooser();

		currentDeck = new Deck();
		selectedCard = null;
		file = null;
		changes = false;

		if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
			try {
				for (LookAndFeelInfo info : UIManager
						.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} catch (Exception e) {
				System.err.println("Nimbus not available");
			}
		}

		deserializer();

		setSize(1000, 720);
		setResizable(false);
	}
	

	// ///////////////////////
	//
	// GUI Builders
	//
	// //////////////////////

	/**
	 * Building search box
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildSearchBox() {
		Box row1 = Box.createHorizontalBox();
		Box row2 = Box.createHorizontalBox();
		Box row3 = Box.createHorizontalBox();
		final NumberFormat numberInput = NumberFormat.getInstance();
		numberInput.setParseIntegerOnly(true);

		final JTextField idSearch = new JTextField();
		final JTextField raritySearch = new JTextField();
		final JTextField nameSearch = new JTextField();
		final JTextField abilitySearch = new JTextField();
		final JTextField minAttackSearch = new JTextField();
		final JTextField maxAttackSearch = new JTextField();
		final JTextField minDefenseSearch = new JTextField();
		final JTextField maxDefenseSearch = new JTextField();
		final JTextField minAttackCompSearch = new JTextField();
		final JTextField maxAttackCompSearch = new JTextField();
		final JTextField minDefenseCompSearch = new JTextField();
		final JTextField maxDefenseCompSearch = new JTextField();
		final JTextField expansionSearch = new JTextField();
		final JTextField seriesSearch = new JTextField();

		CardAssociation.Type[] classifications = null;
		classifications = CardAssociation.Type.values();
		final JComboBox typeList = new JComboBox(classifications);
		typeList.setSelectedItem(null);
		
		final String[] genderSelections = {"","Male","Female","None"};
		final JComboBox genderList = new JComboBox(genderSelections);
		genderList.setSelectedItem(genderSelections[0]);
		
		final String[] elementSelections = {"","Earth","Water","Fire","Wind","Light","Dark","Neutral"};
		final JComboBox elementList = new JComboBox(elementSelections);
		elementList.setSelectedItem(elementSelections[0]);
		
		final class DoSearch {
			public void run() {
				resultList.clear();
				String cardID = idSearch.getText();
				String name = nameSearch.getText();
				
				CardAssociation.Type sType = (CardAssociation.Type) typeList
				.getSelectedItem();
				
				int sMinAttack;
				try {
					sMinAttack = numberInput.parse(minAttackSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMinAttack = Integer.MIN_VALUE;
				}
				
				int sMaxAttack;
				try {
					sMaxAttack = numberInput.parse(maxAttackSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMaxAttack = Integer.MAX_VALUE;
				}
				
				int sMinDefense;
				try {
					sMinDefense = numberInput.parse(minDefenseSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMinDefense = Integer.MIN_VALUE;
				}
				
				int sMaxDefense;
				try {
					sMaxDefense = numberInput.parse(maxDefenseSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMaxDefense = Integer.MAX_VALUE;
				}
				
				int sMinAttackComp;
				try {
					sMinAttackComp = numberInput.parse(minAttackCompSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMinAttackComp = Integer.MIN_VALUE;
				}
				
				int sMaxAttackComp;
				try {
					sMaxAttackComp = numberInput.parse(maxAttackCompSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMaxAttackComp = Integer.MAX_VALUE;
				}
				
				int sMinDefenseComp;
				try {
					sMinDefenseComp = numberInput.parse(minDefenseCompSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMinDefenseComp = Integer.MIN_VALUE;
				}
				
				int sMaxDefenseComp;
				try {
					sMaxDefenseComp = numberInput.parse(maxDefenseCompSearch.getText()).intValue();
				} catch (ParseException e1) {
					sMaxDefenseComp = Integer.MAX_VALUE;
				}
				
				String sGender = (String) genderList.getSelectedItem();
				String sElement = (String) elementList.getSelectedItem();
				String sExpansion = expansionSearch.getText();
				String sSeries = seriesSearch.getText();
				String sAbility = abilitySearch.getText();
				
				for (Card c : completeList) {
					if (c.meetsRequirement(
							cardID,"",name,sType,sGender,sElement,
							sMinAttack,sMaxAttack,sMinDefense,sMaxDefense,sMinAttackComp,sMaxAttackComp,sMinDefenseComp,sMaxDefenseComp,
							sExpansion,sSeries,sAbility))
						resultList.add(c);
				}
				
				refresh("search");
			}
			
		};
		final DoSearch search = new DoSearch();
		
		final KeyListener searchFieldListener = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					search.run();

				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

		};

		/*
		 * final JFormattedTextField powerSearch = new JFormattedTextField(
		 * numberInput); final JFormattedTextField costSearch = new
		 * JFormattedTextField( numberInput); final JFormattedTextField
		 * levelSearch = new JFormattedTextField( numberInput); final
		 * JFormattedTextField soulSearch = new JFormattedTextField(
		 * numberInput);
		 */

		JButton submitButton = new JButton("Submit");
		// submitButton.setPreferredSize(new Dimension(100, 25));
		JButton clearButton = new JButton("Clear All");
		// submitButton.setPreferredSize(new Dimension(100, 25));

		// nameSearch.setMaximumSize(new Dimension(255, 20));

		JLabel idLabel = new JLabel("Card ID");
		JLabel nameLabel = new JLabel("Card Name");
		JLabel rarityLabel = new JLabel("Rarity");
		JLabel genderLabel = new JLabel("Gender");
		JLabel elementLabel = new JLabel("Element");
		JLabel typeLabel = new JLabel("Type");
		JLabel attackLabel = new JLabel("ATK");
		JLabel attackTildeLabel = new JLabel("~");
		JLabel defenseLabel = new JLabel("DEF");
		JLabel defenseTildeLabel = new JLabel("~");
		JLabel attackCompLabel = new JLabel("ATK Comp");
		JLabel attackCompTildeLabel = new JLabel("~");
		JLabel defenseCompLabel = new JLabel("DEF Comp");
		JLabel defenseCompTildeLabel = new JLabel("~");
		JLabel expansionLabel = new JLabel("Expansion");
		JLabel seriesLabel = new JLabel("Series");
		JLabel abilityLabel = new JLabel("Ability");

		idLabel.setLabelFor(idSearch);
		nameLabel.setLabelFor(nameSearch);
		rarityLabel.setLabelFor(raritySearch);
		attackLabel.setLabelFor(minAttackSearch);
		attackTildeLabel.setLabelFor(maxAttackSearch);
		defenseLabel.setLabelFor(minDefenseSearch);
		defenseTildeLabel.setLabelFor(maxDefenseSearch);
		attackCompLabel.setLabelFor(minAttackCompSearch);
		attackCompTildeLabel.setLabelFor(maxAttackCompSearch);
		defenseCompLabel.setLabelFor(minDefenseCompSearch);
		defenseCompTildeLabel.setLabelFor(maxDefenseCompSearch);
		expansionLabel.setLabelFor(expansionSearch);
		seriesLabel.setLabelFor(seriesSearch);
		abilityLabel.setLabelFor(abilitySearch);


		idSearch.addKeyListener(searchFieldListener);
		nameSearch.addKeyListener(searchFieldListener);
		raritySearch.addKeyListener(searchFieldListener);
		minAttackSearch.addKeyListener(searchFieldListener);
		maxAttackSearch.addKeyListener(searchFieldListener);
		minDefenseSearch.addKeyListener(searchFieldListener);
		maxDefenseSearch.addKeyListener(searchFieldListener);
		minAttackCompSearch.addKeyListener(searchFieldListener);
		maxAttackCompSearch.addKeyListener(searchFieldListener);
		minDefenseCompSearch.addKeyListener(searchFieldListener);
		maxDefenseCompSearch.addKeyListener(searchFieldListener);
		expansionSearch.addKeyListener(searchFieldListener);
		seriesSearch.addKeyListener(searchFieldListener);
		abilitySearch.addKeyListener(searchFieldListener);

		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search.run();
			}
		});

		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				idSearch.setText("");
				nameSearch.setText("");
				raritySearch.setText("");
				abilitySearch.setText("");
				minAttackSearch.setText("");
				maxAttackSearch.setText("");
				minDefenseSearch.setText("");
				maxDefenseSearch.setText("");
				minAttackCompSearch.setText("");
				maxAttackCompSearch.setText("");
				minDefenseCompSearch.setText("");
				maxDefenseCompSearch.setText("");
				expansionSearch.setText("");
				seriesSearch.setText("");
				genderList.setSelectedItem(genderSelections[0]);
				elementList.setSelectedItem(elementSelections[0]);
				
				typeList.setSelectedItem(null);
			}
		});

		row1.add(Box.createHorizontalStrut(5));
		row1.add(idLabel);
		row1.add(Box.createHorizontalStrut(5));
		row1.add(idSearch);
		row1.add(Box.createHorizontalStrut(5));
		/*
		row1.add(rarityLabel);
		row1.add(Box.createHorizontalStrut(5));
		row1.add(raritySearch);
		row1.add(Box.createHorizontalStrut(5));
		*/
		row1.add(nameLabel);
		row1.add(Box.createHorizontalStrut(5));
		row1.add(nameSearch);
		nameSearch.setPreferredSize(new Dimension(450,0));
		row1.add(Box.createHorizontalStrut(5));
		row1.add(typeLabel);
		row1.add(Box.createHorizontalStrut(5));
		row1.add(typeList);
		row1.add(Box.createHorizontalStrut(5));
		row1.add(submitButton);
		row1.add(Box.createHorizontalStrut(5));

		row2.add(Box.createHorizontalStrut(5));
		row2.add(genderLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(genderList);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(elementLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(elementList);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(attackLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(minAttackSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(attackTildeLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(maxAttackSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(defenseLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(minDefenseSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(defenseTildeLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(maxDefenseSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(attackCompLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(minAttackCompSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(attackCompTildeLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(maxAttackCompSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(defenseCompLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(minDefenseCompSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(defenseCompTildeLabel);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(maxDefenseCompSearch);
		row2.add(Box.createHorizontalStrut(5));
		row2.add(clearButton);
		row2.add(Box.createHorizontalStrut(5));

		row3.add(Box.createHorizontalStrut(5));
		row3.add(abilityLabel);
		row3.add(Box.createHorizontalStrut(5));
		row3.add(abilitySearch);
		row3.add(Box.createHorizontalStrut(5));

		searchBox.add(row1);
		searchBox.add(row2);
		searchBox.add(row3);
	}

	/**
	 * Building the menu bar
	 */
	/*
	 * private void buildMenu() {
	 * 
	 * JMenu firstTab = new JMenu("File");
	 * 
	 * newD = new JMenuItem("New"); save = new JMenuItem("Save"); load = new
	 * JMenuItem("Load"); exit = new JMenuItem("Exit");
	 * 
	 * newD.addActionListener(new ActionListener() {
	 * 
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Override public void actionPerformed(ActionEvent e) { if (changes) {
	 * saveOption(); } file = null; currentDeck = new Deck();
	 * 
	 * resultList = (ArrayList<Card>) completeList.clone();
	 * 
	 * refresh("new"); } });
	 * 
	 * save.addActionListener(new ActionListener() {
	 * 
	 * @Override public void actionPerformed(ActionEvent e) { changes = false;
	 * action(e); } });
	 * 
	 * load.addActionListener(new ActionListener() {
	 * 
	 * @Override public void actionPerformed(ActionEvent e) { currentDeck = new
	 * Deck(); action(e); changes = false; } });
	 * 
	 * exit.addActionListener(new ActionListener() {
	 * 
	 * @Override public void actionPerformed(ActionEvent e) { saveOption();
	 * System.exit(1); } });
	 * 
	 * firstTab.add(newD); firstTab.add(save); firstTab.add(load);
	 * firstTab.add(exit);
	 * 
	 * menu.add(firstTab);
	 * 
	 * }
	 */

	/**
	 * Building the selection pane
	 * 
	 * @return Box populated with the option buttons
	 */
	private Box buildOption() {

		Box box = Box.createHorizontalBox();

		newdb = new JButton("New");
		saveb = new JButton("Save");
		loadb = new JButton("Load");
		exitb = new JButton("Exit");

		newdb.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (changes) {
					saveOption();
				}
				file = null;
				currentDeck = new Deck();

				resultList = (ArrayList<Card>) completeList.clone();

				refresh("new");
			}
		});

		saveb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changes = false;
				action(e);
			}
		});

		loadb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// currentDeck = new Deck();
				action(e);
				changes = false;
			}
		});

		exitb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveOption();
				System.exit(0);
			}
		});

		box.add(newdb);
		box.add(saveb);
		box.add(loadb);
		box.add(exitb);

		return box;
	}

	/**
	 * Building the card information pane
	 * 
	 * @param c
	 *            Display the information of the selected Card c
	 */
	private Box buildCardInfo(Card c) {
		Box splitter = Box.createHorizontalBox();

		

		Box optionBox = buildOption();

		int widthM = getWidth() / 2 - OFFSET;
		int heightM = listBox.getPreferredSize().height
				- optionBox.getHeight();
		heightM = 250;

		if (getPreferredSize().width / 2 - OFFSET > widthM)
			widthM = getPreferredSize().width / 2 - OFFSET;
		else
			widthM = getWidth() / 2 - OFFSET;

		splitter.setPreferredSize(new Dimension(widthM, heightM));
		splitter.setMaximumSize(new Dimension(widthM, heightM));
		// splitter2.setPreferredSize(new Dimension(widthM, 300));
		// splitter2.setMaximumSize(new Dimension(widthM, 300));

		if (c == null) {
		} else {
			splitter.add(c.displayImage(
					(int) (splitter.getPreferredSize().width * 0.22),
					splitter.getPreferredSize().height));
			splitter.add(c.getInfoPane(
					(int) (splitter.getPreferredSize().width * 0.78),
					splitter.getPreferredSize().height));
			// splitter2.add(splitter);

		}

		Box splitter3 = Box.createVerticalBox();

		splitter3.add(splitter);
		splitter3.add(optionBox);
		
		splitter3.revalidate();

		// cardInfo.add(splitter3);

		// System.out.println("splitter has components: " +
		// splitter.getComponentCount());

		return splitter3;
	}

	/**
	 * Building search result list
	 * 
	 * @return JScrollPane populated with information of the cards in the result
	 *         list
	 */
	private JScrollPane buildResultList() {

		resultListModel = new ResultListTableModel(resultList);
		resultListTable = new JTable(resultListModel) {

			private static final long serialVersionUID = 3570425890676389430L;

			public boolean isCellEditable(int rowIndex, int colIndex) {
				return false;
			}
			
			public TableCellRenderer getCellRenderer(int row, int column) {
				switch (column) {
				case 0:
					return cardIDRenderer;
				case 5:
				case 6:
					return numberRenderer;
				case 7:
				case 8:
					return statCompRenderer;
				default:
					return super.getCellRenderer(row, column);
				}
			}
		};

		// Allocate column widths
		int widthM = getPreferredSize().width / 2 - OFFSET;
		if (getWidth() / 2 - OFFSET > widthM)
			widthM = getWidth() / 2 - OFFSET;
		else
			widthM = getPreferredSize().width / 2 - OFFSET;

		/*
		 * resultListTable.setPreferredScrollableViewportSize(new Dimension(
		 * widthM, 70));
		 */
		resultListTable.setFillsViewportHeight(true);
		// resultListTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultListTable.setRowSorter(new TableRowSorter<TableModel>(
				resultListTable.getModel()));

		// Handles right click selection
		resultListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// Left mouse click
				if (SwingUtilities.isLeftMouseButton(e)) {
					// Do something
				}
				// Right mouse click
				else if (SwingUtilities.isRightMouseButton(e)) {
					// Get the coordinates of the mouse click
					Point p = e.getPoint();

					// Get the row index that contains that coordinate
					int rowNumber = resultListTable.rowAtPoint(p);

					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = resultListTable
							.getSelectionModel();

					// Set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one
					// row.
					model.setSelectionInterval(rowNumber, rowNumber);

					int row = resultListTable.getSelectedRow();
					if (row > -1) {
						String key = (String) resultListTable.getValueAt(row, 0);
						selectedCard = cardHolder.get(key.split(" ")[0]);
						// selectedCard = allCards.get(row);
					}
					refresh("listBox");
				}

			}
		});

		// Handles click action
		resultListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = resultListTable.getSelectedRow();
				if (row > -1) {
					String key = (String) resultListTable.getValueAt(row, 0);
					selectedCard = cardHolder.get(key.split(" ")[0]);
					// selectedCard = allCards.get(row);
				}
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					refresh("listBox");
				} /*
				 * else if ((e.getClickCount() == 2 && e.getButton() ==
				 * MouseEvent.BUTTON1) || (e.getClickCount() == 1 &&
				 * e.getButton() == MouseEvent.BUTTON3) && row > -1) {
				 * 
				 * currentDeck.addCard(selectedCard); refresh("listBox"); }
				 */
			}

			public void mouseReleased(MouseEvent e) {
				int row = resultListTable.getSelectedRow();
				if (row > -1) {
					String key = (String) resultListTable.getValueAt(row, 0);
					selectedCard = cardHolder.get(key.split(" ")[0]);
					// selectedCard = allCards.get(row);
				}
				if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
						|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
						&& row > -1) {
					if (currentDeck.addCard(selectedCard, true))
						refresh("addToDeck");
				}
			}
		});

		// Handles keyboard inputs
		resultListTable.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int row = resultListTable.getSelectedRow();
				if (row > -1) {
					String key = (String) resultListTable.getValueAt(row, 0);
					selectedCard = cardHolder.get(key.split(" ")[0]);
				}
				if (e.getKeyCode() == KeyEvent.VK_DELETE
						|| e.getKeyCode() == KeyEvent.VK_MINUS) {
					if (currentDeck.removeCard(selectedCard))
						refresh("removeFromDeck");
				} else if (e.getKeyCode() == KeyEvent.VK_EQUALS
						|| e.getKeyCode() == KeyEvent.VK_ADD
						|| e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (currentDeck.addCard(selectedCard, true))
						refresh("addToDeck");
				} else if (e.getKeyCode() == KeyEvent.VK_UP
						|| e.getKeyCode() == KeyEvent.VK_DOWN) {
					refresh("listBox");
				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

		});

		TableColumn indCol = resultListTable.getColumnModel().getColumn(0);
		indCol.setPreferredWidth(110);
		TableColumn namCol = resultListTable.getColumnModel().getColumn(1);
		namCol.setPreferredWidth(widthM - 110 - 55 - 40 - 40 - 40 - 40 - 35 - 35);
		TableColumn typCol = resultListTable.getColumnModel().getColumn(2);
		typCol.setPreferredWidth(55);
		TableColumn genderCol = resultListTable.getColumnModel().getColumn(3);
		genderCol.setPreferredWidth(40);
		TableColumn elementCol = resultListTable.getColumnModel().getColumn(4);
		elementCol.setPreferredWidth(40);
		TableColumn attackCol = resultListTable.getColumnModel().getColumn(5);
		attackCol.setPreferredWidth(40);
		TableColumn defenseCol = resultListTable.getColumnModel().getColumn(6);
		defenseCol.setPreferredWidth(40);
		TableColumn attackCompCol = resultListTable.getColumnModel().getColumn(7);
		attackCompCol.setPreferredWidth(35);
		TableColumn defenseCompCol = resultListTable.getColumnModel().getColumn(8);
		defenseCompCol.setPreferredWidth(35);

		resultListTable.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				previousFocus = resultListTable;
			}

		});

		resultPane = new JScrollPane(resultListTable);
		resultPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		return resultPane;
	}

	private void refreshResultList() {
		resultListModel.setCardList(resultList);
		resultListTable.scrollRectToVisible(resultListTable.getCellRect(0, 0,
				true));

	}

	/**
	 * Building search result thumbnail
	 * 
	 * @param listPane
	 *            The ResultList JScrollPane
	 * @return JScrollPane populated with thumbnail of the cards in the result
	 *         list
	 */
	private JScrollPane buildResultThumbPane(JScrollPane listPane) {
		JPanel panel = new JPanel();
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Box.LEFT_ALIGNMENT);
		Box vbox = Box.createVerticalBox();
		vbox.setAlignmentX(Box.LEFT_ALIGNMENT);

		if (resultList.size() > MAXIMUMRESULTSHOWN) {

		} else {

			for (int i = 0; i < resultList.size(); i++) {
				if (i % RESULTPERLINE == 0 && i > 0) {
					vbox.add(box);
					box = Box.createHorizontalBox();
					box.setAlignmentX(Box.LEFT_ALIGNMENT);
				}
				final Card thisCard = resultList.get(i);
				JLabel tempLab = thisCard.initiateImage();
				MouseListener listener = new MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						// JComponent comp = (JComponent) e.getSource();
						// TransferHandler handler = comp.getTransferHandler();
						// handler.exportAsDrag(comp, e, TransferHandler.COPY);

						selectedCard = thisCard;
						System.out.println("selected "
								+ selectedCard.getName());
						refresh("listBox2");

						if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
								|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)) {
							selectedCard = thisCard;
							if (currentDeck.addCard(selectedCard, true))
								refresh("addToDeck");
						}
					}
				};

				tempLab.addMouseListener(listener);
				box.add(tempLab);

			}
			vbox.add(box);

			panel.add(vbox);
		}
		JScrollPane jsp = new JScrollPane(panel);
		jsp.setPreferredSize(new Dimension(listPane.getPreferredSize()));
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		return jsp;
	}

	/**
	 * Building the search result viewer area
	 * 
	 * @return JTabbedPane with different tabs for thumbnail and list views of
	 *         the result list
	 */
	private JTabbedPane buildResultArea() {

		resultPane = buildResultList();
		// resultThumbPane = buildResultThumbPane(resultPane);
		resultArea = new JTabbedPane();
		resultArea.add("List View", resultPane);
		resultArea.add("Thumbnail View", resultThumbPane);
		resultArea.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (resultArea.getSelectedIndex() == resultArea
						.indexOfComponent(resultThumbPane)) {
					// resultThumbPane = buildResultThumbPane(resultPane);
					refreshResultArea();
				}
			}
		});
		return resultArea;
	}

	private void refreshResultArea() {
		int resultThumbIndex = resultArea.indexOfComponent(resultThumbPane);
		resultHeader.setText("Result count: " + resultList.size());
		refreshResultList();
		resultThumbPane = buildResultThumbPane(resultPane);
		resultArea.setComponentAt(resultThumbIndex, resultThumbPane);
	}

	private Box buildAddRemoveButtonBox() {
		Box box = Box.createHorizontalBox();

		JButton plusOne = new JButton("+1");
		JButton plusFour = new JButton("+4");
		JButton minusOne = new JButton("-1");
		JButton minusFour = new JButton("-4");

		plusOne.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedCard != null
						&& currentDeck.addCard(selectedCard, true)) {
					refresh("addToDeck");
				}
				previousFocus.requestFocus();
			}
		});

		plusFour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedCard != null) {
					currentDeck.addCard(selectedCard, true);
					for (int i = 1; i < 4
							&& currentDeck.addCard(selectedCard, false); ++i)
						;
					refresh("addToDeck");
				}
				previousFocus.requestFocus();
			}
		});

		minusOne.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedCard != null
						&& currentDeck.removeCard(selectedCard)) {
					refresh("removeFromDeck");
				}
				previousFocus.requestFocus();
			}
		});

		minusFour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedCard != null) {
					for (int i = 0; i < 4
							&& currentDeck.removeCard(selectedCard); ++i)
						;
					refresh("removeFromDeck");
				}
				previousFocus.requestFocus();
			}
		});

		box.add(Box.createHorizontalGlue());
		box.add(plusOne);
		box.add(Box.createRigidArea(new Dimension(20, 0)));
		box.add(plusFour);
		box.add(Box.createRigidArea(new Dimension(20, 0)));
		box.add(minusOne);
		box.add(Box.createRigidArea(new Dimension(20, 0)));
		box.add(minusFour);
		box.add(Box.createHorizontalGlue());
		box.setPreferredSize(new Dimension(300, 50));
		return box;
	}

	/**
	 * Building the deck list pane
	 * 
	 * @return JScrollPane populated with information of the cards in the deck
	 */
	private JScrollPane buildDeckList() {

		deckListModel = new DeckListTableModel(currentDeck.getUnique());
		deckListTable = new JTable(deckListModel) {

			private static final long serialVersionUID = 3570425890676389430L;

			public boolean isCellEditable(int rowIndex, int colIndex) {
				return false;
			}
			public TableCellRenderer getCellRenderer(int row, int column) {
				switch(column) {
					case 1:
						return cardIDRenderer;
					case 6:
					case 7:
						return numberRenderer;
					case 8:
					case 9:
						return statCompRenderer;
					default:
						return super.getCellRenderer(row, column);
				}
			}
			
		};

		deckListTable
				.setPreferredScrollableViewportSize(new Dimension(750, 175));
		deckListTable.setFillsViewportHeight(true);
		deckListTable.setRowSorter(new LinkedTableRowSorter<TableModel>(deckListTable
				.getModel(),this));

		// Handles right click selection
		deckListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// Left mouse click
				if (SwingUtilities.isLeftMouseButton(e)) {
					// Do something
				}
				// Right mouse click
				else if (SwingUtilities.isRightMouseButton(e)) {
					// get the coordinates of the mouse click
					Point p = e.getPoint();

					// get the row index that contains that coordinate
					int rowNumber = deckListTable.rowAtPoint(p);

					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = deckListTable
							.getSelectionModel();

					// Set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one
					// row.
					model.setSelectionInterval(rowNumber, rowNumber);

					int row = deckListTable.getSelectedRow();
					if (row > -1) {
						selectedCard = cardHolder.get(deckListTable.getValueAt(
								row, 1).toString().split(" ")[0]);
						// selectedCard = allCards.get(row);
					}
					refresh("deckListSelect");
				}

			}
		});

		// Handles click action
		deckListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = deckListTable.getSelectedRow();
				if (row > -1)
					selectedCard = cardHolder.get(deckListTable.getValueAt(row,
							1).toString().split(" ")[0]);
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					refresh("deckListSelect");
				} /*
				 * else if ((e.getClickCount() == 2 && e.getButton() ==
				 * MouseEvent.BUTTON1) || (e.getClickCount() == 1 &&
				 * e.getButton() == MouseEvent.BUTTON3) && row > -1) {
				 * currentDeck.removeCard(selectedCard); refresh("deckList2"); }
				 */
			}

			public void mouseReleased(MouseEvent e) {
				int row = deckListTable.getSelectedRow();
				if (row > -1)
					selectedCard = cardHolder.get(deckListTable.getValueAt(row,
							1).toString().split(" ")[0]);
				if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
						|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
						&& row > -1) {
					if (currentDeck.removeCard(selectedCard))
						refresh("removeFromDeck");
				}
			}
		});

		deckListTable.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent f) {
			}

			@Override
			public void focusLost(FocusEvent f) {
			}
		});

		// Keyboard controls to operate the builder
		deckListTable.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {

			}

			/*
			 * Key Explanations DELETE remove a card - remove a card = add a
			 * card + add a card ENTER add a card
			 * 
			 * @see
			 * java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				int row = deckListTable.getSelectedRow();
				if (row > -1)
					selectedCard = cardHolder.get(deckListTable.getValueAt(row,
							1).toString().split(" ")[0]);
				if (e.getKeyCode() == KeyEvent.VK_UP
						|| e.getKeyCode() == KeyEvent.VK_DOWN)
					refresh("deckListSelect");
				if (e.getKeyCode() == KeyEvent.VK_DELETE
						|| e.getKeyCode() == KeyEvent.VK_MINUS) {
					if (currentDeck.removeCard(selectedCard))
						refresh("removeFromDeck");
				} else if (e.getKeyCode() == KeyEvent.VK_EQUALS
						|| e.getKeyCode() == KeyEvent.VK_ADD
						|| e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (currentDeck.addCard(selectedCard, true))
						refresh("addToDeck");
				}

			}

			@Override
			public void keyTyped(KeyEvent e) {

			}

		});

		// allocating size for columns
		int widthM = getPreferredSize().width;

		if (getWidth() / 2 - OFFSET > widthM)
			widthM = getWidth();
		else
			widthM = getPreferredSize().width;

		int cntW = 40;
		int indW = 150;
		int typW = 80;
		int genW = 45;
		int eleW = 45;
		int atkW = 45;
		int defW = 45;
		int atkCW = 35;
		int defCW = 35;

		int usedSpace = cntW + indW + typW + genW + eleW + atkW + defW + atkCW + defCW;

		TableColumn cntCol = deckListTable.getColumnModel().getColumn(0);
		cntCol.setPreferredWidth(cntW);
		TableColumn indCol = deckListTable.getColumnModel().getColumn(1);
		indCol.setPreferredWidth(indW);
		TableColumn namCol = deckListTable.getColumnModel().getColumn(2);
		namCol.setPreferredWidth(widthM - usedSpace);
		TableColumn typCol = deckListTable.getColumnModel().getColumn(3);
		typCol.setPreferredWidth(typW);
		TableColumn genCol = deckListTable.getColumnModel().getColumn(4);
		genCol.setPreferredWidth(genW);
		TableColumn eleCol = deckListTable.getColumnModel().getColumn(5);
		eleCol.setPreferredWidth(eleW);
		TableColumn atkCol = deckListTable.getColumnModel().getColumn(6);
		atkCol.setPreferredWidth(atkW);
		TableColumn defCol = deckListTable.getColumnModel().getColumn(7);
		defCol.setPreferredWidth(defW);
		TableColumn atkCCol = deckListTable.getColumnModel().getColumn(8);
		atkCCol.setPreferredWidth(atkCW);
		TableColumn defCCol = deckListTable.getColumnModel().getColumn(9);
		defCCol.setPreferredWidth(defCW);

		deckListTable.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				previousFocus = deckListTable;
			}

		});

		deckPane = new JScrollPane(deckListTable);
		deckPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		return deckPane;
	}
	
	private JScrollPane buildExtraDeckList() {

		extraDeckListModel = new DeckListTableModel(currentDeck.getUniqueExtra());
		extraDeckListTable = new JTable(extraDeckListModel) {

			private static final long serialVersionUID = 3570425890676389430L;

			public boolean isCellEditable(int rowIndex, int colIndex) {
				return false;
			}
			public TableCellRenderer getCellRenderer(int row, int column) {
				switch(column) {
					case 1:
						return cardIDRenderer;
					case 6:
					case 7:
						return numberRenderer;
					case 8:
					case 9:
						return statCompRenderer;
					default:
						return super.getCellRenderer(row, column);
				}
			}
			
		};

		extraDeckListTable
				.setPreferredScrollableViewportSize(new Dimension(750, 175));
		extraDeckListTable.setFillsViewportHeight(true);
		extraDeckListTable.setRowSorter(new TableRowSorter<TableModel>(extraDeckListTable
				.getModel()));

		// Handles right click selection
		extraDeckListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// Left mouse click
				if (SwingUtilities.isLeftMouseButton(e)) {
					// Do something
				}
				// Right mouse click
				else if (SwingUtilities.isRightMouseButton(e)) {
					// get the coordinates of the mouse click
					Point p = e.getPoint();

					// get the row index that contains that coordinate
					int rowNumber = extraDeckListTable.rowAtPoint(p);

					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = extraDeckListTable
							.getSelectionModel();

					// Set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one
					// row.
					model.setSelectionInterval(rowNumber, rowNumber);

					int row = extraDeckListTable.getSelectedRow();
					if (row > -1) {
						selectedCard = cardHolder.get(extraDeckListTable.getValueAt(
								row, 1));
						// selectedCard = allCards.get(row);
					}
					refresh("deckListSelect");
				}

			}
		});

		// Handles click action
		extraDeckListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = extraDeckListTable.getSelectedRow();
				if (row > -1)
					selectedCard = cardHolder.get(extraDeckListTable.getValueAt(row,
							1).toString().split(" ")[0]);
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					refresh("deckListSelect");
				} /*
				 * else if ((e.getClickCount() == 2 && e.getButton() ==
				 * MouseEvent.BUTTON1) || (e.getClickCount() == 1 &&
				 * e.getButton() == MouseEvent.BUTTON3) && row > -1) {
				 * currentDeck.removeCard(selectedCard); refresh("deckList2"); }
				 */
			}

			public void mouseReleased(MouseEvent e) {
				int row = extraDeckListTable.getSelectedRow();
				if (row > -1)
					selectedCard = cardHolder.get(extraDeckListTable.getValueAt(row,
							1).toString().split(" ")[0]);
				if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
						|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
						&& row > -1) {
					if (currentDeck.removeCard(selectedCard))
						refresh("removeFromDeck");
				}
			}
		});

		extraDeckListTable.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent f) {
			}

			@Override
			public void focusLost(FocusEvent f) {
			}
		});

		// Keyboard controls to operate the builder
		extraDeckListTable.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {

			}

			/*
			 * Key Explanations DELETE remove a card - remove a card = add a
			 * card + add a card ENTER add a card
			 * 
			 * @see
			 * java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				int row = extraDeckListTable.getSelectedRow();
				if (row > -1)
					selectedCard = cardHolder.get(extraDeckListTable.getValueAt(row,
							1).toString().split(" ")[0]);
				if (e.getKeyCode() == KeyEvent.VK_UP
						|| e.getKeyCode() == KeyEvent.VK_DOWN)
					refresh("deckListSelect");
				if (e.getKeyCode() == KeyEvent.VK_DELETE
						|| e.getKeyCode() == KeyEvent.VK_MINUS) {
					if (currentDeck.removeCard(selectedCard))
						refresh("removeFromDeck");
				} else if (e.getKeyCode() == KeyEvent.VK_EQUALS
						|| e.getKeyCode() == KeyEvent.VK_ADD
						|| e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (currentDeck.addCard(selectedCard, true))
						refresh("addToDeck");
				}

			}

			@Override
			public void keyTyped(KeyEvent e) {

			}

		});

		// allocating size for columns
		int widthM = getPreferredSize().width;

		if (getWidth() / 2 - OFFSET > widthM)
			widthM = getWidth();
		else
			widthM = getPreferredSize().width;

		int cntW = 40;
		int indW = 150;
		int typW = 80;
		int genW = 45;
		int eleW = 45;
		int atkW = 45;
		int defW = 45;
		int atkCW = 35;
		int defCW = 35;

		int usedSpace = cntW + indW + typW + genW + eleW + atkW + defW + atkCW + defCW;

		TableColumn cntCol = extraDeckListTable.getColumnModel().getColumn(0);
		cntCol.setPreferredWidth(cntW);
		TableColumn indCol = extraDeckListTable.getColumnModel().getColumn(1);
		indCol.setPreferredWidth(indW);
		TableColumn namCol = extraDeckListTable.getColumnModel().getColumn(2);
		namCol.setPreferredWidth(widthM - usedSpace);
		TableColumn typCol = extraDeckListTable.getColumnModel().getColumn(3);
		typCol.setPreferredWidth(typW);
		TableColumn genCol = extraDeckListTable.getColumnModel().getColumn(4);
		genCol.setPreferredWidth(genW);
		TableColumn eleCol = extraDeckListTable.getColumnModel().getColumn(5);
		eleCol.setPreferredWidth(eleW);
		TableColumn atkCol = extraDeckListTable.getColumnModel().getColumn(6);
		atkCol.setPreferredWidth(atkW);
		TableColumn defCol = extraDeckListTable.getColumnModel().getColumn(7);
		defCol.setPreferredWidth(defW);
		TableColumn atkCCol = extraDeckListTable.getColumnModel().getColumn(8);
		atkCCol.setPreferredWidth(atkCW);
		TableColumn defCCol = extraDeckListTable.getColumnModel().getColumn(9);
		defCCol.setPreferredWidth(defCW);

		extraDeckListTable.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				previousFocus = extraDeckListTable;
			}

		});

		extraDeckPane = new JScrollPane(extraDeckListTable);
		extraDeckPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		return extraDeckPane;
	}

	private void refreshDeckList() {
		deckListModel.setDeckList(currentDeck.getUnique());
		int i;
		for (i = 0; i < deckListTable.getRowCount(); ++i) {
			
			if (cardHolder.get(deckListTable.getValueAt(i, 1).toString().split(" ")[0]).equals(
					selectedCard))
				break;
		}
		if (i < deckListTable.getRowCount()) {
			if (deckArea.getSelectedComponent() != deckThumbPane) 
				deckArea.setSelectedComponent(deckPane);
			deckListTable.getSelectionModel().setSelectionInterval(i, i);
			deckListTable.scrollRectToVisible(deckListTable.getCellRect(i, 0,
					true));
		}
	}
	
	private void refreshExtraDeckList() {
		extraDeckListModel.setDeckList(currentDeck.getUniqueExtra());
		int i;
		for (i = 0; i < extraDeckListTable.getRowCount(); ++i) {
			
			if (cardHolder.get(extraDeckListTable.getValueAt(i, 1).toString().split(" ")[0]).equals(
					selectedCard))
				break;
		}
		if (i < extraDeckListTable.getRowCount()) {
			if (deckArea.getSelectedComponent() != deckThumbPane) 
				deckArea.setSelectedComponent(extraDeckPane);
			extraDeckListTable.getSelectionModel().setSelectionInterval(i, i);
			extraDeckListTable.scrollRectToVisible(extraDeckListTable.getCellRect(i, 0,
					true));
		}
	}

	private void refreshStats() {

		cardCountText.setText(String.valueOf(currentDeck.getCards().size()));
		charaCountText.setText(String.valueOf(currentDeck.getNumChara()));
		extraCountText.setText(String.valueOf(currentDeck.getNumExtra()));
		eventCountText.setText(String.valueOf(currentDeck.getNumEvent()));
		setCountText.setText(String.valueOf(currentDeck.getNumSet()));

	}

	/**
	 * Building deck statistic analyzer
	 * 
	 * @return Box with the statistical information of the deck displayed
	 */
	private Box buildStatsZone() {
		Box leftPanel = Box.createVerticalBox();
		Box analyzerBox = Box.createHorizontalBox();

		// 16 Lv0
		// 12-14 Lv1
		// 6-8 Lv2
		// 4-6 Lv3
		// 8 CX

		Box newVert = Box.createVerticalBox();
		newVert.add(new JLabel("Deck Size: "));
		newVert.add(new JLabel("Extra Deck Size: "));
		newVert.add(new JLabel(" "));
		newVert.add(new JLabel("Chara: "));
		newVert.add(new JLabel("Event: "));
		newVert.add(new JLabel("Set: "));
		Box newVert2 = Box.createVerticalBox();

		cardCountText = new JLabel(
				String.valueOf(currentDeck.getCards().size()-currentDeck.getNumExtra()));
		extraCountText = new JLabel(String.valueOf(currentDeck.getNumExtra()));
		charaCountText = new JLabel(String.valueOf(currentDeck.getNumChara()));
		eventCountText = new JLabel(String.valueOf(currentDeck.getNumEvent()));
		setCountText = new JLabel(String.valueOf(currentDeck.getNumSet()));


		newVert2.add(cardCountText);
		newVert2.add(extraCountText);
		newVert2.add(new JLabel(" "));
		newVert2.add(charaCountText);
		newVert2.add(eventCountText);
		newVert2.add(setCountText);
		newVert2.setPreferredSize(new Dimension(20, 100));


		analyzerBox.add(newVert);
		// analyzerBox.add(Box.createHorizontalGlue());
		analyzerBox.add(Box.createRigidArea(new Dimension(5, 0)));
		analyzerBox.add(newVert2);
		analyzerBox.setPreferredSize(new Dimension(80, 100));

		JButton clearDeck = new JButton("Clear Deck");
		clearDeck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (changes) {
					saveOption();
				}
				file = null;
				currentDeck = new Deck();

				refresh("deckList2");
			}
		});

		leftPanel.add(analyzerBox);
		Box boxtemp = Box.createHorizontalBox();
		boxtemp.add(Box.createHorizontalGlue());
		boxtemp.add(clearDeck);
		leftPanel.add(boxtemp);
		leftPanel.setAlignmentX(CENTER_ALIGNMENT);
		leftPanel.setPreferredSize(new Dimension(60, 100));
		return leftPanel;
	}

	/**
	 * Building deck thumbnail
	 * 
	 * @param deckListPane
	 *            The ResultList JScrollPane
	 * @return JScrollPane populated with thumbnail of the cards in the deck
	 */
	private JScrollPane buildDeckThumbPane(JScrollPane deckListPane) {

		JPanel panel = new JPanel();
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Box.LEFT_ALIGNMENT);
		Box vbox = Box.createVerticalBox();
		vbox.setAlignmentX(Box.LEFT_ALIGNMENT);

		int cards = 0;
		for (int i = 0; i < deckListModel.getRowCount(); i++) {
			int qty = (Integer) deckListTable.getValueAt(i, 0);
			final Card thisCard = cardHolder.get(deckListTable.getValueAt(i, 1).toString().split(" ")[0]);
			for (int j = 0; j < qty; ++j) {
				JLabel tempLab = thisCard.initiateImage();
				MouseListener listener = new MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						selectedCard = thisCard;
						System.out.println(thisCard.getName() + " has "
								+ thisCard.getCardCount() + " copies");
						refresh("deckListSelect");
	
						if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
								|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)) {
							currentDeck.removeCard(selectedCard);
							refresh("removeFromDeck");
						}
					}
				};
	
				tempLab.addMouseListener(listener);
				box.add(tempLab);
				if (++cards % DECKPERLINE == 0 && cards > 0) {
					vbox.add(box);
					box = Box.createHorizontalBox();
					box.setAlignmentX(Box.LEFT_ALIGNMENT);
				}
			}

		}
		vbox.add(box);
		Component spacer = Box.createVerticalStrut(10);
		vbox.add(spacer);
		box = Box.createHorizontalBox();
		box.setAlignmentX(Box.LEFT_ALIGNMENT);
		for (int i = 0; i < extraDeckListModel.getRowCount(); i++) {
			int qty = (Integer) extraDeckListTable.getValueAt(i, 0);
			final Card thisCard = cardHolder.get(extraDeckListTable.getValueAt(i, 1).toString().split(" ")[0]);
			for (int j = 0; j < qty; ++j) {
				JLabel tempLab = thisCard.initiateImage();
				MouseListener listener = new MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						selectedCard = thisCard;
						System.out.println(thisCard.getName() + " has "
								+ thisCard.getCardCount() + " copies");
						refresh("deckListSelect");
	
						if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
								|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)) {
							currentDeck.removeCard(selectedCard);
							refresh("removeFromDeck");
						}
					}
				};
	
				tempLab.addMouseListener(listener);
				box.add(tempLab);
			}

		}
		vbox.add(box);
		panel.add(vbox);

		JScrollPane jsp = new JScrollPane(panel);
		panel.setAlignmentY(LEFT_ALIGNMENT);
		jsp.setAlignmentY(LEFT_ALIGNMENT);
		jsp.setPreferredSize(new Dimension(deckListPane.getPreferredSize()));
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		return jsp;
	}

	/**
	 * Building deck viewer area
	 * 
	 * @return JTabbedPane with different tabs for thumbnail and list views of
	 *         the deck
	 */
	private JTabbedPane buildDeckArea() {
		deckArea = new JTabbedPane();

		deckPane = buildDeckList();
		extraDeckPane = buildExtraDeckList();
		deckThumbPane = buildDeckThumbPane(deckPane);
		deckArea = new JTabbedPane();
		deckArea.addTab("Main Deck", deckPane);
		deckArea.addTab("Extra Deck", extraDeckPane);
		deckArea.addTab("Thumbnail View", deckThumbPane);

		return deckArea;
	}

	private void refreshDeckArea() {
		int resultThumbIndex = deckArea.indexOfComponent(deckThumbPane);
		refreshDeckList();
		refreshExtraDeckList();
		deckThumbPane = buildDeckThumbPane(deckPane);
		deckArea.setComponentAt(resultThumbIndex, deckThumbPane);
	}

	// ///////////////////////
	//
	// Public Methods
	//
	// //////////////////////

	/**
	 * Initialize the client
	 */
	public void init() {
		numberRenderer = new NumberCellRenderer();
		statCompRenderer = new StatCompCellRenderer();
		cardIDRenderer = new CardIDCellRenderer();
		buildUI();
		// pack();
	}

	/**
	 * Action events that are necessary
	 * 
	 * @param e
	 *            ActionEvent e
	 */
	public void action(ActionEvent e) {
		fc.setCurrentDirectory(new File("Deck"));

		if (e.getSource() == loadb) {
			// saveOption();
			int returnVal = fc.showOpenDialog(this.getParent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				currentDeck = new Deck();
				currentDeck.loadRaw(file, cardHolder);
				refresh("load");
			}
		} else if (e.getSource() == saveb) {
			File directoryMaker = new File("Deck");
			if (!directoryMaker.exists())
				directoryMaker.mkdir();
			fc.setCurrentDirectory(directoryMaker);
			int returnVal = fc.showSaveDialog(this.getParent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// if (file == null)
				file = fc.getSelectedFile();
				if (file.exists()) {
					// System.err.println(file.getCardName());
				} else {

					file = new File(directoryMaker, file.getName());
					System.out.println(file.getName() + " was created");
				}
				currentDeck.saveRaw(file);
			}
		}
	}

	/**
	 * Refresh view
	 * 
	 * @param source
	 *            The string representation of the source called refresh
	 */
	public void refresh(String source) {
		// getContentPane().removeAll();
		// validate();

		if (source.equalsIgnoreCase("load")
				|| source.equalsIgnoreCase("listBox")
				|| source.equalsIgnoreCase("deckListSelect")
				|| source.equalsIgnoreCase("search")
				|| source.equalsIgnoreCase("new")
				|| source.equalsIgnoreCase("listBox2")) {
			cardInfo.removeAll();
			cardInfo.revalidate();
			cardInfo.add(buildCardInfo(selectedCard));
		}

		if (source.equalsIgnoreCase("search") || source.equalsIgnoreCase("new")) {
			refreshResultArea();
		}

		if (source.equalsIgnoreCase("load")) {
			changes = true;
		}

		if (source.equalsIgnoreCase("addToDeck")
				|| source.equalsIgnoreCase("removeFromDeck")
				|| source.equalsIgnoreCase("deckList2")
				|| source.equalsIgnoreCase("load")
				|| source.equalsIgnoreCase("new")) {
			refreshStats();
			refreshDeckArea();
			// changes = true;
		}
		if (source.equalsIgnoreCase("deckThumbs")) {
			int resultThumbIndex = deckArea.indexOfComponent(deckThumbPane);
			deckThumbPane = buildDeckThumbPane(deckPane);
			deckArea.setComponentAt(resultThumbIndex, deckThumbPane);
		}

		// BorderLayout layout = new BorderLayout();
		// getContentPane().setLayout(layout);
		//
		// add(BorderLayout.NORTH, searchBox);
		// add(BorderLayout.WEST, cardInfo);
		// add(BorderLayout.EAST, listBox);
		// add(BorderLayout.SOUTH, deckList);
		// setJMenuBar(menu);

		// pack();
		// System.out.println(getWidth() + " * " + getHeight());

		resizeSearchBox(searchBox);
		resizeSearchBox(cardInfo);
		// getContentPane().setVisible(true);
	}

	/**
	 * Prompt for save
	 */
	private void saveOption() {
		if (changes) {
			if (file == null) {
				int returnVal = fc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					file = fc.getSelectedFile();
				}
			}

			if (file != null) {
				currentDeck.saveRaw(file);
				currentDeck.save(file);
			}
		}
	}

	/**
	 * Resize the search box components
	 */
	private void resizeSearchBox(JComponent comp) {
		for (Component jc : comp.getComponents()) {
			((JComponent) jc).putClientProperty("JComponent.sizeVariant",
					"mini");
		}
	}

	/**
	 * Build the internal elements of the UI
	 */
	public void buildUI() {
		buildSearchBox();
		resizeSearchBox(searchBox);
		// buildMenu();

		resultHeader = new JLabel("Result count: " + resultList.size());
		Box headerBox = Box.createHorizontalBox();
		headerBox.add(Box.createRigidArea(new Dimension(10, 0)));
		headerBox.add(resultHeader);
		headerBox.add(Box.createHorizontalGlue());

		listBox.add(headerBox);
		listBox.add(buildResultArea());
		listBox.add(buildAddRemoveButtonBox());

		cardInfo.add(buildCardInfo(selectedCard));
		resizeSearchBox(cardInfo);

		deckList.add(buildStatsZone());
		deckList.add(buildDeckArea());

		setJMenuBar(menu);

		BorderLayout layout = new BorderLayout();
		getContentPane().setLayout(layout);

		add(BorderLayout.NORTH, searchBox);
		add(BorderLayout.WEST, cardInfo);
		add(BorderLayout.EAST, listBox);
		add(BorderLayout.SOUTH, deckList);
	}

	/**
	 * used by Player.java to load a selected deck
	 * 
	 * @param selectedDeck
	 *            Name of the selected deck
	 */
	public void loadDefaultDeck(String selectedDeck) {
		currentDeck.load(new File("Deck/" + selectedDeck), cardHolder);
		refresh("load");
	}

	/**
	 * De-serialize the data given
	 */
	@SuppressWarnings("unchecked")
	private void deserializer() {

		// FileInputStream fileInput;
		InputStream fileInput;
		ObjectInputStream objectInput;

		try {
			// fileInput = new FileInputStream();
			System.out.println("Opening data");
			fileInput = getClass().getResourceAsStream("/resources/CardDatav2");
			objectInput = new ObjectInputStream(fileInput);

			completeList = (ArrayList<Card>) objectInput.readObject();
			resultList = (ArrayList<Card>) completeList.clone();
			cardHolder = (HashMap<String, Card>) objectInput.readObject();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	// main
	public static void main(String[] args) {
		BuilderGUI builderGui = new BuilderGUI();
		builderGui.init();

		builderGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		builderGui.setLocationRelativeTo(null);
		builderGui.setVisible(true);
	}

}
