package jab.models;

import jab.models.format.AppointmentParseException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.Locale;

/**
 * The {@code Appointment} class represents an appointment into {@code Book}.
 *
 * @author Lorenzo Ferron
 * @version 2019.02.11
 * @see Book
 */
public final class Appointment implements Comparable<Appointment> {
    /**
     * The string is used to serialize this appointment
     */
    public static final String SEPARATOR = "|";

    /**
     * The date format choosen
     */
    public static final String FORMAT_DATE = "dd-MM-uuuu";

    /**
     * The formatter for date based on {@code FORMAT_DATE}
     */
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(FORMAT_DATE, Locale.ITALY).withResolverStyle(ResolverStyle.STRICT);

    /**
     * The time format chosen
     */
    public static final String FORMAT_TIME = "HH-mm";

    /**
     * The formatter for date based on {@code FORMAT_TIME}
     */
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern(FORMAT_TIME, Locale.ITALY).withResolverStyle(ResolverStyle.STRICT);


    private static final String ZONE_ID = "Europe/Rome";
    private static final int ARGC = 5;
    private static final int DATE_COLUMN = 0;
    private static final int TIME_COLUMN = 1;
    private static final int DURATION_COLUMN = 2;
    private static final int DESCR_COLUMN = 3;
    private static final int PLACE_COLUMN = 4;

    private LocalDate date;
    private LocalTime startTime;
    private Duration duration;
    private String description;
    private String place;

    /**
     * Constructs and initializes an appointment with required parameters.
     * The {@code date} must have the following syntax:
     * <pre>
     *     dd-MM-yyyy ({@code FORMAT_DATE})
     * </pre>
     * The {@code startTime} must have the following syntax:
     * <pre>
     *     HH-mm ({@code FORMAT_TIME})
     * </pre>
     *
     * @param date        The String that represents a date as described above.
     * @param startTime   The String that represents an appointment time as described
     *                    above.
     * @param duration    the duration, measured in minutes
     * @param description The String containing the person’s name with whom
     *                    appointment is made.
     * @param place       The String containing the name of a place where
     *                    appointment is made.
     * @throws DateTimeParseException   if the text cannot be parsed to a date or
     *                                  appointment time
     * @throws IllegalArgumentException if the duration is negative
     */
    public Appointment(String date, String startTime, int duration, String description, String place) {
        setDate(date);
        setStartTime(startTime);
        setDuration(duration);
        setDescription(description);
        setPlace(place);
    }

