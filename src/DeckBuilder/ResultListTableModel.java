package DeckBuilder;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import CardAssociation.*;

public class ResultListTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 3975544447965529587L;
	
	private String[] columnNames = { "ID", "Name", "Type", "Gen", "Elem", "ATK", "DEF", "AC", "DC"};
	private Class<?>[] types = {
			String.class, 
			String.class, 
			Type.class,
			String.class, 
			String.class, 
			Integer.class,
			Integer.class,
			Integer.class,
			Integer.class};
	
	private ArrayList<Card> cardlist;
	
	public ResultListTableModel() {
	}
	
	public ResultListTableModel(ArrayList<Card> cardlist) {
		this.cardlist = cardlist;
	}
	
	public void setCardList(ArrayList<Card> cardlist) {
		this.cardlist = cardlist;
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return cardlist.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Card c = cardlist.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return c.getID() + " " + c.getRarity();
		case 1:
			return c.getName();
		case 2:
			return c.getT();
		case 3:
			return c.getGender();
		case 4:
			return c.getElement();
		case 5:
			return c.getAttack();
		case 6:
			return c.getDefense();
		case 7:
			return c.getAttackComp();
		case 8:
			return c.getDefenseComp();
		default:
			return null;
		}
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}	
}
