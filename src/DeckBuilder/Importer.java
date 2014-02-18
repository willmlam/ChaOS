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
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import CardAssociation.*;

public class Importer {

	private static ArrayList<Card> setCards = new ArrayList<Card>();
	private static HashMap<String, Card> allCards = new HashMap<String, Card>();
	private Writer nameDict;
	private static Scanner s;

	public Importer() {
		try {
			nameDict = new BufferedWriter(new FileWriter(new File(
					"nameDict.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			}
			
			if (c.getT() != Type.EVENT) {
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
				if (setid.equals("VA")) setid="va";
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
