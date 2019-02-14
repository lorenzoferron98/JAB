package jab;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.AsciiTableException;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.u8.U8_Grids;
import de.vandermeer.skb.interfaces.document.TableRowStyle;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import jab.jbook.util.InputUtils;
import jab.models.Appointment;
import jab.models.Book;
import org.apache.commons.lang3.SystemUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Lorenzo Ferron
 * @version 2019.02.11
 * @see Appointment
 * @see Book
 */
public class GUI {

    private static final Logger LOGGER = Logger.getLogger(GUI.class.getName());
    private Book book;
    private boolean saved;
    private AsciiTable at;

    public GUI(String[] args) {
        setupAsciiTable();
        book = args.length == 0 ? new Book() : new Book(args[0]);
        saved = true;
    }

    // ======================================================
    // (START) ASCII TABLE METHODS
    // ======================================================

    private void setupAsciiTable() {
        at = new AsciiTable();
        at.getContext().setGrid(U8_Grids.borderStrongDoubleLight());
        at.getRenderer().setCWC(new CWC_LongestLine());
    }

    private void showAsciiTable() {
        at.setTextAlignment(TextAlignment.CENTER);
        System.out.println(at.render());
        System.out.println();
    }

    private void initAsciiTable(boolean search) {
        at.getRawContent().clear();
        at.addRule(TableRowStyle.STRONG);
        try {
            if (search) {
                at.addRow("", "Date", "Start Time", "Duration", "Description (with)", "Place");
            } else {
                at.addRow("Date", "Start Time", "Duration", "Description (with)", "Place");
            }
            at.addRule(TableRowStyle.STRONG);
        } catch (AsciiTableException e) {
            setupAsciiTable();
            initAsciiTable(search);
        }
    }

    private void rowAppointment(Appointment appointment, boolean search, int index) {
        if (search) {
            at.addRow(index, appointment.getDate().format(Appointment.FORMATTER_DATE),
                    appointment.getStartTime().format(Appointment.FORMATTER_TIME),
                    appointment.getDuration().toMinutes(),
                    appointment.getDescription(),
                    appointment.getPlace());
        } else {
            at.addRow(appointment.getDate().format(Appointment.FORMATTER_DATE),
                    appointment.getStartTime().format(Appointment.FORMATTER_TIME),
                    appointment.getDuration().toMinutes(),
                    appointment.getDescription(),
                    appointment.getPlace());
        }
        at.addRule();
    }

    // ======================================================
    // (END) ASCII TABLE METHODS
    // ======================================================

