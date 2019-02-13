package jab.models.format;

public class AppointmentParseException extends IllegalArgumentException {
    private static final long serialVersionUID = 3190062889334977404L;

    /**
     * Constructs an <code>AppuntamentoParseException</code> with no
     * detail message.
     */
    public AppointmentParseException() {
    }

    /**
     * Constructs an <code>AppuntamentoParseException</code> with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    public AppointmentParseException(String s) {
        super(s);
    }
}
