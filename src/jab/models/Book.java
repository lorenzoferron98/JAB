package jab.models;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * The {@code Book} represent a simply real Appointment Book.
 * It implements all basic operation to realize an
 * Appointment scheduling software in CLI mode.
 *
 * @author Lorenzo Ferron
 * @version %G%
 * @see Appointment
 */
public class Book implements Iterable<Appointment> {
    private static final String DEFAULT_FILE = "book.csv";
    private static final Logger LOGGER = Logger.getLogger(Book.class.getName());

    private final List<Appointment> book;

    private File file;

    /**
     * Constructs and initializes an empty appointment book with default filename
     * associated.
     */
    public Book() {
        this(DEFAULT_FILE);
    }

    /**
     * Constructs and initializes an empty appointment book with custom filename
     * associated.
     *
     * @param filename the name of the file containing appointments
     */
    public Book(String filename) {
        file = new File(filename);
        book = new ArrayList<>();
    }

    /**
     * Returns true if the {@code current} appointment does not overlap with its
     * duration the {@code other}.
     *
     * @param current the current appointment
     * @param other   another appointment
     * @return true if appointments as arguments do not overlap themselves, false
     * otherwise
     */
    private static boolean overlaps(Appointment current, Appointment other) {
        return current.getStartInstant().equals(other.getStartInstant()) && current.getEndInstant().equals(other.getEndInstant()) ||
                current.getStartInstant().compareTo(other.getEndInstant()) < 0 && other.getStartInstant().compareTo(current.getEndInstant()) < 0;
    }

    /**
     * Returns a predicate that tests if an appointment and a date are equal
     * according to {@link LocalDate#equals(Object)}.
     *
     * @param date the String with which to compare for equality
     * @return a predicate that tests if an appointment and a date are equal
     * @throws DateTimeParseException if the text cannot be parsed to a date
     * @see Book#search(Predicate)
     */
    public static Predicate<Appointment> forDate(String date) throws DateTimeParseException {
        return p -> p.getDate().equals(LocalDate.parse(date, Appointment.FORMATTER_DATE));
    }

    /**
     * Returns a predicate that tests if a {@code description} is contained
     * into an appointment according to {@link String#contains(CharSequence)}
     *
     * @param description the String with which to compare
     * @return a predicate that tests if a {@code description} is contained into an
     * appointment
     * @throws IllegalArgumentException if the {@code String} contains any
     *                                  {@code SEPARARATOR} or empty string
     * @see Book#search(Predicate)
     * @see Appointment#SEPARATOR
     */
    public static Predicate<Appointment> forDescription(String description) throws IllegalArgumentException {
        Appointment.checkString(description, "Description", " field must not contain a SEPARATOR char (" + Appointment.SEPARATOR + ")");
        return p -> p.getDescription().toLowerCase().contains(description.toLowerCase());
    }

    /**
     * Gets the file field.
     *
     * @return the file, not null
     */
    public File getFile() {
        return file;
    }

