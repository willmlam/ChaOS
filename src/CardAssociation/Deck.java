/**
 * @file Deck.java
 * @author Jia Chen
 * @date Sept 05, 2011
 * @description 
 * 		Deck.java is an object representation of the deck used in
 * 		this application.
 */

package CardAssociation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Deck {

	// deck properties
	private String deckName;
	public static final int MAX_DECK_SIZE = 50;
	public static final int MAX_EXTRA_DECK_SIZE = 10;
	// private int cardsRemain = 0;

	// deck statistics
	private int numChara;
//	private int numExtra;
	private int numEvent;
	private int numSet;
	
	private boolean hasPartner;

	private ArrayList<Card> cards;
	private ArrayList<Card> extraCards;
	private ArrayList<Card> unique;
	private ArrayList<Card> uniqueExtra;
	private ArrayList<CardWrapper> cardWrapper;
	private JFrame frame;

	public ArrayList<Card> shuffledCards;
	private Scanner s;

	// create a deck
	public Deck() {
		setDeckName("Untitled");
		cards = new ArrayList<Card>();
		extraCards = new ArrayList<Card>();
		unique = new ArrayList<Card>();
		uniqueExtra = new ArrayList<Card>();
		shuffledCards = new ArrayList<Card>();
		cardWrapper = new ArrayList<CardWrapper>();
		frame = new JFrame();
	}

	// add cards to a deck
	public boolean addCard(Card referenceCard, boolean verbose) {
		System.out.println("adding in Deck");
		ArrayList<Card> deck;
		ArrayList<Card> uniq;
		if (referenceCard.getT() != Type.EXTRA) {
			deck = cards;
			uniq = unique;
		} else {
			deck = extraCards;
			uniq = uniqueExtra;
		}

		// check if the deck has max number of cards
		if ((referenceCard.getT() != Type.EXTRA && deck.size() < MAX_DECK_SIZE) ||
			(referenceCard.getT() == Type.EXTRA && deck.size() < MAX_EXTRA_DECK_SIZE)) {
			
			// check if deck has a partner card already
			if (referenceCard.getT() == Type.PARTNER && hasPartner) {
				if (verbose) {
					JOptionPane.showMessageDialog(
							frame,
							"The deck already has a partner card.", 
							"Max 1 Partner",
							JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			boolean toAdd = true;
			int x = 0;
			for (; x < cardWrapper.size(); x++) {
				if (cardWrapper.get(x).containsCard(referenceCard)) {
					toAdd = cardWrapper.get(x).addCard(referenceCard);
					break;
				}
			}

			if (x == cardWrapper.size()) {
				CardWrapper newWrapper = new CardWrapper();
				newWrapper.setCardName(referenceCard.getName());
				cardWrapper.add(newWrapper);
				toAdd = cardWrapper.get(x).addCard(referenceCard);
			}

			// if not, check to see if there are 4 copies of a card
			if (deck.contains(referenceCard)) {
				// if not, then add
				// System.err.println(card.getName());
				if (referenceCard.getCardCount() < Card
						.getMaxInDeck(referenceCard) && toAdd) {
					referenceCard.addCount();
					for (int i = 0; i < deck.size(); i++) {
						Card tempCard = deck.get(i);
						if (referenceCard.compareTo(tempCard) == 0) {
							tempCard.setCount(referenceCard.getCardCount());
						}
					}

					Card card = referenceCard.clone();
					card.setName(card.getName());
					onlineUpdateStatistics(card, true);
					deck.add(card);
					if (card.getT() != Type.EXTRA)
						shuffledCards.add(card);
					if (card.getT() == Type.PARTNER)
						hasPartner = true;
					// System.err.println(card.toString());
					return true;
				} else if (verbose) {
					System.out.println(referenceCard.getName()
							+ " has maximum copies");
					// Warn the user that there are 4 copies existing
					JOptionPane.showMessageDialog(
							frame,
							"There are already the maximum copies ("
									+ Card.getMaxInDeck(referenceCard)
									+ ") of " + referenceCard.getName()
									+ " in the deck", "Max Copies",
							JOptionPane.WARNING_MESSAGE);
				}
			} else if (toAdd) {
				// card does not exist, add
				referenceCard.resetCount();
				referenceCard.addCount();
				Card card = referenceCard.clone();
				onlineUpdateStatistics(card, true);
				deck.add(card);
				if (card.getT() != Type.EXTRA)
					shuffledCards.add(card);
				if (card.getT() == Type.PARTNER)
					hasPartner = true;
				uniq.add(referenceCard);
				Collections.sort(uniq);

				return true;
			} else if (verbose) {
				System.out.println(referenceCard.getName()
						+ " has maximum copies");
				// Warn the user that there are 4 copies existing
				JOptionPane.showMessageDialog(
						frame,
						"There are already the maximum copies ("
								+ Card.getMaxInDeck(referenceCard) + ") of "
								+ referenceCard.getName() + " in the deck",
						"Max Copies", JOptionPane.WARNING_MESSAGE);
			}
		} else if (verbose) {
			System.out.println("FULL DECK");
			// Warn the user that it is a full deck
			JOptionPane.showMessageDialog(frame,
					"Deck is full!", "Full Deck",
					JOptionPane.WARNING_MESSAGE);
		}
		return false;
	}

	// remove a card form the deck
	public boolean removeCard(Card card) {
		ArrayList<Card> deck;
		ArrayList<Card> uniq;
		if (card.getT() != Type.EXTRA) {
			deck = cards;
			uniq = unique;
		} else {
			deck = extraCards;
			uniq = uniqueExtra;
		}
		// check to see if the card exists in the deck
		if (deck.contains(card)) {

			int x = 0;
			for (; x < cardWrapper.size(); x++) {
				if (cardWrapper.get(x).containsCard(card)) {
					cardWrapper.get(x).removeCount();
				}
			}

			onlineUpdateStatistics(card, false);
			if (card.getT() == Type.PARTNER)
				hasPartner = false;
			deck.remove(card);
			card.removeCount();
			for (int i = 0; i < deck.size(); i++) {
				Card tempCard = deck.get(i);
				if (card.compareTo(tempCard) == 0) {
					tempCard.setCount(card.getCardCount());
				}
				if (i < uniq.size()) {
					if (uniq.get(i).compareTo(card) == 0) {
						uniq.get(i).setCount(card.getCardCount());
					}
				}
			}
			System.out.println(card.getName() + " has "
					+ card.getCardCount() + " copies");
			if (card.getCardCount() == 0) {
				uniq.remove(card);
			}
			return true;
		}
		return false;
	}

	// get the deck list
	public ArrayList<Card> getCards() {
		return cards;
	}
	
	public ArrayList<Card> getExtraCards() {
		return extraCards;
	}

	public ArrayList<Card> getUnique() {
		return unique;
	}
	
	public ArrayList<Card> getUniqueExtra() {
		return uniqueExtra;
	}

	// public void setCardsRemain(int cardsRemain) {
	// this.cardsRemain = cardsRemain;
	// }
	//
	// public int getCardsRemain() {
	// return cardsRemain;
	// }

	// set the deck name
	public void setDeckName(String deckName) {
		this.deckName = deckName;
	}

	// get the deck name
	public String getDeckName() {
		return deckName;
	}

	// save the deck
	public void save(File file) {
		try {
			FileOutputStream fileOutput;
			ObjectOutputStream objectOutput;
			fileOutput = new FileOutputStream(file.getAbsolutePath());
			objectOutput = new ObjectOutputStream(fileOutput);

			objectOutput.writeObject(cards);
			objectOutput.writeObject(extraCards);
			objectOutput.writeObject(shuffledCards);

			objectOutput.close();

			// FileWriter fstream = new FileWriter(file.getParent() + "/"
			// + file.getName());
			// BufferedWriter out = new BufferedWriter(fstream);
			// for (int i = 0; i < cards.size(); i++) {
			// Card c = cards.get(i);
			// out.write(c.toString() + "\n");
			// }
			// out.close();
		} catch (IOException e) {
			System.out.println("File does not exist");
		}
	}

	public void saveRaw(File file) {
		try {
			OutputStreamWriter fw = new OutputStreamWriter(
					new FileOutputStream(file.getAbsolutePath()), "UTF-8");
			// FileWriter fw = new FileWriter(file.getAbsolutePath());
			for (Card c : unique) {
				for (int i = 0; i < c.getCardCount(); ++i)
					fw.write(c.getID() + "\n");
			}
			for (Card c : uniqueExtra) {
				for (int i = 0; i < c.getCardCount(); ++i)
					fw.write(c.getID() + "\n");
			}
			fw.close();
		} catch (IOException e) {
			System.out.println("File does not exist");
		}
	}

	public void loadRaw(File file, HashMap<String, Card> dictionary) {
		try {
			InputStreamReader fr = new InputStreamReader(new FileInputStream(
					file.getAbsolutePath()), "UTF-8");
			s = new Scanner(fr);

			java.util.Iterator<Card> valueList = dictionary.values().iterator();

			while (s.hasNextLine()) {
				String line = s.nextLine();

				// line = line.replace(" ", "");
				String pID = line.charAt(0) + "";

				for (int i = 1; i < line.length(); i++) {
					if ((Character.isLetter(line.charAt(i)) && Character
							.isDigit(line.charAt(i - 1)))
							|| (Character.isSpaceChar(line.charAt(i)) && Character
									.isDigit(line.charAt(i - 1)))) {
						break;
					} else {
						pID += line.charAt(i);
					}
				}

				System.out.println(pID);
				Card c = dictionary.get(line);
				if (c == null) {
					while (valueList.hasNext()) {
						Card temp = valueList.next();
						if (temp.meetsRequirement(pID,"","",null,"","",
								Integer.MIN_VALUE,Integer.MAX_VALUE,
								Integer.MIN_VALUE,Integer.MAX_VALUE,
								Integer.MIN_VALUE,Integer.MAX_VALUE,
								Integer.MIN_VALUE,Integer.MAX_VALUE,
								"","","")) {
							c = temp;
						}
					}
				}

				if (c != null)
					addCard(c, false);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// load the deck
	@SuppressWarnings("unchecked")
	public void load(File file, HashMap<String, Card> dictionary) {

		FileInputStream fileInput;
		ObjectInputStream objectInput;

		try {
			fileInput = new FileInputStream(file.getAbsolutePath());
			objectInput = new ObjectInputStream(fileInput);
			cards = (ArrayList<Card>) objectInput.readObject();
			extraCards = (ArrayList<Card>) objectInput.readObject();
			offlineUpdateStatistics();
			shuffledCards = (ArrayList<Card>) objectInput.readObject();
			System.out.println("card count: " + shuffledCards.size());
			objectInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// try {
		// Scanner scan = new Scanner(file);
		// cards.clear();
		// shuffledCards.clear();
		// while (scan.hasNext()) {
		// Scanner lineScan = new Scanner(scan.nextLine())
		// .useDelimiter(":");
		// String id = lineScan.next();
		// String name = lineScan.next();
		// addCard(dictionary.get(id));
		// System.out.println("card count: " + shuffledCards.size());
		// }
		// } catch (FileNotFoundException e) {
		// System.out.println("File does not exist");
		// }
	}

	// get a shuffled deck for playing
	public ArrayList<Card> getPlayingDeck() {
		Collections.shuffle(shuffledCards);
		return shuffledCards;
	}

	// Update stats
	private void onlineUpdateStatistics(Card c, boolean inc) {
		int offset;
		if (inc)
			offset = 1;
		else
			offset = -1;

		switch (c.getT()) {
		case CHARA:
		case PARTNER:
			numChara += offset;
			break;
			/*
		case EXTRA:
			numExtra += offset;
			break;
			*/
		case EVENT:
			numEvent += offset;
			break;
		case SET:
			numSet += offset;
			break;
		default:
		}
	}

	private void offlineUpdateStatistics() {
		numChara = 0;
//		numExtra = 0;
		numEvent = 0;
		numSet = 0;
		for (Card c : cards) {
			onlineUpdateStatistics(c, true);
		}
	}

	// Statistics methods
	public int getNumCards() {
		return cards.size();
	}

	public int getNumChara() {
		return numChara;
	}

	public int getNumExtra() {
		return extraCards.size();
	}

	public int getNumEvent() {
		return numEvent;
	}
	
	public int getNumSet() {
		return numSet;
	}

	public void printStatistics() {
		System.out.println("# cards:" + getNumCards());
		System.out.println("# Chara:" + getNumChara());
		System.out.println("# Extra:" + getNumExtra());
		System.out.println("# Event:" + getNumEvent());
		System.out.println("# Set:" + getNumSet());
	}
}
