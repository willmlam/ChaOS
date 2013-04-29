/**
 * @file State.java
 * @author Jia Chen
 * @date Sept 06, 2011
 * @description 
 * 		Type.java is an enumeration of the different card type
 */

package CardAssociation;

public enum Type {
	ALL(""), PARTNER("Partner"), CHARA("Chara"), EXTRA("Extra"), EVENT("Event"), SET("Set");

	String s;

	Type(String type) {
		s = type;
	}

	public String toString() {
		return s;
	}
	
	public int getNumericCode() {
    	switch(this) {
	    	case ALL:
	    		return 0;
	    	case CHARA:
	    		return 1;
	    	case EXTRA:
	    		return 2;
	    	case EVENT:
	    		return 3;
	    	case SET:
	    		return 4;
	    	default:
    			return 255;
    	}
    }
}