    /**
     * Checks that {@code str} does not contain any {@code SEPARARATOR} or empty
     * string.
     *
     * @param str       the String to be tested
     * @param fieldName the String containing name of field to be tested
     * @param msg       The error message for the exception.
     * @throws IllegalArgumentException if the {@code String} contains any
     *                                  {@code SEPARATOR} or empty string
     */
    public static void checkString(String str, String fieldName, String msg) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " " + "must not be empty");
        }
        if (str.contains(SEPARATOR)) {
            throw new IllegalArgumentException(fieldName + msg);
        }
    }

    /**
     * Parses the string argument as an appointment.
     *
     * <p>
     * An exception of type {@code AppointmentParseException}
     * (a subclass of {@link IllegalArgumentException}) is
     * thrown if any of the following situations occurs:
     * <ul>
     * <li>The string contains multiple lines.</li>
     * <li>The string contains few fields.</li>
     * </ul>
     * </p>
     *
     * <p>
     * An exception of type {@code IllegalArgumentException} is
     * thrown if any of the situations describe in
     * {@link Appointment#Appointment(String, String, int, String, String)} occur.
     * </p>
     *
     * @param line the String containing the appointment representation to be parsed
     * @return the appointment represented by the string argument, not null
     * @throws DateTimeParseException   if the text cannot be parsed to a date or
     *                                  appointment time
     * @throws IllegalArgumentException if the {@code String} does not contain a
     *                                  parsable {@code Appointment}.
     * @see AppointmentParseException
     */
    public static Appointment parse(String line) {
        if (line.contains("\n") || line.contains("\r")) {
            throw new AppointmentParseException("Two or more lines detected");
        }
        String[] values = line.split("\\s?+[\\" + SEPARATOR + "\\s]++");
        if (values.length != ARGC) {
            throw new AppointmentParseException("Illegal parsing");
        }
        return new Appointment(values[DATE_COLUMN], values[TIME_COLUMN], Integer.parseInt(values[DURATION_COLUMN]), values[DESCR_COLUMN], values[PLACE_COLUMN]);
    }

    /**
     * Gets the date field.
     *
     * @return the date, not null
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Registers the parsed {@code date} in an appointment.
     *
     * @param date the String containing the date representation to be parsed
     * @throws DateTimeParseException if the text cannot be parsed to a date
     */
    public void setDate(String date) {
        this.date = LocalDate.parse(date, FORMATTER_DATE);
    }

    /**
     * Gets the appointment time field.
     *
     * @return the appointment time, not null
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Registers the parsed {@code startTime} in an appointment.
     *
     * @param startTime the String containing the appointment time representation to be
     *                  parsed
     * @throws DateTimeParseException if the text cannot be parsed to an appointment
     *                                time
     */
    public void setStartTime(String startTime) {
        this.startTime = LocalTime.parse(startTime, FORMATTER_TIME);
    }

    /**
     * Gets the duration field.
     *
     * @return the duration, not null
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Registers the created {@code duration} in an appointment.
     *
     * @param duration the duration, measured in minutes
     * @throws IllegalArgumentException if the duration is negative
     */
    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Sorry, " + duration + " is an invalid duration. Please enter only minutes (>0).");
        }
        this.duration = Duration.ofMinutes(duration);
    }

    /**
     * Gets the description field.
     *
     * @return the description, not null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Registers the checked {@code description} in an appointment.
     *
     * @param description The String containing the description without SEPARATOR
     *                    string or is not empty.
     * @throws IllegalArgumentException if the text contains SEPARATOR string or is
     *                                  empty
     */
    public void setDescription(String description) {
        checkString(description, "Description", " field must not contain a SEPARATOR char (" + SEPARATOR + ")");
        this.description = description;
    }

    /**
     * Gets the place field.
     *
     * @return the place, not null
     */
    public String getPlace() {
        return place;
    }

    /**
     * Registers the checked {@code place} in an appointment.
     *
     * @param place The String containing the place without SEPARATOR string or is
     *              not empty.
     * @throws IllegalArgumentException if the text contains SEPARATOR string or is
     *                                  empty
     */
    public void setPlace(String place) {
        checkString(place, "Place", " field must not contain a SEPARATOR char (" + SEPARATOR + ")");
        this.place = place;
    }

    /**
     * Converts this {@code date}-{@code startTime} to an {@code LocalDateTime}.
     *
     * <p>
     * Useful to calculate overlap between two appointments in {@link Book}.
     * </p>
     *
     * @return an {@code LocalDateTime} representing the same appointment date-time,
     * not null
     * @see Book
     */
    public LocalDateTime toDateTime() {
        return LocalDateTime.of(date, startTime);
    }

    /**
     * Gets the start {@code Instant} of this appointment.
     *
     * @return the start {@code Instant}, not null
     * @see Appointment#toDateTime()
     */
    public Instant getStartInstant() {
        return toDateTime().atZone(ZoneId.of(ZONE_ID)).toInstant();
    }

    /**
     * Gets the end {@code Instant} of this appointment.
     *
     * @return the end {@code Instant}, not null
     * @see Appointment#toDateTime()
     */
    public Instant getEndInstant() {
        return toDateTime().plusMinutes(duration.toMinutes()).atZone(ZoneId.of(ZONE_ID)).toInstant();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return date.format(FORMATTER_DATE) + SEPARATOR + startTime.format(FORMATTER_TIME) + SEPARATOR + duration.toMinutes() + SEPARATOR + description + SEPARATOR + place;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return
     * {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)}
     * should return {@code true} if and only if
     * {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if
     * {@code x.equals(y)} returns {@code true} and
     * {@code y.equals(z)} returns {@code true}, then
     * {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of
     * {@code x.equals(y)} consistently return {@code true}
     * or consistently return {@code false}, provided no
     * information used in {@code equals} comparisons on the
     * objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Appointment) {
            final Appointment otherAppointment = (Appointment) obj;
            return this.date.equals(otherAppointment.getDate()) &&
                    this.startTime.equals(otherAppointment.getStartTime()) &&
                    this.duration.equals(otherAppointment.getDuration()) &&
                    this.description.equals(otherAppointment.getDescription()) &&
                    this.place.equals(otherAppointment.getPlace());
        }
        return false;
    }

    /**
     * Compares this object with the specified object to order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     *
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param otherAppointment the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Appointment otherAppointment) {
        return this.toDateTime().compareTo(otherAppointment.toDateTime());
    }
}