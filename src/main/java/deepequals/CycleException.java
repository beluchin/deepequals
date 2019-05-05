package deepequals;

public final class CycleException extends IllegalArgumentException {
    CycleException(final String msg) {
        super(msg);
    }
}
