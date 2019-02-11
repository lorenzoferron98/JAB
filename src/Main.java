import jab.GUI;

/**
 * Main class for JAB.
 */
public class Main {

    /**
     * Entry point for JAB.
     *
     * @param args use args[0] to pass a filename
     */
    public static void main(String[] args) {
        GUI gui = new GUI(args);
        gui.start();
    }
}
