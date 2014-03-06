package city;

import java.util.ArrayList;
import java.util.List;

import UnitTestingCommon.interfaces.AgentInterface;
import agent.Agent;

// Passes info about a payment obligation from one Agent to another
public class Bill {
	public final int ID;
	public final AgentInterface billee, biller;
	public final ArrayList<Bill.Item> items;
	private double total;
	
	public Bill(int ID, AgentInterface billee, AgentInterface biller) {
		this.ID = ID;
		this.billee = billee;
		this.biller = biller;
		items = new ArrayList<Bill.Item>();
		total = 0.0;
	}
	
	public Bill copy() {
		Bill b = new Bill(ID, billee, biller);
		for (Item i : items) {
			b.addItem(i.memo, i.amount);
		}
		return b;
	}
	
	public void addItem(final String memo, final double amount) {
		items.add(new Bill.Item(memo, amount));
		total += amount;
	}
	
	public double getTotal() {
		return total;
	}
	
	public static class Item {
		public final String memo;
		public final double amount;
		public Item(String memo, double amount) {
			this.memo = memo;
			this.amount = amount;
		}
	}
}