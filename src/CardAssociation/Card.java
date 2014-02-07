/**
 * @file Cardv2.java
 * @author Jia Chen
 * @date May 04, 2012
 * @description 
 * 		Cardv2.java is the test instance of Card.java.
 */

package CardAssociation;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.UUID;
import java.awt.Component;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

public class Card implements Serializable, MouseListener, MouseMotionListener, Comparable<Object>, Transferable {

	/* * * * * * * * * * * *
	 * serialized versions *
	 */
	// private static final long serialVersionUID = 5876059325645604130L;
	// private static final long serialVersionUID = 5876059325645604131L;
	private static final long serialVersionUID = 5876059325645604132L;
	
	private int dupCount = 0;

	// card properties
	private String[] sameID;
	private String id;
	private String rarity;
	private String pID;
	private String name;
	private String name_kana;
	private String name_e;
	private Type t;
	private int attack;
	private int defense;
	private int attackComp;
	private int defenseComp;
	private String gender;
	private String gender_e;
	private String element;
	private String element_e;
	private ArrayList<String> rule;
	private ArrayList<String> rule_e;
	private String flavorText;
	private String flavorText_e;
	private String expansion;
	private String expansion_e;
	private String series;
	private String series_e;
	
	
	private String realCardName;
	private ArrayList<Attribute> attributes;
	private ArrayList<Card> associatedCards;
	private boolean isAlternateArt;

	/*
	// game play properties
	private State currentState;
	private Zone currentZone;
	*/

	// other properties
	private String imageResource;
	private String backResource;
	private DataFlavor[] flavors;
	private int MINILEN = 3;

