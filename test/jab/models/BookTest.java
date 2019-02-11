package jab.models;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The test class {@code BookTest}.
 *
 * @author Lorenzo Ferron
 * @version %G%
 * @see Appointment
 */
public class BookTest {

    private Book book = new Book("agenda.csv");

    /**
     * Sets up the test fixture.
     * <p>
     * Called before every test case method.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        book.add(Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         | 64277 Pleasure Pass"));
        book.add(Appointment.parse("03-02-2019 | 08-57 | 123 | Kirbie Sterman         | 3 Scofield Way"));
        book.add(Appointment.parse("05-12-2018 | 05-55 | 54  | Fredra Robilart        | 1622 Marcy Center"));
        book.add(Appointment.parse("27-01-2019 | 17-48 | 54  | Trip Dameisele         | 061 Westerfield Lane"));
        book.add(Appointment.parse("30-12-2018 | 05-04 | 167 | Essa Cranshaw          | 5884 Esker Plaza"));
    }

    /**
     * Tears down the test fixture.
     * <p>
     * Called after every test case method.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        book = new Book();
    }

    @Test
    public void bookOpIO() {
        try {
            book.loadBookFromFile();
            fail("Mi aspettavo un'eccezione");
        } catch (IOException ignored) {
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            book.setFile(System.getenv("SystemDrive") + File.separator + "ag3nda.csv");
        } else {
            book.setFile("/ag3nda.csv");
        }
        try {
            book.saveBookToFile();
            fail("Mi aspettavo un'eccezione");
        } catch (IOException ignored) {
        }
        String defaultPath = System.getProperty("user.home") + File.separator + "ag3nda.csv";
        book.setFile(defaultPath);
        try {
            book.saveBookToFile();
        } catch (IOException e) {
            fail("Non mi aspettavo un'eccezione");
        }
        Book actual = new Book(defaultPath);
        try {
            actual.loadBookFromFile();
        } catch (IOException e) {
            fail("Non mi aspettavo un'eccezione");
        }
        assertEquals(book.getSortedBook().size(), actual.getSortedBook().size());
        Iterator<Appointment> iterator = book.iterator();
        assertNotNull(iterator);
        Appointment previous = iterator.next();
        while (iterator.hasNext()) {
            Appointment recent = iterator.next();
            assertTrue(actual.delete(recent));
        }
        assertTrue(actual.delete(previous));
        assertTrue(actual.getSortedBook().isEmpty());
        try {
            Files.deleteIfExists(Paths.get(defaultPath));
        } catch (IOException e) {
            fail("Non mi aspettavo un'eccezione");
        }
    }

    @Test
    public void add() {
        assertNull(book.add(Appointment.parse("31-12-2018 | 00-32 | 187 | Teresina Deer          | 322 Kings Hill")));
        Appointment tmp = Appointment.parse("05-12-2018 | 05-55 | 54  | Fredra Robilart        | 1622 Marcy Center");
        Appointment overlapped = book.add(tmp);
        assertNotNull(overlapped);
        assertEquals(overlapped, tmp);
        tmp = Appointment.parse("30-12-2018 | 03-04 | 240 | Pattin Flippen          | 81 Bayside Road");
        overlapped = book.add(tmp);
        assertNotNull(overlapped);
        assertTrue("30-12-2018".equals(overlapped.getDate().format(Appointment.FORMATTER_DATE)) &&
                "05-04".equals(overlapped.getStartTime().format(Appointment.FORMATTER_TIME)) &&
                overlapped.getDuration().toMinutes() == 167 && "Essa Cranshaw".equals(overlapped.getDescription()) &&
                "5884 Esker Plaza".equals(overlapped.getPlace()));
    }

    @Test
    public void overlapsAll() {
        assertNull(book.overlapsAll(Appointment.parse("31-12-2018 | 00-32 | 187 | Teresina Deer          | 322 Kings Hill")));
        assertNotNull(book.overlapsAll(Appointment.parse("30-12-2018 | 03-04 | 240 | Pattin Flippen          | 81 Bayside Road")));
        assertNotNull(book.overlapsAll(Appointment.parse("05-12-2018 | 05-55 | 54  | Fredra Robilart        | 1622 Marcy Center")));
        // casi limiti
        assertNull(book.overlapsAll(Appointment.parse("31-12-2018 | 03-40 | 10 | Sunshine Clingoe           | 5779 Coleman Alley")));
        assertNotNull(book.overlapsAll(Appointment.parse("27-01-2019 | 18-41 | 10  | Niko Spooner         | 070 Moulton Place")));
    }

    @Test
    public void search() {
        assertEquals(0, book.search(Book.forDate("31-12-2018")).size());
        assertEquals(1, book.search(Book.forDate("30-12-2018")).size());
        try {
            book.search(Book.forDate("29-02-2018"));
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException ignored) {
        }
        try {
            book.search(Book.forDate("31-02-2018"));
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException ignored) {
        }
        try {
            book.search(Book.forDate("29/02/2018"));
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException ignored) {
        }
        assertEquals(0, book.search(Book.forDescription("Lorenzo Ferron")).size());
        assertEquals(1, book.search(Book.forDescription("Essa Cranshaw")).size());
        try {
            book.search(Book.forDescription("Lorenzo | Ferron"));
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Description field must not contain a SEPARATOR char (" + Appointment.SEPARATOR + ")", e.getMessage());
        }
        try {
            book.search(Book.forDescription(""));
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Description must not be empty", e.getMessage());
        }
    }

    @Test
    public void delete() {
        assertTrue(book.delete(Appointment.parse("30-12-2018 | 05-04 | 167 | Essa Cranshaw          | 5884 Esker Plaza")));
        assertFalse(book.delete(Appointment.parse("30-12-2018 | 03-04 | 240 | Pattin Flippen          | 81 Bayside Road")));
    }

    @Test
    public void iterator() {
        Iterator<Appointment> iterator = book.iterator();
        assertNotNull(iterator);
        List<Appointment> sortedBook = book.getSortedBook();
        Appointment previous = iterator.next();
        while (iterator.hasNext()) {
            Appointment recent = iterator.next();
            assertTrue(previous.toDateTime().isBefore(recent.toDateTime()) || previous.toDateTime().isEqual(recent.toDateTime()));
            assertTrue(sortedBook.remove(recent));
        }
        assertTrue(sortedBook.remove(previous));
        assertTrue(sortedBook.isEmpty());
    }

    @Test
    public void edit() {
        assertNull(book.edit(Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         | 64277 Pleasure Pass"), "",
                "10-13", "", "", ""));
        assertNotNull(book.edit(Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         | 64277 Pleasure Pass"), "03-02-2019",
                "08-30", "180", "", ""));
        assertNull(book.edit(Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         | 64277 Pleasure Pass"), "",
                "", "", "", ""));
        try {
            book.edit(Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         | 64277 Pleasure Pass"), "",
                    "", "cento", "", "");
            fail("Mi aspettavo un'eccezione");
        } catch (NumberFormatException ignore) {
        }
    }
}