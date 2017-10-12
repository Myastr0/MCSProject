import fr.enseeiht.danck.voice_analyzer.*;

public final class TokenDefault extends Token {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String displayName;
    private final boolean recordable;
    
	protected TokenDefault(String name, String displayName, boolean recordable) {
		super(name);
		this.displayName = displayName;
        this.recordable = recordable;
	}

	 String getDisplayName() {
	        return this.displayName;
	    }

	    boolean isRecordable() {
	        return this.recordable;
	    }

	    @Override
	    public String toString() {
	        return this.displayName;
	    }
}
