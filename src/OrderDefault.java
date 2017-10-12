import fr.enseeiht.danck.voice_analyzer.*;

public final class OrderDefault extends Order {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String displayName;
	
	protected OrderDefault(String name, String displayName) {
		super(name);
		this.displayName = displayName;
	}

	String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