    /**
     * Changes the filename that is the file.
     *
     * @param filename the new filename, not null
     */
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * Writes this book on {@code file}.
     *
     * @throws IOException if an I/O error occurs while writing to {@code file}
     */
    public void saveBookToFile() throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file.toPath())) {
            for (Appointment appointment : book) {
                bufferedWriter.write(appointment.toString());
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Reads {@code file} and loads this book.
     *
     * @return A couple appointments that collide.
     * @throws IOException if an I/O error occurs while reading to {@code file}
     */
    public Map<Appointment, Appointment> loadBookFromFile() throws IOException {
        Map<Appointment, Appointment> collisions = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            Appointment overlapped, current;
            int loaded = 0;
            while ((line = bufferedReader.readLine()) != null) {
                loaded++;
                try {
                    current = Appointment.parse(line);
                    overlapped = add(current);
                    if (overlapped != null) {
                        collisions.put(current, overlapped);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warning("Line " + loaded + ": duration is not a positive integer" + System.lineSeparator());
                } catch (DateTimeParseException | IllegalArgumentException e) {
                    LOGGER.warning("Line " + loaded + ": " + e.getMessage() + System.lineSeparator());
                }
            }
        }
        return collisions;
    }

    /**
     * Appends a new appointment to this book specifying all data about an
     * appointment.
     *
     * <p>
     * The new appointment must not collide with another.
     * </p>
     *
     * @param date        The String that represents a date.
     * @param startTime   The String that represents an appointment time.
     * @param duration    the duration, measured in minutes
     * @param description The String containing the person’s name with who
     *                    appointment is made.
     * @param place       The String containing the name of a place where
     *                    appointment is made.
     * @return an overlapped appointment, null otherwise
     * @throws DateTimeParseException   if the text cannot be parsed to a date or
     *                                  appointment time
     * @throws IllegalArgumentException if the {@code String} does not contain a
     *                                  parsable {@code Appointment}.
     * @see Appointment#Appointment(String, String, int, String, String)
     */
    public Appointment add(String date, String startTime, int duration, String description, String place) throws DateTimeParseException, IllegalArgumentException {
        return add(new Appointment(date, startTime, duration, description, place));
    }

    /**
     * Appends a new appointment to this book.
     *
     * @param appointment the new appointment, not null
     * @return an overlapped appointment, null otherwise
     * @see Book#overlapsAll(Appointment)
     */
    public Appointment add(Appointment appointment) {
        Appointment overlapped = overlapsAll(appointment);
        if (overlapped == null) {
            book.add(appointment);
        }
        return overlapped;
    }

    /**
     * Checks if {@code appointment} collides with another.
     *
     * @param appointment an appointment to be checked
     * @return an overlapped appointment, null otherwise
     * @see Book#overlaps(Appointment, Appointment)
     */
    public Appointment overlapsAll(Appointment appointment) {
        for (Appointment current : book) {
            if (overlaps(current, appointment)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Returns search result for {@code filter}.
     *
     * @param filter the criterion for selecting
     * @return a list of appointments
     */
    public List<Appointment> search(Predicate<Appointment> filter) {
        List<Appointment> results = new ArrayList<>();
        for (Appointment current : book) {
            if (filter.test(current)) {
                results.add(current);
            }
        }
        return results;
    }

    /**
     * Removes an {@code appointment} from this book.
     *
     * @param appointment appointment to be removed from this book, if present
     * @return true if this book contained the specified appointment
     */
    public boolean delete(Appointment appointment) {
        return book.remove(appointment);
    }

    /**
     * Returns a sorted clone of this book.
     *
     * @return a sorted clone of this book, not null
     */
    public List<Appointment> getSortedBook() {
        List<Appointment> sortedBook = new ArrayList<>(book);
        Collections.sort(sortedBook);
        return sortedBook;
    }

    /**
     * Edits one or more field of existing appointment.
     *
     * <p>
     * For every change it follows a check for possible overlaps.
     * </p>
     *
     * @param old         an existing appointment
     * @param date        The String that represents a new date.
     * @param startTime   The String that represents a new appointment time.
     * @param duration    the new duration, measured in minutes
     * @param description The String containing the new person’s name with who
     *                    appointment is made.
     * @param place       The String containing the new name of a place where
     *                    appointment is made.
     * @return an overlapped appointment, null otherwise
     * @throws DateTimeParseException   if the text cannot be parsed to a date or
     *                                  appointment time
     * @throws IllegalArgumentException if the {@code String} does not contain a
     *                                  parsable {@code Appointment}.
     */
    public Appointment edit(Appointment old, String date, String startTime, String duration, String description, String place) throws DateTimeParseException, IllegalArgumentException {
        Appointment newAppointment = new Appointment(date.isEmpty() ? old.getDate().format(Appointment.FORMATTER_DATE) : date,
                startTime.isEmpty() ? old.getStartTime().format(Appointment.FORMATTER_TIME) : startTime,
                duration.isEmpty() ? (int) old.getDuration().toMinutes() : Integer.parseInt(duration),
                description.isEmpty() ? old.getDescription() : description,
                place.isEmpty() ? old.getPlace() : place);
        if (old.equals(newAppointment)) {
            return null;
        }
        delete(old);
        Appointment overlapped = overlapsAll(newAppointment);
        if (overlapped != null) {
            book.add(old);
            return overlapped;
        }
        book.add(newAppointment);
        return null;
    }

    /**
     * Returns an iterator over elements of type {@code Appointment}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Appointment> iterator() {
        return new BookIterator();
    }

    private class BookIterator implements Iterator<Appointment> {
        int cursor;         // index of next element to return
        int lastRet = -1;   // index of last element returned; -1 if no such

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return cursor != book.size();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Appointment next() {
            int i = cursor;
            if (i >= book.size()) {
                throw new NoSuchElementException();
            }
            cursor = i + 1;
            return book.get(lastRet = i);
        }

        /**
         * Removes from the underlying collection the last element returned
         * by this iterator (optional operation).  This method can be called
         * only once per call to {@link #next}.  The behavior of an iterator
         * is unspecified if the underlying collection is modified while the
         * iteration is in progress in any way other than by calling this
         * method.
         *
         * @throws UnsupportedOperationException if the {@code remove}
         *                                       operation is not supported by this iterator
         *                                       {@link UnsupportedOperationException} and performs no other action.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
