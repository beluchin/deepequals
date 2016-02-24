package deepequals;

@SuppressWarnings("serial")
public class IllegalClassException extends IllegalArgumentException {
    public IllegalClassException(final String msg) {
        super(msg);
    }
}