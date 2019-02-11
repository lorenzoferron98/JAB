package jab.models;

import jab.models.format.AppointmentParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.format.DateTimeParseException;

import static org.junit.Assert.*;

/**
 * The test class {@code AppointmentTest}.
 *
 * @author Lorenzo Ferron
 * @version 2018.02.11
 * @see Appointment
 */
public class AppointmentTest {

    private Appointment fixed;

    /**
     * Sets up the test fixture.
     * <p>
     * Called before every test case method.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        fixed = Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         | 64277 Pleasure Pass");
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
    }

    @Test
    public void checkString() {
        Appointment.checkString("Yovonnda Knapper", "Description", "an error message");
        Appointment.checkString("7 Delaware Crossing", "Place", "an error message");
        try {
            Appointment.checkString("", "Description", "an error message");
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Description must not be empty", e.getMessage());
        }
        try {
            Appointment.checkString("Nerti | Castagneto", "Description", " an error message");
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Description an error message", e.getMessage());
        }
    }

    @Test
    public void parse() {
        assertNotNull(Appointment.parse("22-04-2018|20-00|105|Yovonnda Knapper|7 Delaware Crossing"));
        assertNotNull(Appointment.parse("22-04-2018\t|\t21-45\t|\t239 |   Nerti Castagneto|58723 Glacier Hill Court"));
        assertNotNull(Appointment.parse("22-04-2018 | 21-45 | 1 |Allene Merryfield|81 Sheridan Circle"));
        try {
            Appointment.parse("22-04-2018|20-00|105\n|Yovonnda Knapper|7 Delaware Crossing");
            fail("Mi aspettavo un'eccezione");
        } catch (AppointmentParseException e) {
            assertEquals("Two or more lines detected", e.getMessage());
        }
        try {
            Appointment.parse("22-04-2018|20-00||Yovonnda Knapper|7 Delaware Crossing");
            fail("Mi aspettavo un'eccezione");
        } catch (AppointmentParseException e) {
            assertEquals("Illegal parsing", e.getMessage());
        }
    }

    @Test
    public void setDate() {
        fixed.setDate("24-12-2018");
        assertEquals("24-12-2018", fixed.getDate().format(Appointment.FORMATTER_DATE));
        try {
            fixed.setDate("29-02-2018");
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException e) {
            assertEquals("24-12-2018", fixed.getDate().format(Appointment.FORMATTER_DATE));
        }
        try {
            fixed.setDate("31-02-2018");
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException e) {
            assertEquals("24-12-2018", fixed.getDate().format(Appointment.FORMATTER_DATE));
        }
        try {
            fixed.setDate("24/02/2018");
        } catch (DateTimeParseException e) {
            assertEquals("24-12-2018", fixed.getDate().format(Appointment.FORMATTER_DATE));
        }
        fixed.setDate("29-02-2016");
        assertEquals("29-02-2016", fixed.getDate().format(Appointment.FORMATTER_DATE));
        fixed.setDate("05-03-2016"); // ambiguous date 5th March or 3rd May, but valid
        assertEquals("05-03-2016", fixed.getDate().format(Appointment.FORMATTER_DATE));
    }

    @Test
    public void setStartTime() {
        fixed.setStartTime("10-37");
        assertEquals("10-37", fixed.getStartTime().format(Appointment.FORMATTER_TIME));
        try {
            fixed.setStartTime("09:56");
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException e) {
            assertEquals("10-37", fixed.getStartTime().format(Appointment.FORMATTER_TIME));
        }
        try {
            fixed.setStartTime("24-00"); // ambiguous time
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException e) {
            assertEquals("10-37", fixed.getStartTime().format(Appointment.FORMATTER_TIME));
        }
        try {
            fixed.setStartTime("09-67");
            fail("Mi aspettavo un'eccezione");
        } catch (DateTimeParseException e) {
            assertEquals("10-37", fixed.getStartTime().format(Appointment.FORMATTER_TIME));
        }
    }

    @Test
    public void setDuration() {
        fixed.setDuration(128);
        assertEquals(128, fixed.getDuration().toMinutes());
        try {
            fixed.setDuration(-96);
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Sorry, -96 is an invalid duration. Please enter only minutes (>0).", e.getMessage());
            assertEquals(128, fixed.getDuration().toMinutes());
        }
    }

    @Test
    public void setDescription() {
        fixed.setDescription("Kirbie Sterman");
        assertEquals("Kirbie Sterman", fixed.getDescription());
        try {
            fixed.setDescription("Kirbie |Sterman");
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Kirbie Sterman", fixed.getDescription());
            assertEquals("Description field must not contain a SEPARATOR char (" + Appointment.SEPARATOR + ")", e.getMessage());
        }
        try {
            fixed.setDescription("");
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("Kirbie Sterman", fixed.getDescription());
            assertEquals("Description must not be empty", e.getMessage());
        }
    }

    @Test
    public void setPlace() {
        fixed.setPlace("3 Scofield Way");
        assertEquals("3 Scofield Way", fixed.getPlace());
        try {
            fixed.setPlace("3 Scof|ield Way");
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("3 Scofield Way", fixed.getPlace());
            assertEquals("Place field must not contain a SEPARATOR char (" + Appointment.SEPARATOR + ")", e.getMessage());
        }
        try {
            fixed.setPlace("");
            fail("Mi aspettavo un'eccezione");
        } catch (IllegalArgumentException e) {
            assertEquals("3 Scofield Way", fixed.getPlace());
            assertEquals("Place must not be empty", e.getMessage());
        }
    }

    @Test
    public void equals() {
        Appointment actual = new Appointment("24-12-2018", "09-13", 127, "Gun De Ambrosi", "64277 Pleasure Pass");
        assertEquals(actual, fixed);
        assertEquals(fixed, fixed);
        assertNotEquals("", fixed);
        Appointment another = Appointment.parse("24-12-2018 | 09-13 | 127 | Gun De Ambrosi         |227 Beilfuss Road");
        assertNotEquals(another, fixed);
        assertNotEquals(fixed, another);
    }
}