	private UUID uniqueID;

	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (isDataFlavorSupported(flavor)) {
			return this;
		}
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavors[0].equals(flavor);
	}

	public boolean equals(Object o) {
		Card card = (Card) o;
		if (card != null)
			return card.getID().equals(id);
		else
			return false;
	}

	@Override
	public int compareTo(Object arg0) {
		return id.compareTo(((Card) arg0).id);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// customCanvas.repaint();
		// customCanvas.setLocation(e.getX(), e.getY());
		// System.out.println(cardName + " dragged " + customCanvas.getX() +
		// ", "
		// + customCanvas.getY());
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		System.out.println("Cardv2.java:clicked " + name);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		System.out.println("Cardv2.java:pressed cardName = " + name);
		System.out.println("Cardv2.java:pressed name = " + getName());
		JComponent comp = (JComponent) arg0.getSource();
		TransferHandler handler = comp.getTransferHandler();
		handler.exportAsDrag(comp, arg0, TransferHandler.COPY);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	/* * * * * * * * * * * * * * * * * * * *
	 * Standard Card Information and Image *
	 */

	public Card(String id, String name) {
		sameID = new String[MINILEN];
		for (int i = 0; i < sameID.length; i++) {
			sameID[i] = "";
		}
		setID(id);
		// setName(id);
		setName(name);

		realCardName = name;

		rule = new ArrayList<String>();
		rule_e = new ArrayList<String>();
		flavorText = "";
		flavorText_e = "";
		// imageFile = new File("FieldImages/cardBack-s.jpg");
		imageResource = "/resources/FieldImages/cardBack-s.jpg";
		backResource = "/resources/FieldImages/cardBack-s.jpg";
		setAssociatedCards(new ArrayList<Card>());
		setAttributes(new ArrayList<Attribute>());
		// addMouseListener(this);
	}

	// create a card
	public Card() {
		rule = new ArrayList<String>();
		rule_e = new ArrayList<String>();
		flavorText = "";
		flavorText_e = "";
		sameID = new String[MINILEN];
		for (int i = 0; i < sameID.length; i++) {
			sameID[i] = "";
		}
	}

	public boolean setID(String id) {
		String newId = id.replace(" ", "");
		pID = newId.charAt(0) + "";

		for (int i = 1; i < newId.length(); i++) {
			if ((Character.isLetter(newId.charAt(i)) && Character.isDigit(newId.charAt(i - 1))) || (Character.isSpaceChar(newId.charAt(i)) && Character.isDigit(newId.charAt(i - 1)))) {
				break;
			} else {
				pID += newId.charAt(i);
			}
		}

		boolean isDupCard = false;

		for (int i = 0; i < sameID.length; i++) {
			if (sameID[i] == null || sameID[i].isEmpty() || sameID[i].equals(pID)) {
				if (sameID[i].equals(pID)) {
					isDupCard = false;
				} else {
					isDupCard = true;
				}
				sameID[i] = pID;
				break;
			}
		}

		// this.id = sameID[0];
		this.id = pID;
		this.id = id;

		return isDupCard;
	}

	public JLabel initiateImage() {
		JLabel imageLabel = new JLabel();
		try {
			Image image = ImageIO.read(getClass().getResourceAsStream(getImageResource()));
			// Image image = ImageIO.read(new
			// File("src/FieldImages/cardBack-s.jpg").toURI().toURL());
			// Image image = ImageIO.read((imageFile.toURI()).toURL());
			// ImageIcon img = new ImageIcon(image);

			ImageIcon img = new ImageIcon(image.getScaledInstance((int) (image.getWidth(null) *0.5 * 0.44), (int) (image.getHeight(null) *0.5 * 0.44), Image.SCALE_SMOOTH));

			imageLabel.setIcon(img);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageLabel;
	}

	// set the card image
	public void setImageResource(String resPath) {
		imageResource = resPath;
		if (resPath.contains("_holo") || resPath.contains("_alt")) {
			setAlternateArt(true);
		}
		// setName(id);
	}

	// get the card image
	public String getImageResource() {
		// if (isWindows)
		// return "/" + new File(imageResource).getPath();
		// else
		return imageResource;
	}

	public JPanel getInfoPane(int w, int h) {

		// Font font = new Font("Courier New", Font.BOLD, 12);

		JPanel infoPanel = new JPanel();
		infoPanel.setPreferredSize(new Dimension(w, h));

		GroupLayout layout = new GroupLayout(infoPanel);
		infoPanel.setLayout(layout);

		JTextArea description = new JTextArea(10, 10);
		// description.setFont(font);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setEditable(false);

		String cardText = "";
		cardText += getEffects() + "\n";

		if (!getFlavorText().equals("")) {
			cardText += "Flavor Text: \n" + getFlavorText();
		}

		description.setText(cardText);

		description.setCaretPosition(0);
		JScrollPane descContainer = new JScrollPane(description);

		JTextField nameLabel = new JTextField(name);
		nameLabel.setEditable(false);
		// nameLabel.setFont(font);
		JTextField idLabel = new JTextField(id + " " + rarity);
		idLabel.setEditable(false);
		// idLabel.setFont(font);
		JTextField typeLabel = new JTextField(t.toString());
		typeLabel.setEditable(false);
		// typeLabel.setFont(font);
		JTextField genderTextLabel = new JTextField("Gender:  " + gender);
		genderTextLabel.setEditable(false);
		JTextField elementTextLabel = new JTextField("Element:  " + element);
		elementTextLabel.setEditable(false);
		String attackString = "" + (attack >= 0 ? String.valueOf(attack) : "-");
		String defenseString = "" + (defense >= 0 ? String.valueOf(defense) : "-");
		String attackCompString;
		if (attackComp != 0) {
			attackCompString = attackComp < 0 ? String.valueOf(attackComp) : "+" + String.valueOf(attackComp);
		} else {
			attackCompString = "-";
		}
		String defenseCompString;
		if (defenseComp != 0) {
			defenseCompString = defenseComp < 0 ? String.valueOf(defenseComp) : "+" + String.valueOf(defenseComp);
		} else {
			defenseCompString = "-";
		}
		JLabel attackTextLabel = new JLabel("ATK:");
		JLabel defenseTextLabel = new JLabel("DEF:");
		JLabel attackLabel = new JLabel(attackString,JLabel.RIGHT);
		JLabel defenseLabel = new JLabel(defenseString,JLabel.RIGHT);
		JLabel attackCompLabel = new JLabel(attackCompString,JLabel.RIGHT);
		JLabel defenseCompLabel = new JLabel(defenseCompString,JLabel.RIGHT);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		Component spacer = Box.createHorizontalStrut(0);
		
		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup();
		hGroup
			.addComponent(nameLabel)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(idLabel,GroupLayout.PREFERRED_SIZE,118,GroupLayout.PREFERRED_SIZE)
						.addComponent(genderTextLabel,GroupLayout.PREFERRED_SIZE,118,GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(typeLabel,GroupLayout.PREFERRED_SIZE,105,GroupLayout.PREFERRED_SIZE)
						.addComponent(elementTextLabel,GroupLayout.PREFERRED_SIZE,105,GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(attackTextLabel,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(attackLabel,GroupLayout.PREFERRED_SIZE,18,GroupLayout.PREFERRED_SIZE)
						.addComponent(attackCompLabel,GroupLayout.PREFERRED_SIZE,18,GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(spacer))
				.addGroup(layout.createParallelGroup()
						.addComponent(defenseTextLabel,GroupLayout.PREFERRED_SIZE,28,GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(defenseLabel,GroupLayout.PREFERRED_SIZE,18,GroupLayout.PREFERRED_SIZE)
						.addComponent(defenseCompLabel,GroupLayout.PREFERRED_SIZE,18,GroupLayout.PREFERRED_SIZE))
						)
			.addComponent(descContainer);
//			);
				
		layout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup
		.addGroup(layout.createParallelGroup().addComponent(nameLabel))
		.addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(typeLabel).addComponent(attackTextLabel).addComponent(attackLabel)
				.addComponent(spacer).addComponent(defenseTextLabel).addComponent(defenseLabel))
		.addGroup(layout.createParallelGroup().addComponent(genderTextLabel)
				.addComponent(elementTextLabel).addComponent(attackCompLabel).addComponent(defenseCompLabel))
		.addGroup(layout.createParallelGroup().addComponent(descContainer));
		

		/*
		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 350, GroupLayout.PREFERRED_SIZE)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup().addGroup(layout.createSequentialGroup()
												.addComponent(idLabel, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
												.addGroup(layout.createSequentialGroup()
														.addComponent(genderLabel)
														.addComponent(attackLabel)
														.addComponent(attackCompLabel)))
								.addGroup(
										layout.createParallelGroup().addGroup(layout.createSequentialGroup()
												.addComponent(typeLabel))
												.addGroup(layout.createSequentialGroup()
														.addComponent(elementLabel)
														.addComponent(defenseLabel)
														.addComponent(defenseCompLabel))))
				.addComponent(descContainer, GroupLayout.PREFERRED_SIZE, 350, GroupLayout.PREFERRED_SIZE));
		*/

		layout.setVerticalGroup(vGroup);

		System.out.println("getInfoPane");

		return infoPanel;

	}

	public JPanel displayImage(int w, int h) {

		JPanel imagePane = new JPanel();
		imagePane.setPreferredSize(new Dimension(w, h));

		try {
			// Image image = ImageIO.read((imageFile.toURI()).toURL());
			System.out.println(getImageResource());
			Image image = ImageIO.read(getClass().getResourceAsStream(getImageResource()));
			ImageIcon img = new ImageIcon(image.getScaledInstance((int)(image.getWidth(null)*0.5), (int)(image.getHeight(null)*0.5),Image.SCALE_SMOOTH));

			imagePane.add(new JLabel(img));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return imagePane;
	}

	// used in Deck.java to check how many copies of the card is there
	public int getCardCount() {
		return dupCount;
	}

	public String getID() {
		return id;
	}

	// used in Deck.java to increment the number of copies of the card
	public void addCount() {
		dupCount++;
	}

	public void setCount(int dupCount) {
		this.dupCount = dupCount;
	}

	// set the card name of the card
	public void setName(String name) {
		this.name = name;
		setUniqueID(UUID.randomUUID());
	}

	// get the card name of the card
	public String getName() {
		return name;
	}

	public void setName_e(String name_e) {
		this.name_e = name_e;
	}

	public String getName_e() {
		return name_e;
	}

	// get the card effects
	public String getEffects() {
		String result = getEffects_j();
		String effectsStr_e = getEffects_e();
		if (!effectsStr_e.isEmpty()) {
			result += "\n" + effectsStr_e;
		}
		return result;
	}

	public String getEffects_j() {
		String result = "";

		for (int i = 0; i < rule.size(); i++) {
			result += rule.get(i) + "\n";
		}

		return result;
	}

	public String getEffects_e() {
		String result = "";

		for (int i = 0; i < rule_e.size(); i++) {
			result += rule_e.get(i) + "\n";
		}

		return result;
	}

	// set the card effects
	public void addEffect(String e) {
		if (!e.isEmpty())
			rule.add(e);
		// TODO: process effects to make attributes
	}

	public void addEffect_e(String e) {
		if (!e.isEmpty())
			rule_e.add(e);
		// TODO: process effects to make attributes
	}

	// get the card type
	public void setT(Type t) {
		this.t = t;
	}

	// set the card type
	public Type getT() {
		return t;
	}

	public String getRarity() {
		return rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	public String getName_kana() {
		return name_kana;
	}

	public void setName_kana(String name_kana) {
		this.name_kana = name_kana;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	public int getAttackComp() {
		return attackComp;
	}

	public void setAttackComp(int attackComp) {
		this.attackComp = attackComp;
	}

	public int getDefenseComp() {
		return defenseComp;
	}

	public void setDefenseComp(int defenseComp) {
		this.defenseComp = defenseComp;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGender_e() {
		return gender_e;
	}

	public void setGender_e(String gender_e) {
		this.gender_e = gender_e;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getElement_e() {
		return element_e;
	}

	public void setElement_e(String element_e) {
		this.element_e = element_e;
	}

	public String getExpansion() {
		return expansion;
	}

	public void setExpansion(String expansion) {
		this.expansion = expansion;
	}

	public String getExpansion_e() {
		return expansion_e;
	}

	public void setExpansion_e(String expansion_e) {
		this.expansion_e = expansion_e;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getSeries_e() {
		return series_e;
	}

	public void setSeries_e(String series_e) {
		this.series_e = series_e;
	}

	public void resetCount() {
		dupCount = 0;
	}

	// used in Deck.java to decrement the number of copies of the card
	public void removeCount() {
		dupCount--;
	}

	public boolean meetsRequirement(
			String sId, 
			String sRarity, 
			String sName, 
			Type sType, 
			String sGender, 
			String sElement, 
			int sMinAttack,
			int sMaxAttack,
			int sMinDefense,
			int sMaxDefense,
			int sMinAttackComp,
			int sMaxAttackComp,
			int sMinDefenseComp,
			int sMaxDefenseComp,
			String sExpansion,
			String sSeries,
			String sAbility) {

		boolean isMet = true;

		if (!id.isEmpty()) {

			String[] parts = sId.split(" ");

			for (int i = 0; i < sameID.length; i++) {
				isMet = true;
				for (int j = 0; j < parts.length; j++) {
					isMet = isMet && sameID[i].toLowerCase().contains(parts[j].toLowerCase());
					/*
					 * if (sameID[i].toLowerCase()
					 * .contains(parts[j].toLowerCase()))
					 * System.out.println(sameID[i] + "???" + parts[j]);
					 */
				}
				if (isMet) {
					break;
				}

			}
			isMet = true;
			for (int j = 0; j < parts.length; j++) {
				isMet = isMet && id.toLowerCase().contains(parts[j].toLowerCase());
				/*
				 * if (id.toLowerCase().contains(parts[j].toLowerCase()))
				 * System.out.println(id + "::CONTAINS::" + parts[j]);
				 */
			}

			/*
			 * if (isMet) { for (int i = 0; i < sameID.length; i++) {
			 * System.out.print("[(" + i + ")" + sameID[i] + "]"); }
			 * System.out.println(); }
			 */
		}

		if (!sName.isEmpty()) {
			isMet = isMet && (name.toLowerCase().contains(sName.toLowerCase()) || name_e.toLowerCase().contains(sName.toLowerCase()));
		}
		
		if (!sRarity.isEmpty()) {
			isMet = isMet && (rarity.toLowerCase().equals(sRarity.toLowerCase()));
		}

		if (sType != null && sType != CardAssociation.Type.ALL) {
			isMet = isMet && (sType == t);
		}
		
		if (!sGender.isEmpty()) {
			isMet = isMet && (gender.toLowerCase().contains(sGender.toLowerCase()) || gender_e.contains(sGender));
		}
		
		if (!sElement.isEmpty()) {
			isMet = isMet && (element.toLowerCase().contains(sElement.toLowerCase()) || element_e.contains(sElement));
		}
		
		isMet = isMet && (sMinAttack <= attack) && (attack <= sMaxAttack);
		isMet = isMet && (sMinDefense <= defense) && (defense <= sMaxDefense);
		isMet = isMet && (sMinAttackComp <= attackComp) && (attackComp <= sMaxAttackComp);
		isMet = isMet && (sMinDefenseComp <= defenseComp) && (defenseComp <= sMaxDefenseComp);

		if (!sExpansion.isEmpty()) {
			isMet = isMet && (expansion.toLowerCase().contains(sExpansion) || expansion_e.toLowerCase().contains(sExpansion));
		}
		
		if (!sSeries.isEmpty()) {
			isMet = isMet && (series.toLowerCase().contains(sSeries) || series_e.toLowerCase().contains(sSeries));
		}

		if (!sAbility.isEmpty()) {

			String[] parts = sAbility.split(" ");

			for (int i = 0; i < parts.length; i++) {
				isMet = isMet && (getEffects().toLowerCase().contains(parts[i].toLowerCase()) || getEffects_e().toLowerCase().contains(parts[i].toLowerCase()));
			}
		}

		return isMet;
	}

	public void setFlavorText(String flavorText) {
		this.flavorText = flavorText;
	}

	public String getFlavorText() {
		return getFlavorText_j() + " " + getFlavorText_e();
	}

	public String getFlavorText_j() {
		return flavorText;
	}

	public void setFlavorText_e(String flavorText_e) {
		this.flavorText_e = flavorText_e;
	}

	public String getFlavorText_e() {
		return flavorText_e;
	}

	public void setRealName(String name) {
		realCardName = name;
	}

	public String getRealName() {
		return realCardName;
	}

	public Card clone() {
		Card cloned = new Card(id, name);

		cloned.setRarity(rarity);
		cloned.setCount(dupCount);
		cloned.setEffects(rule);
		cloned.setEffects_e(rule_e);
		cloned.setGender(gender);
		cloned.setGender_e(gender_e);
		cloned.setElement(element);
		cloned.setElement_e(element_e);
		cloned.setAttack(attack);
		cloned.setDefense(defense);
		cloned.setAttackComp(attackComp);
		cloned.setDefenseComp(defenseComp);
		cloned.setT(t);
		cloned.setFlavorText(flavorText);
		cloned.setFlavorText_e(flavorText_e);
		cloned.setImageResource(imageResource);

		return cloned;
	}

	private void setEffects(ArrayList<String> rule) {
		this.rule = rule;
	}

	private void setEffects_e(ArrayList<String> rule_e) {
		this.rule_e = rule_e;
	}


	// Hard code special cases where you may put >4 cards in the deck
	// FZ/SE13-24 C
	// FZ/SE13-26 C
	// MF/S13-034 U
	// MF/S13-040 C
	// ID/W10-014 C
	// SG/W19-038 C
	// FT/SE10-29

	public static int getMaxInDeck(Card c) {
		return 4;
	}

	public String toString() {
		return name;
	}

	public void setAlternateArt(boolean isAlternateArt) {
		this.isAlternateArt = isAlternateArt;
	}

	public boolean isAlternateArt() {
		return isAlternateArt;
	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<Attribute> attributes) {
		this.attributes = attributes;
	}

	public ArrayList<Card> getAssociatedCards() {
		return associatedCards;
	}

	public void setAssociatedCards(ArrayList<Card> associatedCards) {
		this.associatedCards = associatedCards;
	}

	public UUID getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(UUID uniqueID) {
		this.uniqueID = uniqueID;
	}

}
