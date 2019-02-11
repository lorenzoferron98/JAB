package jab.models.format;

public class AppointmentParseException extends IllegalArgumentException {
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