    public void start() {
        try {
            Map<Appointment, Appointment> collisions = book.loadBookFromFile();
            if (!collisions.isEmpty()) {
                for (Map.Entry<Appointment, Appointment> entry : collisions.entrySet()) {
                    initAsciiTable(false);
                    rowAppointment(entry.getKey(), false, 0);
                    at.addRow(null, null, null, null, "COLLIDES WITH");
                    at.addRule();
                    rowAppointment(entry.getValue(), false, 0);
                    showAsciiTable();
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.warning(e.getMessage() + "\n");
        } catch (IOException e) {
            e.getStackTrace();
            System.exit(1);
        }
        clearScreen();
        displayLogo();
        displayMenu();
    }

    // ======================================================
    // (START) GENERAL PURPOSE METHODS
    // ======================================================

    /**
     * This method clear the current screen
     *
     * <p>
     * On Windows systems, ANSI escapecode are supported by PowerShell 6.0.
     * </p>
     */
    private void clearScreen() {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.out.print("`e[H`e[2J");
        } else {
            System.out.print("\033[H\033[2J");
        }
        System.out.flush();
    }

    private void displayMenu() {
        showMenu();
        selectAction(choose(8, false));
    }

    private int choose(int end, boolean subMenu) {
        int choice = -1;
        do {
            try {
                String subExitQuery = subMenu ? ", 0 to quit" : "";
                choice = InputUtils.readInt("Enter a number [1-" + end + subExitQuery + "]: ");
                if (choice < 0 || choice > end || !subMenu && choice == 0) {
                    LOGGER.info("Number is not valid [1-" + end + subExitQuery + "]\n");
                    System.out.print("[Re-]");
                }
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid input, this is not a number.\n");
                System.out.print("[Re-]");
            }
        } while (choice < 0 || choice > end || !subMenu && choice == 0);
        return choice;
    }

    private void selectAction(int choice) {
        switch (choice) {
            case 1:
                addAction();
                break;
            case 2:
                deleteAction();
                break;
            case 3:
                editAction();
                break;
            case 4:
                searchAction();
                break;
            case 5:
                printAction();
                break;
            case 6:
                saveAction();
                break;
            case 7:
                saveAsAction();
                break;
            case 8:
                quitAction();
                break;
            default:
                break;
        }
        displayMenu();
    }

    // ======================================================
    // (END) GENERAL PURPOSE METHODS
    // ======================================================

    private void quitAction() {
        if (saved) {
            System.out.println("BYE!");
            System.exit(0);
        } else {
            String choice;
            do {
                choice = InputUtils.readString("Do you want to close without saving the changes? [y/N] ");
                if (choice.toLowerCase().matches("y|n|yes|no") || choice.isEmpty()) {
                    System.out.println();
                } else {
                    LOGGER.info("Please try again\n");
                }
            } while (!(choice.toLowerCase().matches("y|n|yes|no") || choice.isEmpty()));
            if (!(choice.toLowerCase().contains("n") || choice.isEmpty())) {
                System.out.println("BYE!");
                System.exit(0);
            }
        }
    }

    // ======================================================
    // (START) EDITING METHODS FOR BOOK
    // ======================================================

    private int selectResult(List<Appointment> results) {
        printBook(results, true);
        int choice = choose(results.size(), true) - 1;
        System.out.println();
        return choice;
    }

    private void editAction() {
        List<Appointment> results = methodsBasedOnSearch("Main Menu > Edit an existing appoitment");
        if (results != null && !results.isEmpty()) {
            int choice = selectResult(results);
            try {
                Appointment old = results.get(choice);
                String date = InputUtils.readString("Date[" + old.getDate().format(Appointment.FORMATTER_DATE) + "]: ");
                String startTime = InputUtils.readString("Start Time[" + old.getStartTime().format(Appointment.FORMATTER_TIME) + "]: ");
                String duration = InputUtils.readString("Duration (in minutes)[" + old.getDuration().toMinutes() + "]: ");
                String description = InputUtils.readString("Description (with)[" + old.getDescription() + "]: ");
                String place = InputUtils.readString("Place[" + old.getPlace() + "]: ");
                Appointment newAppointment = book.edit(old, date, startTime, duration, description, place);
                if (newAppointment == null) {
                    saved = false;
                } else {
                    printCollision(newAppointment);
                }
                System.out.println();
            } catch (IndexOutOfBoundsException e) {
                if (choice != -1) {
                    LOGGER.warning("Invalid appointment [1-" + results.size() + "]\n");
                }
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid input, Duration is not a number.\n");
            } catch (Exception e) {
                LOGGER.warning(e.getMessage() + "\n");
            }
        }
    }

    private void deleteAction() {
        List<Appointment> results = methodsBasedOnSearch("Main Menu > Remove an existing appointment");
        if (results != null && !results.isEmpty()) {
            int choice = selectResult(results);
            try {
                if (book.delete(results.get(choice))) {
                    saved = false;
                }
            } catch (IndexOutOfBoundsException e) {
                if (choice != -1) {
                    LOGGER.warning("Invalid appointment [1-" + results.size() + "]\n");
                }
            }
        }
    }

    // ======================================================
    // (END) EDITING METHODS FOR BOOK
    // ======================================================

    // ======================================================
    // (START) PRINTING METHODS FOR BOOK
    // ======================================================

    private void printCollision(Appointment appointment) {
        System.out.println("COLLIDES WITH");
        System.out.println("\tDate (dd-MM-yyyy): " + appointment.getDate().format(Appointment.FORMATTER_DATE));
        System.out.println("\tStart Time (HH-mm): " + appointment.getStartTime().format(Appointment.FORMATTER_TIME));
        System.out.println("\tDuration (in minutes): " + appointment.getDuration().toMinutes());
        System.out.println("\tDescription (with): " + appointment.getDescription());
        System.out.println("\tPlace: " + appointment.getPlace());
        System.out.println();
    }

    private void printAction() {
        clearScreen();
        System.out.println("Main Menu > Show appointments sorted for date\n");
        printBook(book.getSortedBook(), false);
    }

    private void printBook(List<Appointment> book, boolean search) {
        if (book.isEmpty()) {
            System.out.println("This book is empty !");
            System.out.println();
        } else {
            initAsciiTable(search);
            for (int i = 0; i < book.size(); i++) {
                rowAppointment(book.get(i), search, i + 1);
            }
            showAsciiTable();
        }
    }

    // ======================================================
    // (END) PRINTING METHODS FOR BOOK
    // ======================================================

    // ======================================================
    // (START) SEARCHING METHODS FOR BOOK
    // ======================================================

    private List<Appointment> methodsBasedOnSearch(String msg) {
        clearScreen();
        System.out.println(msg + "\n");
        List<Appointment> results = search();
        if (results != null) {
            System.out.println();
            System.out.println("RESULTS FOUND: " + results.size());
            if (results.isEmpty()) {
                System.out.println();
            }
        }
        return results;
    }

    private void searchAction() {
        List<Appointment> results = methodsBasedOnSearch("Main Menu > Search an existing appointment for...");
        if (results != null && !results.isEmpty()) {
            Collections.sort(results);
            printBook(results, false);
        }
    }

    private List<Appointment> search() {
        List<Appointment> bookSorted = book.getSortedBook();
        printBook(bookSorted, false);
        if (!bookSorted.isEmpty()) {
            System.out.println(
                    "Options:\n" +
                            "\n" +
                            "       1. date\n" +
                            "       2. description\n");
            try {
                switch (choose(2, true)) {
                    case 1:
                        String date = InputUtils.readString("Date: ");
                        return book.search(Book.forDate(date));
                    case 2:
                        String description = InputUtils.readString("Description (with): ");
                        return book.search(Book.forDescription(description));
                    default:
                        System.out.println();
                        break;
                }
            } catch (Exception e) {
                LOGGER.warning(e.getMessage() + "\n");
            }
        }
        return null;
    }

    // ======================================================
    // (END) SEARCHING METHODS FOR BOOK
    // ======================================================

    // ======================================================
    // (START) SAVING METHODS FOR BOOK
    // ======================================================

    private void saveAction() {
        try {
            book.saveBookToFile();
            saved = true;
        } catch (NoSuchFileException e) {
            LOGGER.warning("No path found\n");
            System.out.println("Main Menu > Save > Save as...\n");
            saveAsAction();
        } catch (AccessDeniedException e) {
            LOGGER.warning("Write permission denied\n");
            System.out.println("Main Menu > Save > Save as...\n");
            saveAsAction();
        } catch (IOException e) {
            LOGGER.warning(e.getMessage() + "\n");
            System.out.println("Main Menu > Save > Save as...\n");
            saveAsAction();
        }
    }

    private void saveAsAction() {
        String filename = InputUtils.readString("Path [" + book.getFile() + "]: ");
        book.setFile(filename.isEmpty() ? book.getFile().getAbsolutePath() : filename);
        saveAction();
    }

    // ======================================================
    // (END) SAVING METHODS FOR BOOK
    // ======================================================

    private void addAction() {
        clearScreen();
        System.out.println("Main Menu > Insert a new appointment\n");
        System.out.println("* = required\n");
        try {
            String date = InputUtils.readString("Date* (dd-MM-yyyy): ");
            String startTime = InputUtils.readString("Start Time* (HH-mm): ");
            int duration = InputUtils.readInt("Duration* (in minutes): ");
            String description = InputUtils.readString("Description* (with): ");
            String place = InputUtils.readString("Place*: ");
            Appointment appointment = book.add(date, startTime, duration, description, place);
            if (appointment == null) {
                saved = false;

            } else {
                printCollision(appointment);
            }
            System.out.println();
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid input, Duration is not a number.\n");
        } catch (Exception e) {
            LOGGER.warning(e.getMessage() + "\n");
        }
    }

    // ======================================================
    // (START) ASCII ART METHODS
    // ======================================================

    private void showMenu() {
        System.out.print(
                "+--------------------------------------------------+\n" +
                        "|                                                  |\n" +
                        "| Options:                                         |\n" +
                        "|                                                  |\n" +
                        "|        1. Insert a new appointment               |\n" +
                        "|        2. Remove an existing appointment         |\n" +
                        "|        3. Edit an existing appoitment            |\n" +
                        "|        4. Search an existing appointment for:    |\n" +
                        "|              date                                |\n" +
                        "|              description                         |\n" +
                        "|        5. Show appointments sorted for date      |\n" +
                        "|        6. Save                                   |\n" +
                        "|        7. Save as...                             |\n" +
                        "|                                                  |\n" +
                        "|        8. QUIT                                   |\n" +
                        "|                                                  |\n" +
                        "+--------------------------------------------------+\n");
        System.out.println();
    }

    private void displayLogo() {
        System.out.print(
                "+--------------------------------------------------+\n" +
                        "|                     __ ___    ___                |\n" +
                        "|                 __ / // _ |  / _ )               |\n" +
                        "|                / // // __ | / _  |               |\n" +
                        "|                \\___//_/ |_|/____/                |\n" +
                        "|                                                  |\n" +
                        "|           by Lorenzo Ferron (20024182)           |\n" +
                        "|                                                  |\n");
    }

    // ======================================================
    // (END) ASCII ART METHODS
    // ======================================================

}
