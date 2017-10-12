import fr.enseeiht.danck.voice_analyzer.*;

public final class ParserDefault implements Parser {

	private State state;
	
	protected enum State {
        DANCK_ONLY, ORDER, SEQUENCE
    }
	
    ParserDefault() {
        this.state = State.DANCK_ONLY;
    }
    
	@Override
	public Token[] nextTokenSet() {
		switch (this.state) {
        	case DANCK_ONLY:
        		return new Token[]{Token.valueOf("DANCK")};
        	case ORDER:
        		// Expects anything bug SEQUENCE_NAME
        		return new Token[]{
        				Token.valueOf("SILENCE"),
        				Token.valueOf("DANCK"),
        				Token.valueOf("STANDBY"),
        				Token.valueOf("FORWARD"),
        				Token.valueOf("BACKWARD"),
        				Token.valueOf("FLIP"),
        				Token.valueOf("ROTATE_LEFT"),
        				Token.valueOf("ROTATE_RIGHT"),
        				Token.valueOf("HOVER_LEFT"),
        				Token.valueOf("HOVER_RIGHT"),
        				Token.valueOf("INVERTED_PENDULUM"),
        				Token.valueOf("SEQUENCE")
        		};
        	case SEQUENCE:
        		return new Token[]{Token.valueOf("DANCK"), Token.valueOf("SEQUENCE_NAME")};
        	default:
        		throw new RuntimeException("Wut?!");
		}	
	}

	@Override
	public MetaOrder parse(MetaToken metaToken) throws UnknownOrderException {
		Token token = metaToken.getToken();

        switch (this.state) {
            case DANCK_ONLY:
                // Expects DANCK or XXX

                if (token.equals(Token.valueOf("DANCK"))) {
                    this.state = State.ORDER;
                } else if (!token.equals(Token.valueOf("XXX"))) {
                    // Do nothing if other than DANCK
                    throw new AssertionError("Only 'DANCK' and 'XXX' tokens are allowed here");
                }

                break;
            case ORDER:
                // Expects anything but SEQUENCE_NAME

                if (token.equals(Token.valueOf("SEQUENCE"))) {
                    this.state = State.SEQUENCE;
                } else if (token.equals(Token.valueOf("SEQUENCE_NAME"))) {
                    throw new AssertionError("Token 'SEQUENCE_NAME' is not allowed here");
                } else if (!token.equals(Token.valueOf("DANCK"))) {
                    // Accept DANCK several times
                    this.state = State.DANCK_ONLY;
                    try {
                        return new MetaOrder(Order.valueOf(token.name()));
                    } catch (IllegalArgumentException iae) {
                        throw new UnknownOrderException();
                    }
                }

                break;
            case SEQUENCE:
                // Expects DANCK, SEQUENCE_NAME or XXX

                this.state = State.DANCK_ONLY;

                if (token.equals(Token.valueOf("DANCK"))) {
                    // Reset
                    this.state = State.ORDER;
                } else if (token.equals(Token.valueOf("SEQUENCE_NAME"))) {
                    String sequence = metaToken.getData();
                    return new MetaOrder(Order.valueOf("SEQUENCE"), sequence);
                } else if (!token.equals(Token.valueOf("XXX"))) {
                    throw new UnknownSequenceException();
                } else {
                    throw new AssertionError("Only 'SEQUENCE_NAME' and 'XXX' tokens are allowed here");
                }

            default:
                throw new RuntimeException("Wut?!");
        }

        return null;
	}
	
	final class UnknownSequenceException extends UnknownOrderException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}
	
}


