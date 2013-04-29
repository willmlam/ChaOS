/**
 * @file Importer.java
 * @author Jia Chen
 * @date Sept 06, 2011
 * @description 
 * 		Importer.java is used for parsing in csv files and images 
 * 		into object representations of the cards
 */

package DeckBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import CardAssociation.*;

public class Importer {

	private static ArrayList<Card> setCards = new ArrayList<Card>();
	private static HashMap<String, Card> allCards = new HashMap<String, Card>();
	private Writer nameDict;
	private Scanner scanner;
	private static Scanner s;

	public Importer() {
		try {
			nameDict = new BufferedWriter(new FileWriter(new File(
					"nameDict.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// parsing the given file to fill setCards and allCards
	/*
	 * public void scan(File file) {
	 * 
	 * try { Scanner scanner = new Scanner(file); scanner.nextLine();
	 * 
	 * while (scanner.hasNext()) { Scanner lineScan = new
	 * Scanner(scanner.nextLine()) .useDelimiter(";");
	 * 
	 * String cardId = lineScan.next(); String rarity = lineScan.next(); String
	 * name = lineScan.next(); String color = lineScan.next(); String side =
	 * lineScan.next(); String cardType = lineScan.next(); int level =
	 * lineScan.nextInt(); int cost = lineScan.nextInt(); int power =
	 * lineScan.nextInt(); int soul = lineScan.nextInt(); String trait1 =
	 * lineScan.next(); String trait2 = lineScan.next(); String trigger =
	 * lineScan.next();
	 * 
	 * Card card = new Card(cardId, name); card.setCost(cost); //
	 * card.setDamage(damage) card.setLevel(level); card.setPower(power);
	 * card.setSoul(soul); card.setTrait1(trait1); card.setTrait2(trait2);
	 * 
	 * System.out.println(trigger);
	 * card.setTrigger(Trigger.convertString(trigger));
	 * 
	 * if (color.equalsIgnoreCase("red")) { card.setC(CCode.RED); } else if
	 * (color.equalsIgnoreCase("blue")) { card.setC(CCode.BLUE); } else if
	 * (color.equalsIgnoreCase("yellow")) { card.setC(CCode.YELLOW); } else if
	 * (color.equalsIgnoreCase("green")) { card.setC(CCode.GREEN); }
	 * 
	 * if (cardType.equalsIgnoreCase("character")) { card.setT(Type.CHARACTER);
	 * } else if (cardType.equalsIgnoreCase("event")) { card.setT(Type.EVENT); }
	 * else if (cardType.equalsIgnoreCase("climax")) { card.setT(Type.CLIMAX); }
	 * 
	 * String textFlavor = lineScan.next(); String cardEffect = lineScan.next();
	 * card.setFlavorText(textFlavor); card.addEffect(cardEffect);
	 * 
	 * String imageName = lineScan.next();
	 * 
	 * card.setImage(new File("WS-AB150px/" + imageName));
	 * 
	 * Card c = allCards.put(name, card); if (c == null) { // new card
	 * setCards.add(card); } else { card.setID(cardId); //allCards.put(name,
	 * card); }
	 * 
	 * // System.out.println(trigger); }
	 * 
	 * } catch (FileNotFoundException e) { System.out.println("File Not Found");
	 * }
	 * 
	 * for (int i = 0; i < setCards.size(); i++) { //
	 * System.out.println(setCards.get(i).toString()); }
	 * 
	 * serializingLists(); }
	 */


	private String rename(String str) throws IOException {
		String resultString = str.charAt(0) + "";
		String testResultString = str.charAt(0) + "";
		String regex1 = "[^a-zA-Z]*[a-zA-Z0-9\\p{Punct}\\s]*[^a-zA-Z0-9\\p{Punct}[\\s]]+[^a-zA-Z\\s0-9\"]+$";
		String regex2 = "[^a-zA-Z]+[a-zA-Z0-9\\p{Punct}\\s]*[^a-zA-Z0-9[\\s]\"]+$";
		String regex3 = "[a-zA-Z0-9\\p{Punct}\\s]+$";

		if (Pattern.matches(regex1, str) || Pattern.matches(regex2, str)
				|| Pattern.matches(regex3, str) || str.contains("Rock Cannon")) {
			return str;
		}

		do {
			str = str.replace("  ", " ");
		} while (str.contains("  "));

		for (int i = 1; i < str.length(); i++) {

			testResultString += str.charAt(i);
			if (Pattern.matches(regex1, testResultString))
				resultString = testResultString;
		}
		resultString = resultString.trim();
		if (!resultString.equals(str)) {
			nameDict.write("[" + str + "  =>  " + resultString + "]\n");
		}
		return resultString;
	}

	private void close() {
		try {
			nameDict.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// serialize the content into "CardData"
	protected void serializingLists() {

		FileOutputStream fileOutput;
		ObjectOutputStream objectOutput;
		try {
			fileOutput = new FileOutputStream("src/resources/CardDatav2");
			objectOutput = new ObjectOutputStream(fileOutput);

			objectOutput.writeObject(setCards);
			objectOutput.writeObject(allCards);

			objectOutput.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void loadFromSQLiteDB() throws SQLiteException {
		Importer importer = new Importer();
		SQLiteConnection db = new SQLiteConnection(new File("ChaOS_DB.sqlite"));
		db.open();
		
		HashMap<String,Integer> fieldMap = new HashMap<String,Integer>();
		

		SQLiteStatement st = db.prepare("SELECT * from cardtable");
		if (st.step()) {
			for (int i = 0; i < st.columnCount(); ++i) {
				fieldMap.put(st.getColumnName(i), i);
			}
		}
		st.dispose();
		
//		private String id;
//		private String rarity;
//		private String pID;
//		private String name;
//		private String name_kana;
//		private String name_e;
//		private Type t;
//		private int attack;
//		private int defense;
//		private int attackComp;
//		private int defenseComp;
//		private String gender;
//		private String gender_e;
//		private String element;
//		private String element_e;
//		private ArrayList<String> rule;
//		private ArrayList<String> rule_e;
//		private String flavorText;
//		private String flavorText_e;
//		private String expansion;
//		private String expansion_e;
//		private String series;
//		private String series_e;
		
		st = db.prepare("SELECT * from cardtable");
		while (st.step()) {
			Card c = new Card();
			
			c.setName(st.columnString(fieldMap.get("name")));
			c.setName_e(st.columnString(fieldMap.get("name_e")));
			c.setName_kana(st.columnString(fieldMap.get("name_kana")));
			c.setGender(st.columnString(fieldMap.get("gender")));
			c.setGender_e(st.columnString(fieldMap.get("gender_e")));
			c.setElement(st.columnString(fieldMap.get("element")));
			c.setElement_e(st.columnString(fieldMap.get("element_e")));
			c.setExpansion(st.columnString(fieldMap.get("expansion")));
			c.setExpansion_e(st.columnString(fieldMap.get("expansion_e")));
			c.setSeries(st.columnString(fieldMap.get("series")));
			c.setSeries_e(st.columnString(fieldMap.get("series_e")));
			
			s = new Scanner(st.columnString(fieldMap.get("rule")));
			while (s.hasNextLine()) {
				c.addEffect(s.nextLine());
			}
			
			s = new Scanner(st.columnString(fieldMap.get("rule_e")));
			while (s.hasNextLine()) {
				c.addEffect_e(s.nextLine());
			}
			
			String temp = st.columnString(fieldMap.get("type"));
			if (temp.equalsIgnoreCase("chara")) {
				c.setT(Type.CHARA);
			} else if (temp.equalsIgnoreCase("extra")) {
				c.setT(Type.EXTRA);
			} else if (temp.equalsIgnoreCase("event")) {
				c.setT(Type.EVENT);
			} else if (temp.equalsIgnoreCase("set")) {
				c.setT(Type.SET);
			} else if (temp.equalsIgnoreCase("partner")) {
				c.setT(Type.PARTNER);
			}
			

			if (c.getT() == Type.EVENT || c.getT() == Type.SET) {
				c.setAttack(-1);
				c.setDefense(-1);
			}
			else {
				c.setAttack(st.columnInt(fieldMap.get("attack")));
				c.setDefense(st.columnInt(fieldMap.get("defense")));
				c.setAttackComp(st.columnInt(fieldMap.get("attackcomp")));
				c.setDefenseComp(st.columnInt(fieldMap.get("defensecomp")));
			}
			
			c.setFlavorText(st.columnString(fieldMap.get("flavor")));
			c.setFlavorText_e(st.columnString(fieldMap.get("flavor_e")));
			
			String setid = st.columnString(fieldMap.get("setid"));
			String imagefilename = st.columnString(fieldMap.get("imagefilename"));
			if (imagefilename.equals("chaos_00.jpg")) {
				c.setImageResource("/resources/cardimages/chaos_00.jpg");	
			}
			else {
				c.setImageResource("/resources/cardimages/" + setid + "/" + imagefilename);
			}
			if (!new File("src" + c.getImageResource()).exists()) {
				// if (newCard.getImage().exists()) {
				// Card c = allCards.put(newCard.getCardName(),
				// newCard);
				c.setImageResource("/resources/cardimages/chaos_00.jpg");	
			}
			System.out.println(c.getImageResource());
			
			c.setID(st.columnString(fieldMap.get("cardid")));
			c.setRarity(st.columnString(fieldMap.get("rarity")));
			//c.setID((c.isAlternateArt() ? cid + "_alt" : cid));
			Card cc = allCards.put(c.getID(), c);
			if (cc == null) {
				setCards.add(c);

			} else {
				// c.setID(newCard.getID());
				// allCards.put(newCard.getCardName(), c);
				allCards.put(c.getID(), cc);
				// if (!c.getImage().exists()) {
				// setCards.remove(c);
				// setCards.add(newCard);
				// }
			}
		}
		System.out.println("# of cards = " + setCards.size());
		System.out.println("# of cards in binder = " + allCards.size());
		Collections.sort(setCards);
		System.out.println(System.getProperty("os.name"));
		importer.serializingLists();
		importer.close();
//		for (String key : fieldMap.keySet()) {
//			System.out.println(key + " " + fieldMap.get(key));
//		}

	}

	public static void main(String[] args) throws SQLiteException {
		//loadFromTxt();
		loadFromSQLiteDB();
	}
}
