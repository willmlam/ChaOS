package CardAssociation;

public enum Attribute {
	ALL("All"), SHIFT("Shift"), ENCORE("Encore"), SUPPORT("Support"), BACKUP("Backup"), HEAL("Heal");

	String s;

	Attribute(String trigger) {
		s = trigger;
	}

	public String toString() {
		return s;
	}
}